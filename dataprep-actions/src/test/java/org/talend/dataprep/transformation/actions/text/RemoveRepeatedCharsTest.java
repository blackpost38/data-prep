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

package org.talend.dataprep.transformation.actions.text;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.actions.ActionMetadataTestUtils.getColumn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.dataprep.api.action.ActionDefinition;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;

/**
 * Test class for RemoveRepeatedChars action. Creates one consumer, and test it.
 *
 * @see RemoveRepeatedChars
 */
public class RemoveRepeatedCharsTest extends AbstractMetadataBaseTest {

    /** The action for removing consecutive. */
    private RemoveRepeatedChars action = new RemoveRepeatedChars();

    private Map<String, String> parameters;

    /**
     * initialize parameters for Whitespace.
     */
    private void initParametersWhitespace() {
        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, "whitespace");
    }

    /**
     * initialize  parameters for custom repeated char
     * @param repStr
     */
    private void initParameterCustom(String repStr) {
        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, RemoveRepeatedChars.CUSTOM);
        parameters.put(RemoveRepeatedChars.CUSTOM_REPEAT_CHAR_PARAMETER, repStr);
    }

    @Test
    public void test_action_name() throws Exception {
        assertEquals("remove_repeated_chars", action.getName());
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS.getDisplayName()));
    }

    @Test
    public void testGetParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters(ENGLISH);
        assertEquals(5, parameters.size());

        final SelectParameter parameter4 = (SelectParameter) parameters.get(4);
        assertEquals(2,parameter4.getItems().size());
        assertEquals("Whitespace", parameter4.getItems().get(0).getLabel());
        assertEquals("other", parameter4.getItems().get(1).getLabel());
    }

    @Test
    public void should_remove_repeated_whiteSpace() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "ab   c  d");
        final DataSetRow row = new DataSetRow(values);
        initParametersWhitespace();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("ab c d", row.get("0000"));
    }

    @Test
    public void should_not_remove_null_value() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", null);
        final DataSetRow row = new DataSetRow(values);
        initParametersWhitespace();

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(null, row.get("0000"));
    }

    @Test
    public void should_not_remove_empty_parameter() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "abcc");
        final DataSetRow row = new DataSetRow(values);
        parameters = new HashMap<>();
        parameters.put(ImplicitParameters.SCOPE.getKey().toLowerCase(), "column");
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0000");
        parameters.put(RemoveRepeatedChars.REMOVE_TYPE, "");
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("abcc", row.get("0000"));
    }

    @Test
    public void should_remove_custom_value() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "haand");
        final DataSetRow row = new DataSetRow(values);
        initParameterCustom("a");
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("hand", row.get("0000"));
    }

    @Test
    public void should_not_remove_custom_null() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "haand");
        final DataSetRow row = new DataSetRow(values);
        initParameterCustom(null);
        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals("haand", row.get("0000"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptField(getColumn(Type.STRING)));
        assertTrue(action.acceptField(getColumn(Type.NUMERIC)));
        assertTrue(action.acceptField(getColumn(Type.FLOAT)));
        assertTrue(action.acceptField(getColumn(Type.DATE)));
        assertTrue(action.acceptField(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_have_expected_behavior() {
        assertEquals(1, action.getBehavior().size());
        assertTrue(action.getBehavior().contains(ActionDefinition.Behavior.VALUES_COLUMN));
    }
}
