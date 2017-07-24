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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Visitor;

public class DiffWriterNode extends BasicNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiffWriterNode.class);

    private final TransformerWriter writer;

    private long totalTime;

    private int count;

    private boolean startRecords;

    private boolean endMetadata;

    private RowMetadata[] lastMetadatas;

    public DiffWriterNode(final TransformerWriter writer) {
        this.writer = writer;
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitNode(this);
    }

    @Override
    public Node copyShallow() {
        return new DiffWriterNode(writer);
    }
}
