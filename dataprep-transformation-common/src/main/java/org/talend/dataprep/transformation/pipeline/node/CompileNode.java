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

package org.talend.dataprep.transformation.pipeline.node;

import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.actions.context.ActionContext;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Visitor;

/**
 * A node that compiles (perform metadata changes
 */
public class CompileNode extends BasicNode {

    private final RunnableAction action;

    private final ActionContext actionContext;

    public CompileNode(RunnableAction action, ActionContext actionContext) {
        this.action = action;
        this.actionContext = actionContext;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitCompile(this);
    }

    @Override
    public Node copyShallow() {
        return new CompileNode(action, actionContext);
    }

    public RunnableAction getAction() {
        return action;
    }

    public ActionContext getActionContext() {
        return actionContext;
    }
}
