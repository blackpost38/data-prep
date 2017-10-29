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

package org.talend.dataprep.transformation.actions.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getRow;

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.ActionDefinition;
import org.talend.dataprep.transformation.actions.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Unit test for the Max action.
 *
 * @see Max
 */
public class MaxTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    private Max action = new Max();

    /** The action parameters. */
    private Map<String, String> parameters;

    @Before
    public void setUp() throws Exception {
        final InputStream parametersSource = MaxTest.class.getResourceAsStream("maxAction.json");
        parameters = ActionMetadataTestUtils.parseParameters(parametersSource);
    }

    @Test
    public void max_with_constant() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "7");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("7.0", row.get("0003"));
    }

    @Test
    public void max_with_undefined_constant() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals(StringUtils.EMPTY, row.get("0003"));
    }

    @Test
    public void max_with_NaN_constant() {
        // given
        DataSetRow row = getRow("5", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "beer");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals(StringUtils.EMPTY, row.get("0003"));
    }

    @Test
    public void max_with_other_column() {
        // given
        DataSetRow row = getRow("5", "8", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("8.0", row.get("0003"));
    }

    @Test
    public void max_with_undefined_other_column() {
        // given
        DataSetRow row = getRow("5", "8", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "000xx");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals(StringUtils.EMPTY, row.get("0003"));
    }

    @Test
    public void max_with_NaN_other_column() {
        // given
        DataSetRow row = getRow("5", "8", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0002");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals(StringUtils.EMPTY, row.get("0003"));
    }

    @Test
    public void max_currency_values_with_constant() {
        // given
        DataSetRow row = getRow("$61,667", "3", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.CONSTANT_MODE);
        parameters.put(OtherColumnParameters.CONSTANT_VALUE, "2");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("61667.0", row.get("0003"));
    }

    @Test
    public void max_currency_value_with_other_column() {
        // given
        DataSetRow row = getRow("5", "8$", "Done !");

        parameters.put(OtherColumnParameters.MODE_PARAMETER, OtherColumnParameters.OTHER_COLUMN_MODE);
        parameters.put(OtherColumnParameters.SELECTED_COLUMN_PARAMETER, "0001");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertColumnWithResultCreated(row);
        assertEquals("8.0", row.get("0003"));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.METADATA_CREATE_COLUMNS));
    }

    private void assertColumnWithResultCreated(DataSetRow row) {
        ColumnMetadata expected = ColumnMetadata.Builder.column().id(3).name("0000_max").type(Type.STRING).build();
        ColumnMetadata actual = row.getRowMetadata().getById("0003");
        assertEquals(expected, actual);
    }

}
