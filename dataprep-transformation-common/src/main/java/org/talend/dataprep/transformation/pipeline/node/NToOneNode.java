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

import java.util.function.Function;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Visitor;

/**
 * Node that execute a function.
 * This function takes a multiRowInput and return a single row.
 */
public class NToOneNode extends BasicNode {

    private final Function<DataSetRow[], DataSetRow> rowFunction;

    private final Function<RowMetadata[], RowMetadata> metadataFunction;

    public NToOneNode(final Function<DataSetRow[], DataSetRow> rowFunction, final Function<RowMetadata[], RowMetadata> metadataFunction) {
        this.rowFunction = rowFunction;
        this.metadataFunction = metadataFunction;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitNToOne(this);
    }

    @Override
    public Node copyShallow() {
        return new NToOneNode(rowFunction, metadataFunction);
    }
}
