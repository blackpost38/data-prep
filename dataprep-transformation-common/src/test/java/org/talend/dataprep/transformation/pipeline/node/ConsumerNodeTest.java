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

import java.util.HashMap;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.TestLink;
import org.talend.dataprep.transformation.pipeline.runtime.RuntimeNodeVisitor;

public class ConsumerNodeTest {
    @Test
    public void receive_should_perform_consumers_and_emit_row() {
        // given
        final RowMetadata metadata1 = new RowMetadata();
        final RowMetadata metadata2 = new RowMetadata();

        final DataSetRow row1 = new DataSetRow(new HashMap<>());
        final DataSetRow row2 = new DataSetRow(new HashMap<>());

        final int rowModified = 0;
        final int metadataModified = 1;
        final int signalModified = 2;
        final boolean[] modifications = new boolean[3];

        final TestLink link = new TestLink(new BasicNode());
        final ConsumerNode node = new ConsumerNode(
                (row) -> modifications[rowModified] = true,
                (metadata) -> modifications[metadataModified] = true,
                (signal) -> modifications[signalModified] = true
        );
        node.setLink(link);

        // when
        node.accept(new RuntimeNodeVisitor()).receive(row1, metadata1);

        // then
        assertThat(link.getEmittedRows(), IsCollectionWithSize.hasSize(1));
        assertThat(link.getEmittedRows(), CoreMatchers.hasItems(row2));
        MatcherAssert.assertThat(modifications[rowModified], CoreMatchers.is(true));

        assertThat(link.getEmittedMetadata(), IsCollectionWithSize.hasSize(1));
        assertThat(link.getEmittedMetadata(), CoreMatchers.hasItems(metadata2));
        MatcherAssert.assertThat(modifications[metadataModified], CoreMatchers.is(true));

        MatcherAssert.assertThat(modifications[signalModified], CoreMatchers.is(false));
    }

    @Test
    public void receive_should_perform_consumer_and_emit_signal() {
        // given
        final int rowModified = 0;
        final int metadataModified = 1;
        final int signalModified = 2;
        final boolean[] modifications = new boolean[3];

        final TestLink link = new TestLink(new BasicNode());
        final ConsumerNode node = new ConsumerNode(
                (row) -> modifications[rowModified] = true,
                (metadata) -> modifications[metadataModified] = true,
                (signal) -> modifications[signalModified] = true
        );
        node.setLink(link);

        // when
        node.accept(new RuntimeNodeVisitor()).signal(Signal.END_OF_STREAM);

        // then
        MatcherAssert.assertThat(modifications[rowModified], CoreMatchers.is(false));
        MatcherAssert.assertThat(modifications[metadataModified], CoreMatchers.is(false));

        assertThat(link.getEmittedSignals(), IsCollectionWithSize.hasSize(1));
        assertThat(link.getEmittedSignals(), CoreMatchers.hasItems(Signal.END_OF_STREAM));

    }
}
