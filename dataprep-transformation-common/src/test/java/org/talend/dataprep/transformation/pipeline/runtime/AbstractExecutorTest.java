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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Test;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.DataSetRowAction;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.actions.context.ActionContext;
import org.talend.dataprep.transformation.actions.context.TransformationContext;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.builder.NodeBuilder;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;

public abstract class AbstractExecutorTest {

    @Test
    public void shouldExecuteAction() throws Exception {
        // Given
        final DataSetRow dataSetRow = new DataSetRow(Collections.singletonMap("1", "Value"));
        final ActionNode actionNode = new ActionNode(new RunnableAction((r, c) -> {
            // Beam tests can't use atomic boolean (apply makes a copy of function)
            System.setProperty("action.execute", "true");
            return r;
        }), new ActionContext(new TransformationContext()));

        final Node model = NodeBuilder.source(Stream.of(dataSetRow)).to(actionNode).build();
        final ExecutorVisitor visitor = getExecutor();
        model.accept(visitor);

        // When
        visitor.toRunnable().run();

        // Then
        assertFalse(Boolean.getBoolean("action.compile"));
        assertTrue(Boolean.getBoolean("action.execute"));
    }

    @Test
    public void shouldExecuteCompile() throws Exception {
        // Given
        final DataSetRow dataSetRow = new DataSetRow(Collections.singletonMap("1", "Value"));
        final RunnableAction action = new RunnableAction(new DataSetRowAction() {
            @Override
            public DataSetRow apply(DataSetRow dataSetRow, ActionContext actionContext) {
                return dataSetRow;
            }

            @Override
            public void compile(ActionContext actionContext) {
                // Beam tests can't use atomic boolean (apply makes a copy of function)
                System.setProperty("action.compile", "true");
            }
        });
        final CompileNode compileNode = new CompileNode(action, new ActionContext(new TransformationContext()));

        final Node model = NodeBuilder.source(Stream.of(dataSetRow)).to(compileNode).build();
        final ExecutorVisitor visitor = getExecutor();
        model.accept(visitor);

        // When
        visitor.toRunnable().run();

        // Then
        assertTrue(Boolean.getBoolean("action.compile"));
        assertFalse(Boolean.getBoolean("action.execute"));
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("action.compile", "false");
        System.setProperty("action.execute", "false");
    }

    protected abstract ExecutorVisitor getExecutor();
}
