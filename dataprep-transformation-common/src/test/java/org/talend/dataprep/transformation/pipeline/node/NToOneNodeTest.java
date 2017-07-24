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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.util.HashMap;

import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.TestLink;
import org.talend.dataprep.transformation.pipeline.runtime.RuntimeNodeVisitor;

public class NToOneNodeTest {

    @Test
    public void receive_should_emit_function_result() {
        // given
        final RowMetadata metadata1 = new RowMetadata();
        final RowMetadata metadata2 = new RowMetadata();
        final RowMetadata[] metadatas = new RowMetadata[] { metadata1, metadata2 };

        final DataSetRow row1 = new DataSetRow(new HashMap<>());
        final DataSetRow row2 = new DataSetRow(new HashMap<>());
        final DataSetRow[] rows = new DataSetRow[] { row1, row2 };

        final TestLink link = new TestLink(new BasicNode());
        final NToOneNode node = new NToOneNode((dataSetRows) -> dataSetRows[1], (rowMetadatas) -> rowMetadatas[1]);
        node.setLink(link);

        // when
        node.accept(new RuntimeNodeVisitor()).receive(rows, metadatas);

        // then
        assertThat(link.getEmittedRows(), hasSize(1));
        assertThat(link.getEmittedRows(), hasItems(row2));

        assertThat(link.getEmittedMetadata(), hasSize(1));
        assertThat(link.getEmittedMetadata(), hasItems(metadata2));

    }
}
