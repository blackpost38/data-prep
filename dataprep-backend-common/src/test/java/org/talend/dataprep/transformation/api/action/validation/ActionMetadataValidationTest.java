//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.action.validation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.talend.dataprep.exception.error.CommonErrorCodes.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.pipeline.ActionRegistry;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ActionMetadataValidationTest.class)
@ComponentScan(basePackages = "org.talend.dataprep")
public class ActionMetadataValidationTest {

    @Autowired
    private ActionMetadataValidation validator;

    @Test
    public void checkScopeConsistency_should_pass() throws Exception {
        //given
        final Map<String,String> parameters = new HashMap<>();
        parameters.put("scope", "column");
        parameters.put("column_id", "0001");
        ActionMetadata actionMock = new ActionMetadataExtendingColumn();

        //when
        validator.checkScopeConsistency(actionMock, parameters);

        //then : should not throw exception
    }

    @Test
    public void checkScopeConsistency_should_throw_exception_on_missing_scope() throws Exception {
        //given
        final Map<String,String> parameters = new HashMap<>();
        parameters.put("column_id", "0001");
        ActionMetadata actionMock = new ActionMetadataExtendingColumn();

        //when
        try {
            validator.checkScopeConsistency(actionMock, parameters);
            fail("should have thrown TDP exception because param scope is missing");
        }

        //then
        catch (final TDPException e) {
            assertThat(e.getCode(), is(MISSING_ACTION_SCOPE));
        }
    }

    @Test
    public void checkScopeConsistency_should_throw_exception_on_unsupported_scope() throws Exception {
        //given
        final Map<String,String> parameters = new HashMap<>();
        parameters.put("scope", "line");
        parameters.put("row_id", "0001");
        ActionMetadataExtendingColumn actionMock = new ActionMetadataExtendingColumn();

        //when
        try {
            validator.checkScopeConsistency(actionMock, parameters);
            fail("should have thrown TDP exception because line scope is not supported by cut (for example)");
        }

        //then
        catch (final TDPException e) {
            assertThat(e.getCode(), is(UNSUPPORTED_ACTION_SCOPE));
        }
    }

    @Test
    public void checkScopeConsistency_should_throw_exception_on_missing_scope_mandatory_parameter() throws Exception {
        //given
        final Map<String,String> parameters = new HashMap<>();
        parameters.put("scope", "column");
        parameters.put("row_id", "0001");
        ActionMetadataExtendingColumn actionMock = new ActionMetadataExtendingColumn();

        //when
        try {
            validator.checkScopeConsistency(actionMock, parameters);
            fail("should have thrown TDP exception because cell scope requires column_id (for example)");
        }

        //then
        catch (final TDPException e) {
            assertThat(e.getCode(), is(MISSING_ACTION_SCOPE_PARAMETER));
        }
    }

    private static class ActionMetadataExtendingColumn extends ActionMetadata implements ColumnAction {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getCategory() {
            return null;
        }

        @Override
        public boolean acceptColumn(ColumnMetadata column) {
            return false;
        }

        @Override
        public Set<Behavior> getBehavior() {
            return null;
        }

        @Override
        public void applyOnColumn(DataSetRow row, ActionContext context) {

        }
    }
}