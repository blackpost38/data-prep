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

package org.talend.dataprep.transformation.actions.delete;

import org.talend.dataprep.api.action.Action;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.actions.context.ActionContext;

@Action(AbstractActionMetadata.ACTION_BEAN_PREFIX + KeepOnly.KEEP_ONLY_ACTION_NAME)
public class KeepOnly extends AbstractFilteringAction {

    static final String KEEP_ONLY_ACTION_NAME = "keep_only";

    @Override
    public String getName() {
        return KEEP_ONLY_ACTION_NAME;
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        if (!context.getFilter().test(row)) {
            row.setDeleted(true);
        }
    }

}
