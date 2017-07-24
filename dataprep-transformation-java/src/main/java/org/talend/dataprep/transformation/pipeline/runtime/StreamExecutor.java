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
import java.util.Iterator;
import java.util.stream.Stream;

import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.node.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StreamExecutor extends ExecutorVisitor<Stream<DataSetRow>> {

    private Stream<DataSetRow> stream;

    @Override
    public Runnable toRunnable() {
        return () -> {
            final Iterator<DataSetRow> iterator = stream.iterator();
            while (iterator.hasNext()) {
                iterator.next();
            }
        };
    }

    @Override
    public Stream<DataSetRow> visitSource(SourceNode sourceNode) {
        try {
            final URL url = new URL(sourceNode.getSourceUrl());
            final ObjectMapper mapper = new ObjectMapper();
            final JsonParser parser = mapper.getFactory().createParser(url.openStream());
            final DataSet dataSet = mapper.reader(DataSet.class).readValue(parser);

            stream = dataSet.getRecords();
            super.visitSource(sourceNode);
            return stream;
        } catch (IOException e) {
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public Stream<DataSetRow> visitLocalSource(LocalSourceNode localSourceNode) {
        stream = localSourceNode.getSource();
        return super.visitLocalSource(localSourceNode);
    }

    @Override
    public Stream<DataSetRow> visitAction(ActionNode actionNode) {
        stream = stream.map(toFunction(actionNode));
        return super.visitAction(actionNode);
    }

    @Override
    public Stream<DataSetRow> visitCompile(CompileNode compileNode) {
        stream = stream.peek(toConsumer(compileNode));
        return super.visitCompile(compileNode);
    }

    @Override
    public Stream<DataSetRow> visitFilterNode(FilterNode filterNode) {
        stream = stream.filter(handleFilterNode(filterNode));
        return super.visitFilterNode(filterNode);
    }

    @Override
    public Stream<DataSetRow> visitCleanUp(CleanUpNode cleanUpNode) {
        stream = stream.onClose(() -> cleanUpNode.getContext().cleanup());
        return super.visitCleanUp(cleanUpNode);
    }

    @Override
    public Stream<DataSetRow> visitStep(StepNode stepNode) {
        stream = stream.map(handleStepNode(stepNode));
        return super.visitStep(stepNode);
    }

    @Override
    public Stream<DataSetRow> visitLimit(LimitNode limitNode) {
        stream = stream.limit(limitNode.getLimit());
        return super.visitLimit(limitNode);
    }
}
