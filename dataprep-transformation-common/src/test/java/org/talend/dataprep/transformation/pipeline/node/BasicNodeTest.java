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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.talend.dataprep.transformation.pipeline.Signal.CANCEL;
import static org.talend.dataprep.transformation.pipeline.node.NodeUtils.receive;

import java.util.ArrayList;
import java.util.HashMap;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.TestLink;
import org.talend.dataprep.transformation.pipeline.runtime.RuntimeNodeVisitor;

public class BasicNodeTest {
    @Test
    public void should_emit_single_input_row_to_its_link() {
        // given
        final TestLink link = new TestLink(new BasicNode());
        final BasicNode node = new BasicNode();
        node.setLink(link);

        final DataSetRow row = new DataSetRow(new HashMap<>());
        final RowMetadata metadata = new RowMetadata(new ArrayList<>());

        // when
        receive(node, row);

        // then
        assertThat(link.getEmittedRows(), Matchers.hasSize(1));
        assertThat(link.getEmittedRows(), Matchers.contains(row));

        assertThat(link.getEmittedMetadata(), Matchers.hasSize(1));
        assertThat(link.getEmittedMetadata(), Matchers.contains(metadata));
    }

    @Test
    public void should_emit_multi_input_row_to_its_link() {
        // given
        final TestLink link = new TestLink(new BasicNode());
        final BasicNode node = new BasicNode();
        node.setLink(link);

        final DataSetRow row1 = new DataSetRow(new HashMap<>());
        final DataSetRow row2 = new DataSetRow(new HashMap<>());
        final RowMetadata metadata1 = new RowMetadata(new ArrayList<>());
        final RowMetadata metadata2 = new RowMetadata(new ArrayList<>());

        final DataSetRow[] rows = new DataSetRow[] { row1, row2 };
        final RowMetadata[] metadatas = new RowMetadata[] { metadata1, metadata2 };

        // when
        receive(node, rows);

        // then
        assertThat(link.getEmittedRows(), Matchers.hasSize(2));
        assertThat(link.getEmittedRows(), Matchers.contains(rows));

        assertThat(link.getEmittedMetadata(), Matchers.hasSize(2));
        assertThat(link.getEmittedMetadata(), Matchers.contains(metadatas));
    }

    @Test
    public void should_emit_signal_to_all_targets() {
        // given
        final TestLink link = new TestLink(null);
        final BasicNode node = new BasicNode();
        node.setLink(link);

        // when
        node.accept(new RuntimeNodeVisitor()).signal(CANCEL);

        // then
        assertThat(link.getEmittedSignals(), Matchers.hasSize(1));
        assertThat(link.getEmittedSignals(), contains(CANCEL));
    }
}
