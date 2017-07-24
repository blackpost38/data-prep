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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.node.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RuntimeNodeVisitor extends ExecutorVisitor<RuntimeNode> {

    private Stream<DataSetRow> stream;
    private RuntimeNode lastNode;

    @Override
    public RuntimeNode visitAction(ActionNode actionNode) {
        lastNode = super.visitAction(actionNode);

        final Function<DataSetRow, DataSetRow> function = toFunction(actionNode);
        final RuntimeNode nextNode = lastNode;
        return new RuntimeNode() {
            @Override
            public void receive(DataSetRow row, RowMetadata metadata) {
                final DataSetRow modifiedRow = function.apply(row);
                if (nextNode != null) {
                    nextNode.receive(modifiedRow, metadata);
                }
            }

            @Override
            public void receive(DataSetRow[] rows, RowMetadata[] metadatas) {
                for (int i = 0; i < rows.length; i++) {
                    receive(rows[i], metadatas[i]);
                }
            }

            @Override
            public void signal(Signal signal) {
                lastNode.signal(signal);
            }
        };
    }

    @Override
    public RuntimeNode visitCompile(CompileNode compileNode) {
        lastNode = super.visitCompile(compileNode);

        final Consumer<DataSetRow> consumer = toConsumer(compileNode);
        final RuntimeNode nextNode = lastNode;
        return new RuntimeNode() {
            @Override
            public void receive(DataSetRow row, RowMetadata metadata) {
                consumer.accept(row);
                if (nextNode != null) {
                    nextNode.receive(row, metadata);
                }
            }

            @Override
            public void receive(DataSetRow[] rows, RowMetadata[] metadatas) {
                for (int i = 0; i < rows.length; i++) {
                    receive(rows[i], metadatas[i]);
                }
            }

            @Override
            public void signal(Signal signal) {
                lastNode.signal(signal);
            }
        };
    }

    @Override
    public Runnable toRunnable() {
        return () -> stream.forEach(dataSetRow -> {
            // Iterate over records
            lastNode.receive(dataSetRow, dataSetRow.getRowMetadata());
        });
    }

    @Override
    public RuntimeNode visitSource(SourceNode sourceNode) {
        try {
            final URL url = new URL(sourceNode.getSourceUrl());
            final ObjectMapper mapper = new ObjectMapper();
            final JsonParser parser = mapper.getFactory().createParser(url.openStream());
            final DataSet dataSet = mapper.reader(DataSet.class).readValue(parser);

            stream = dataSet.getRecords();
            return super.visitSource(sourceNode);
        } catch (IOException e) {
            throw new TalendRuntimeException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public RuntimeNode visitCleanUp(CleanUpNode cleanUpNode) {
        lastNode = super.visitCleanUp(cleanUpNode);

        final RuntimeNode nextNode = lastNode;
        return new RuntimeNode() {
            @Override
            public void receive(DataSetRow row, RowMetadata metadata) {
                lastNode.receive(row, metadata);
            }

            @Override
            public void receive(DataSetRow[] rows, RowMetadata[] metadatas) {
                if (nextNode == null) {
                    return;
                }

                for (int i = 0; i < rows.length; i++) {
                    nextNode.receive(rows[i], metadatas[i]);
                }
            }

            @Override
            public void signal(Signal signal) {
                cleanUpNode.getContext().cleanup();
            }
        };
    }

    @Override
    public RuntimeNode visitFilterNode(FilterNode filterNode) {
        lastNode = super.visitFilterNode(filterNode);

        return new RuntimeNode() {
            @Override
            public void receive(DataSetRow row, RowMetadata metadata) {
                lastNode.receive(row, metadata);
            }

            @Override
            public void receive(DataSetRow[] rows, RowMetadata[] metadatas) {
                for (int i = 0; i < rows.length; i++) {
                    lastNode.receive(rows[i], metadatas[i]);
                }
            }

            @Override
            public void signal(Signal signal) {
                lastNode.signal(signal);
            }
        };
    }

    @Override
    public RuntimeNode visitLocalSource(LocalSourceNode localSourceNode) {
        stream = localSourceNode.getSource();
        lastNode = super.visitLocalSource(localSourceNode);
        return lastNode;
    }
}
