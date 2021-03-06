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

package org.talend.dataprep.transformation.actions.conversions;

import org.apache.commons.lang3.math.NumberUtils;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.i18n.ActionsBundle;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.math.AbstractMathNoParameterAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.units.TemperatureImpl;

import javax.measure.quantity.Temperature;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import static org.talend.dataprep.parameters.ParameterType.INTEGER;
import static org.talend.dataprep.transformation.actions.conversions.TemperaturesConverter.ACTION_NAME;
import static org.talend.dataprep.transformation.actions.conversions.TemperaturesConverter.TemperatureUnit.*;

/**
 * Abstract class for conversions from Fahrenheit to Celsius and vice versa.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + ACTION_NAME)
public class TemperaturesConverter extends AbstractMathNoParameterAction {

    public static final String ACTION_NAME = "temperatures_converter";

    private static final String FROM_UNIT_PARAMETER = "from_temperature";

    private static final String TO_UNIT_PARAMETER = "to_temperature";

    private static final String TARGET_PRECISION = "precision";

    @Override
    protected String calculateResult(String columnValue, ActionContext context) {
        TemperatureUnit fromTemperatureUnit = TemperatureUnit.valueOf(context.getParameters().get(FROM_UNIT_PARAMETER));
        TemperatureUnit toTemperatureUnit = TemperatureUnit.valueOf(context.getParameters().get(TO_UNIT_PARAMETER));
        String precisionParameter = context.getParameters().get(TARGET_PRECISION);

        BigDecimal value = BigDecimalParser.toBigDecimal(columnValue);
        TemperatureImpl fromTemp = new TemperatureImpl(value, fromTemperatureUnit.asJavaUnit());
        TemperatureImpl temperature = fromTemp.convertTo(toTemperatureUnit.asJavaUnit());

        // Precision is used as scale, for precision as significant digits, see history
        Integer targetScale = NumberUtils.toInt(precisionParameter, value.scale());

        return temperature.getValue().setScale(targetScale, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(SelectParameter.Builder.builder()
                .item(FAHRENHEIT.name(), FAHRENHEIT.toString())
                .item(CELSIUS.name(), CELSIUS.toString())
                .item(KELVIN.name(), KELVIN.toString())
                .canBeBlank(false)
                .defaultValue(FAHRENHEIT.name())
                .name(FROM_UNIT_PARAMETER)
                .build());

        parameters.add(SelectParameter.Builder.builder()
                .item(FAHRENHEIT.name(), FAHRENHEIT.toString())
                .item(CELSIUS.name(), CELSIUS.toString())
                .item(KELVIN.name(), KELVIN.toString())
                .canBeBlank(false)
                .defaultValue(CELSIUS.name())
                .name(TO_UNIT_PARAMETER)
                .build());

        parameters.add(new Parameter(TARGET_PRECISION, INTEGER, null, false, true, "precision"));
        return ActionsBundle.attachToAction(parameters, this);
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.CONVERSIONS.getDisplayName();
    }

    @Override
    protected String getColumnNameSuffix(Map<String, String> parameters) {
        String name = parameters.get(TO_UNIT_PARAMETER);
        TemperatureUnit temperatureUnit = TemperatureUnit.valueOf(name);
        return "in " + temperatureUnit.toString();
    }

    /**
     * Part of the TemperatureConverture API.
     * conversion rates based on https://fr.wikipedia.org/wiki/Degr%C3%A9_Fahrenheit#Conversions_dans_d.27autres_.C3.A9chelles_de_temp.C3.A9rature
     */
    public enum TemperatureUnit {
        FAHRENHEIT("Fahrenheit", NonSI.FAHRENHEIT),
        CELSIUS("Celsius", SI.CELSIUS),
        KELVIN("Kelvin", SI.KELVIN);

        private final String displayName;

        private final Unit<Temperature> javaUnit;

        TemperatureUnit(String displayName, Unit<Temperature> javaUnit) {
            this.displayName = displayName;
            this.javaUnit = javaUnit;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public Unit<Temperature> asJavaUnit() {
            return javaUnit;
        }
    }
}
