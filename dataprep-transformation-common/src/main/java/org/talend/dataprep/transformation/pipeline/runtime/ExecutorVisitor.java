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

package org.talend.dataprep.transformation.pipeline.runtime;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.DataSetRowAction;
import org.talend.dataprep.transformation.actions.context.ActionContext;
import org.talend.dataprep.transformation.actions.context.TransformationContext;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;
import org.talend.dataprep.transformation.pipeline.node.FilterNode;
import org.talend.dataprep.transformation.pipeline.node.StepNode;

public abstract class ExecutorVisitor<T> extends Visitor<T> {

    protected TransformationContext context = new TransformationContext();

    @Nonnull Predicate<DataSetRow> handleFilterNode(FilterNode filterNode) {
        return row -> {
            for (BiPredicate<DataSetRow, RowMetadata> filter : filterNode.getFilters()) {
                if (!filter.test(row, row.getRowMetadata())) {
                    return false;
                }
            }
            return true;
        };
    }

    @Nonnull Consumer<DataSetRow> toConsumer(CompileNode compileNode) {
        final AtomicInteger previousHashCode = new AtomicInteger(0);
        return row -> {
            final DataSetRowAction rowAction = compileNode.getAction().getRowAction();
            final ActionContext actionContext = context.in(rowAction);
            boolean needCompile = actionContext.getActionStatus() == ActionContext.ActionStatus.NOT_EXECUTED;
            final RowMetadata rowMetadata = row.getRowMetadata();
            if (actionContext.getRowMetadata() == null || !previousHashCode.compareAndSet(previousHashCode.get(), rowMetadata.hashCode())) {
                actionContext.setRowMetadata(rowMetadata.clone());
                needCompile = true; // Metadata changed, force re-compile
            }
            if (needCompile) {
                rowAction.compile(actionContext);
            }
            row.setRowMetadata(actionContext.getRowMetadata());
        };
    }

    @Nonnull Function<DataSetRow, DataSetRow> toFunction(ActionNode actionNode) {
        return row -> {
            final DataSetRowAction rowAction = actionNode.getAction().getRowAction();
            final ActionContext actionContext = context.in(rowAction);
            final DataSetRow actionRow;
            switch (actionContext.getActionStatus()) {
                case NOT_EXECUTED:
                case OK:
                    actionRow = rowAction.apply(row, actionContext);
                    break;
                case DONE:
                case CANCELED:
                default:
                    actionRow = row;
                    break;
            }
            row.setRowMetadata(actionContext.getRowMetadata());
            return actionRow;
        };
    }

    @Nonnull Function<DataSetRow, DataSetRow> handleStepNode(StepNode stepNode) {
        return row -> {
            final RowMetadata stepRowMetadata = stepNode.getStepRowMetadata();
            if (stepRowMetadata != null) {
                row.setRowMetadata(stepRowMetadata);
            }
            return row;
        };
    }

    @Override
    public T visitAction(ActionNode actionNode) {
        context.create(actionNode.getAction().getRowAction(), new RowMetadata());
        return super.visitAction(actionNode);
    }

    @Override
    public T visitCompile(CompileNode compileNode) {
        context.create(compileNode.getAction().getRowAction(), new RowMetadata());
        return super.visitCompile(compileNode);
    }

    @Override
    public T visitStep(StepNode stepNode) {
        super.visitStep(stepNode);
        return stepNode.getEntryNode().accept(this);
    }

    public abstract Runnable toRunnable();
}
