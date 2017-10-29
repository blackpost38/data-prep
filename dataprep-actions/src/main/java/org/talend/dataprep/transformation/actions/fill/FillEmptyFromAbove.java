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

package org.talend.dataprep.transformation.actions.fill;

import static org.talend.dataprep.transformation.actions.category.ActionCategory.DATA_CLEANSING;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.common.ColumnAction;
import org.talend.dataprep.transformation.actions.context.ActionContext;

/**
 * TDQ-13265 msjian : Fill empty cell from above.
 */
@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + FillEmptyFromAbove.ACTION_NAME)
public class FillEmptyFromAbove extends AbstractActionMetadata implements ColumnAction {

    /** the action name. */
    public static final String ACTION_NAME = "fill_empty_from_above"; //$NON-NLS-1$

    /** previous row's value store. */
    public static final String PREVIOUS = "previous"; //$NON-NLS-1$

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    @Override
    public boolean acceptField(ColumnMetadata column) {
        return true;
    }

    @Override
    public Set<Behavior> getBehavior() {  return EnumSet.of(Behavior.VALUES_COLUMN, Behavior.FORBID_DISTRIBUTED); }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            actionContext.get(PREVIOUS, values -> new PreviousValueHolder());
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        // the first time applyOnColumn is called, save the current value in PreviousValueHolder
        // and the Optional.ofNullable(...) allows you to NOT modify the first row (add an empty value)
        // then second call of applyOnColumn, PreviousContextHolder has... previous value
        final PreviousValueHolder holder = context.get(PREVIOUS);
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);

        // Empty means null, empty string and any whitespace only strings
        if (StringUtils.isBlank(value)) {
            if (holder.getValue() != null) {
                row.set(columnId, holder.getValue());
            }
        } else {
            holder.setValue(value);
        }
    }

    /** this class is used to store the previous value. */
    private static class PreviousValueHolder {
        String value;
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
}
