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
package org.talend.dataprep.transformation.actions.math;

import static org.talend.daikon.number.BigDecimalParser.toBigDecimal;
import static org.talend.dataprep.transformation.actions.common.OtherColumnParameters.*;
import static org.talend.dataprep.transformation.actions.math.Negate.NEGATE_NAME;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Create a new column with negate value
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + NEGATE_NAME)
public class Negate extends AbstractMathNoParameterAction {

    protected static final String NEGATE_NAME = "negate_numbers";

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters = super.getParameters();

        parameters.add(new Parameter("create_new_column", ParameterType.BOOLEAN, "" + getCreateNewColumnDefaultValue()));

        return parameters;
    }

    public boolean getCreateNewColumnDefaultValue() {
        return false;
    }

    @Override
    protected String calculateResult(String columnValue, ActionContext context) {
        return Double.toString(-toBigDecimal(columnValue).doubleValue());
    }

    @Override
    protected String getColumnNameSuffix(Map<String, String> parameters) {
        return "negate";
    }

    @Override
    public String getName() {
        return NEGATE_NAME;
    }

}
