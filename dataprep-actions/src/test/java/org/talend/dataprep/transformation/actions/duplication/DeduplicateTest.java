// ============================================================================
//
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
package org.talend.dataprep.transformation.actions.duplication;

import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

/**
 * Unit test for the Deduplicate class.
 *
 * @see Deduplicate
 */
public class DeduplicateTest extends AbstractMetadataBaseTest {

    /**
     * The action to test.
     */
    private Deduplicate action = new Deduplicate();

    private Map<String, String> parameters;

    final private DecimalFormat format = new DecimalFormat("0000");

    private void initParameters() {
        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "dataset");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
    }

    private DataSetRow getDataSetRow(String... values) {
        Map<String, String> rowContent = new HashMap<>();
        for (int j = 0; j < values.length; j++) {
            rowContent.put(format.format(j), values[j]);
        }
        return new DataSetRow(rowContent);
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }

    @Test
    public void should_not_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.ANY)));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DEDUPLICATION.getDisplayName()));
    }

    @Test
    public void should_deduplicate() {

        // row 1
        final DataSetRow row1 = getDataSetRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getDataSetRow("David", "Bowie");

        // row 3
        final DataSetRow row3 = getDataSetRow("Toto", "Cafe");

        initParameters();

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(true));
        assertThat(row3.isDeleted(), is(false));
    }



    @Test
    public void deduplicate_with_empty_string() {

        // row 1
        final DataSetRow row1 = getDataSetRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getDataSetRow("DavidBowie", "");

        initParameters();

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
    }

    @Test
    public void deduplicate_with_uppercase() {

        // row 1
        final DataSetRow row1 = getDataSetRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getDataSetRow("DAvid", "Bowie");

        initParameters();

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
    }

    @Test
    public void deduplicate_with_num() {

        // row 1
        final DataSetRow row1 = getDataSetRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getDataSetRow("1David", "Bowie");

        initParameters();

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
    }

    @Test
    public void deduplicate_with_accentued_carac() {

        // row 1
        final DataSetRow row1 = getDataSetRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getDataSetRow("Dàvid", "Bowie");

        initParameters();

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
    }

    @Test
    public void deduplicate_with_date() {

        // row 1
        final DataSetRow row1 = getDataSetRow("05/10/2017", "06/10/2017");

        // row 2
        final DataSetRow row2 = getDataSetRow("05/10/2017", "07/10/2017");

        initParameters();

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
    }

    @Test
    public void deduplicate_with_ponct() {

        // row 1
        final DataSetRow row1 = getDataSetRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getDataSetRow("David.", "Bowie");

        initParameters();

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
    }

    @Test
    public void deduplicate_with_mutiple_deletes() {

        // row 1
        final DataSetRow row1 = getDataSetRow("David", "Bowie");

        // row 2
        final DataSetRow row2 = getDataSetRow("Toto", "Cafe");

        // row 3
        final DataSetRow row3 = getDataSetRow("Toto", "Cafe");

        // row 4
        final DataSetRow row4 = getDataSetRow("David", "Bowie");

        // row 5
        final DataSetRow row5 = getDataSetRow("Bowie", "David");

        initParameters();

        // when
        ActionTestWorkbench.test(Arrays.asList(row1, row2, row3, row4, row5), actionRegistry, factory.create(action, parameters));

        // then
        assertThat(row1.isDeleted(), is(false));
        assertThat(row2.isDeleted(), is(false));
        assertThat(row3.isDeleted(), is(true));
        assertThat(row4.isDeleted(), is(true));
        assertThat(row5.isDeleted(), is(false));
    }

}
