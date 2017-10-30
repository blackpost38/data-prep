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

import static org.talend.dataprep.transformation.actions.math.Logarithm.LOGARITHM_NAME;

import java.util.Map;

import org.apache.commons.math3.util.FastMath;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Create a new column with Logarithm
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + LOGARITHM_NAME)
public class Logarithm extends AbstractMathNoParameterAction {

    protected static final String LOGARITHM_NAME = "logarithm_numbers";

    @Override
    protected String calculateResult(String columnValue, ActionContext context) {
        double value = BigDecimalParser.toBigDecimal(columnValue).doubleValue();

        double result = FastMath.log10(value);

        return Double.isNaN(result) ? ERROR_RESULT : Double.toString(result);
    }

    @Override
    public String getColumnNameSuffix(ActionContext context) {
        return "logarithm";
    }

    @Override
    public String getName() {
        return LOGARITHM_NAME;
    }

}
