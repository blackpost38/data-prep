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

import static org.apache.commons.lang.BooleanUtils.toStringTrueFalse;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.api.type.Type.BOOLEAN;
import static org.talend.dataprep.parameters.ParameterType.INTEGER;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.ParameterType;
import org.talend.dataprep.parameters.SelectParameter;
import org.talend.dataprep.transformation.actions.category.ActionCategory;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.common.OtherColumnParameters;
import org.talend.dataprep.transformation.api.action.context.ActionContext;

/**
 * Create a new column with Boolean result <code>true</code> if the Levenstein distance is less or equals the parameter
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + FuzzyMatching.ACTION_NAME)
public class FuzzyMatching extends AbstractActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "fuzzy_matching";

    public static final String VALUE_PARAMETER = "reference_value";

    public static final String SENSITIVITY = "sensitivity";

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_matches";

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public List<Parameter> getParameters(Locale locale) {
        final List<Parameter> parameters = super.getParameters(locale);

        parameters.add(SelectParameter.Builder.builder() //
                .name(OtherColumnParameters.MODE_PARAMETER) //
                .item(OtherColumnParameters.CONSTANT_MODE, OtherColumnParameters.CONSTANT_MODE,//
                        new Parameter.ParameterBuilder().setName(VALUE_PARAMETER)
                                .setType(ParameterType.STRING)
                                .setDefaultValue(EMPTY)
                                .createParameter(this, locale)) //
                .item(OtherColumnParameters.OTHER_COLUMN_MODE, OtherColumnParameters.OTHER_COLUMN_MODE,//
                        new Parameter.ParameterBuilder().setName(OtherColumnParameters.SELECTED_COLUMN_PARAMETER)
                                .setType(ParameterType.COLUMN)
                                .setDefaultValue(StringUtils.EMPTY)
                                .setCanBeBlank(false)
                                .createParameter(this, locale)) //
                .defaultValue(OtherColumnParameters.CONSTANT_MODE).build(this));

        parameters.add(new Parameter.ParameterBuilder().setName(SENSITIVITY)
                .setType(INTEGER)
                .setDefaultValue("1")
                .setCanBeBlank(false)
                .createParameter(this, locale));
        return parameters;
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            final String columnId = context.getColumnId();
            // create new column and append it after current column
            RowMetadata rowMetadata = context.getRowMetadata();
            ColumnMetadata column = rowMetadata.getById(columnId);

            context.column(column.getName() + APPENDIX, r -> {
                final ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .name(column.getName() + APPENDIX) //
                        .type(BOOLEAN) //
                        .typeForce(true) //
                        .empty(column.getQuality().getEmpty()) //
                        .invalid(column.getQuality().getInvalid()) //
                        .valid(column.getQuality().getValid()) //
                        .headerSize(column.getHeaderSize()) //
                        .build();
                rowMetadata.insertAfter(columnId, c);
                return c;
            });
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        Map<String, String> parameters = context.getParameters();

        int sensitivity = NumberUtils.toInt(parameters.get(SENSITIVITY));

        // create new column and append it after current column
        RowMetadata rowMetadata = context.getRowMetadata();
        ColumnMetadata column = rowMetadata.getById(columnId);

        final String fuzzyMatches = context.column(column.getName() + APPENDIX);

        String value = row.get(context.getColumnId());
        String referenceValue;
        if (parameters.get(OtherColumnParameters.MODE_PARAMETER).equals(OtherColumnParameters.CONSTANT_MODE)) {
            referenceValue = parameters.get(VALUE_PARAMETER);
        } else {
            final ColumnMetadata selectedColumn = rowMetadata
                    .getById(parameters.get(OtherColumnParameters.SELECTED_COLUMN_PARAMETER));
            referenceValue = row.get(selectedColumn.getId());
        }

        final String columnValue = toStringTrueFalse(fuzzyMatches(value, referenceValue, sensitivity));
        row.set(fuzzyMatches, columnValue);
    }

    private boolean fuzzyMatches(String value, String reference, int sensitivity) {
        int levenshteinDistance = StringUtils.getLevenshteinDistance(value, reference);
        return levenshteinDistance <= sensitivity;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }

}
