// ============================================================================
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.pipeline.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.dataset.row.InvalidMarker;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.link.ZipLink;
import org.talend.dataprep.transformation.pipeline.node.*;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

public class ReactorExecutor extends ExecutorVisitor<Flux<DataSetRow>> {

    private final Map<Node, Flux<DataSetRow>> clonedLinks = new HashMap<>();

    private Flux<DataSetRow> flux;

    @Override
    public Runnable toRunnable() {
        return () -> flux.subscribe();
    }

    @Override
    public Flux<DataSetRow> visitAction(ActionNode actionNode) {
        flux = flux.map(toFunction(actionNode));
        return super.visitAction(actionNode);
    }

    @Override
    public Flux<DataSetRow> visitCompile(CompileNode compileNode) {
        final Consumer<DataSetRow> consumer = toConsumer(compileNode);
        flux = flux.map(row -> {
            consumer.accept(row);
            return row;
        });
        return super.visitCompile(compileNode);
    }

    @Override
    public Flux<DataSetRow> visitSource(SourceNode sourceNode) {
        try {
            final URL url = new URL(sourceNode.getSourceUrl());
            final ObjectMapper mapper = new ObjectMapper();
            final JsonParser parser = mapper.getFactory().createParser(url.openStream());
            final DataSet dataSet = mapper.reader(DataSet.class).readValue(parser);

            flux = Flux.create(fluxSink -> dataSet.getRecords().forEach(fluxSink::next));
        } catch (IOException e) {
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
        return super.visitSource(sourceNode);
    }

    @Override
    public Flux<DataSetRow> visitLocalSource(LocalSourceNode localSourceNode) {
        flux = Flux.create(fluxSink -> localSourceNode.getSource().forEach(fluxSink::next));
        return super.visitLocalSource(localSourceNode);
    }

    @Override
    public Flux<DataSetRow> visitStep(StepNode stepNode) {
        flux = flux.map(handleStepNode(stepNode));
        return super.visitStep(stepNode);
    }

    @Override
    public Flux<DataSetRow> visitFilterNode(FilterNode filterNode) {
        flux = flux.filter(handleFilterNode(filterNode));
        return super.visitFilterNode(filterNode);
    }

    @Override
    public Flux<DataSetRow> visitCleanUp(CleanUpNode cleanUpNode) {
        flux = flux.doOnTerminate(() -> cleanUpNode.getContext().cleanup());
        return super.visitCleanUp(cleanUpNode);
    }

    @Override
    public Flux<DataSetRow> visitLimit(LimitNode limitNode) {
        flux = flux.take(limitNode.getLimit());
        return super.visitLimit(limitNode);
    }

    @Override
    public Flux<DataSetRow> visitZipLink(ZipLink zipLink) {
        for (Node node : zipLink.getSources()) {
            flux = flux.zipWith(clonedLinks.remove(node), (dataSetRow, dataSetRow2) -> dataSetRow);
        }
        return super.visitZipLink(zipLink);
    }

    @Override
    public Flux<DataSetRow> visitCloneLink(CloneLink cloneLink) {
        for (Node node : cloneLink.getNodes()) {
            final ReactorExecutor visitor = new ReactorExecutor();
            node.accept(visitor);
            clonedLinks.put(node, visitor.flux);
        }
        return super.visitCloneLink(cloneLink);
    }

    @Override
    public Flux<DataSetRow> visitStatistics(StatisticsNode statisticsNode) {
        flux = flux.map(row -> {
            final Analyzer<Analyzers.Result> configuredAnalyzer = statisticsNode.getConfiguredAnalyzer();
            final List<ColumnMetadata> filteredColumns = statisticsNode.getFilteredColumns();
            configuredAnalyzer.analyze(row.filter(filteredColumns).order(filteredColumns).toArray(DataSetRow.SKIP_TDP_ID));
            return row;
        });
        return super.visitStatistics(statisticsNode);
    }

    @Override
    public Flux<DataSetRow> visitInvalidDetection(InvalidDetectionNode invalidDetectionNode) {
        flux = flux.map(row -> {
            final InvalidMarker invalidMarker = invalidDetectionNode.getInvalidMarker();
            invalidMarker.apply(row);
            return row;
        });
        return super.visitInvalidDetection(invalidDetectionNode);
    }
}
