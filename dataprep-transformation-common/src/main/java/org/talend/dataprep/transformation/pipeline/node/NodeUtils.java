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

package org.talend.dataprep.transformation.pipeline.node;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.builder.NodeBuilder;
import org.talend.dataprep.transformation.pipeline.runtime.RuntimeNodeVisitor;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NodeUtils {

    private NodeUtils() {
    }

    public static void receive(Node node, DataSetRow row) {
        receive(node, new DataSetRow[] { row });
    }

    public static void receive(Node node, DataSetRow... rows) {
        File tempFile = null;
        try {
            // Write rows to temp file
            tempFile = File.createTempFile("testInput", ".json");
            final ObjectMapper mapper = new ObjectMapper();
            DataSet dataSet = new DataSet();
            dataSet.setRecords(Stream.of(rows));
            mapper.writerFor(DataSet.class).writeValue(tempFile, dataSet);

            // Source node
            final NodeBuilder builder = NodeBuilder.source(tempFile.toURI().toURL().toString()).to(node);
            final Node pipeline = builder.build();

            // Use JavaExecutor
            final RuntimeNodeVisitor visitor = new RuntimeNodeVisitor();
            pipeline.accept(visitor);

            final Runnable runnable = visitor.toRunnable();
            runnable.run();
        } catch (IOException e) {
            if(tempFile != null) {
                tempFile.delete();
            }
            throw new RuntimeException(e);
        }
    }
}
