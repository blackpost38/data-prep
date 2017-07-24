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

import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.pipeline.Signal.END_OF_STREAM;
import static org.talend.dataprep.transformation.pipeline.node.NodeUtils.receive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.DataSetRowAction;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.actions.context.ActionContext;
import org.talend.dataprep.transformation.actions.context.TransformationContext;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.TestNode;
import org.talend.dataprep.transformation.pipeline.builder.NodeBuilder;
import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.runtime.RuntimeNode;
import org.talend.dataprep.transformation.pipeline.runtime.RuntimeNodeVisitor;
import org.talend.dataprep.transformation.pipeline.util.NodeClassVisitor;

public class PipelineTest {

    private TestNode output;

    @Before
    public void setUp() throws Exception {
        output = new TestNode();
    }

    @Test
    public void testCompileAction() throws Exception {
        // Given
        final RunnableAction mockAction = new RunnableAction() {

            @Override
            public DataSetRowAction getRowAction() {
                return new DataSetRowAction() {

                    @Override
                    public void compile(ActionContext actionContext) {
                        actionContext.get("ExecutedCompile", p -> true);
                    }

                    @Override
                    public DataSetRow apply(DataSetRow dataSetRow, ActionContext context) {
                        return dataSetRow;
                    }
                };
            }
        };
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row = new DataSetRow(rowMetadata);
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final Node node = NodeBuilder.source(Stream.of(row)).to(new CompileNode(mockAction, actionContext)).to(output).build();

        // when
        assertFalse(actionContext.has("ExecutedCompile"));
        receive(node, row);

        // then
        assertTrue(actionContext.has("ExecutedCompile"));
        assertTrue(actionContext.get("ExecutedCompile"));
    }

    @Test
    public void testActionNode() throws Exception {
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final RunnableAction mockAction = new RunnableAction();
        ActionNode compileNode = new ActionNode(mockAction, actionContext);

        assertEquals(actionContext, compileNode.getActionContext());
        assertEquals(mockAction, compileNode.getAction());
    }

    @Test
    public void testCompileNode() throws Exception {
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final RunnableAction mockAction = new RunnableAction();
        CompileNode compileNode = new CompileNode(mockAction, actionContext);

        assertEquals(actionContext, compileNode.getActionContext());
        assertEquals(mockAction, compileNode.getAction());
    }

    @Test
    public void testRecompileAction() throws Exception {
        // Given
        AtomicInteger compileCount = new AtomicInteger();
        final RunnableAction mockAction = new RunnableAction() {

            @Override
            public DataSetRowAction getRowAction() {
                return new DataSetRowAction() {

                    @Override
                    public void compile(ActionContext actionContext) {
                        compileCount.incrementAndGet();
                        actionContext.setActionStatus(ActionContext.ActionStatus.OK);
                    }

                    @Override
                    public DataSetRow apply(DataSetRow dataSetRow, ActionContext context) {
                        return dataSetRow;
                    }
                };
            }
        };
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row = new DataSetRow(rowMetadata);
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final Node node = NodeBuilder.source(Stream.of(row)).to(new CompileNode(mockAction, actionContext)).to(output).build();

        // when
        assertEquals(0, compileCount.get());
        receive(node, row);
        rowMetadata.addColumn(new ColumnMetadata()); // Change row metadata in middle of the transformation (to trigger
                                                     // new compile).
        receive(node, row);

        // then
        assertEquals(2, compileCount.get());
    }

    @Test
    public void testAction() throws Exception {
        // Given
        final RunnableAction mockAction = new RunnableAction() {

            @Override
            public DataSetRowAction getRowAction() {
                return (r, context) -> {
                    context.get("ExecutedApply", p -> true);
                    return r;
                };
            }
        };
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row = new DataSetRow(rowMetadata);
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final Node node = NodeBuilder.source(Stream.of(row)).to(new ActionNode(mockAction, actionContext)).to(output).build();

        // when
        assertFalse(actionContext.has("ExecutedApply"));
        receive(node, row);

        // then
        assertTrue(actionContext.has("ExecutedApply"));
        assertTrue(actionContext.get("ExecutedApply"));
    }

    @Test
    public void testCanceledAction() throws Exception {
        // Given
        final RunnableAction mockAction = new RunnableAction() {

            @Override
            public DataSetRowAction getRowAction() {
                return (r, context) -> {
                    context.get("Executed", p -> true);
                    return r;
                };
            }
        };
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row = new DataSetRow(rowMetadata);
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
        final Node node = NodeBuilder.source(Stream.of(row)).to(new ActionNode(mockAction, actionContext)).to(output).build();

        // when
        receive(node, row);

        // then
        assertFalse(actionContext.has("Executed"));
    }

    @Test
    public void testCloneLink() throws Exception {
        // Given
        final TestNode output2 = new TestNode();
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row1 = new DataSetRow(rowMetadata);
        final DataSetRow row2 = row1.clone();
        row1.setTdpId(1L);
        row2.setTdpId(2L);
        final Node node = NodeBuilder.source(of(row1, row2)).dispatchTo(output, output2).build();

        // when
        receive(node, row1, row2);

        // then
        assertEquals(2, output.getReceivedRows().size());
        assertEquals(2, output2.getReceivedRows().size());
        assertEquals(row2, output.getReceivedRows().get(1));
        assertEquals(row2, output2.getReceivedRows().get(1));
        assertEquals(rowMetadata, output.getReceivedMetadata().get(0));
        assertEquals(rowMetadata, output2.getReceivedMetadata().get(0));
        assertEquals(END_OF_STREAM, output.getReceivedSignals().get(0));
        assertEquals(END_OF_STREAM, output2.getReceivedSignals().get(0));
    }

    @Test
    public void testSourceNode() throws Exception {
        // Given
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row1 = new DataSetRow(rowMetadata);
        final DataSetRow row2 = row1.clone();
        row1.setTdpId(1L);
        row2.setTdpId(2L);
        final Node node = NodeBuilder.source(of(row1, row2)).to(output).build();

        // when
        receive(node, row1, row2);

        // then
        assertEquals(2, output.getReceivedRows().size());
        assertEquals(row2, output.getReceivedRows().get(1));
        assertEquals(rowMetadata, output.getReceivedMetadata().get(0));
    }

    @Test
    public void testFilteredSourceNode() throws Exception {
        // Given
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row1 = new DataSetRow(rowMetadata);
        final DataSetRow row2 = row1.clone();
        row1.setTdpId(1L);
        row2.setTdpId(2L);
        final Node node = NodeBuilder.filteredSource(r -> r.getTdpId() == 2, of(row1, row2)).to(output).build();

        // when
        receive(node, row1, row2);

        // then
        assertEquals(1, output.getReceivedRows().size());
        assertEquals(row2, output.getReceivedRows().get(0));
        assertEquals(rowMetadata, output.getReceivedMetadata().get(0));
    }

    @Test
    public void testPipeline() throws Exception {
        // given
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row1 = new DataSetRow(rowMetadata);
        final DataSetRow row2 = new DataSetRow(rowMetadata);
        final List<DataSetRow> records = new ArrayList<>();
        records.add(row1);
        records.add(row2);
        final Pipeline pipeline = new Pipeline(NodeBuilder.source(of(row1, row2)).to(output).build());

        final DataSet dataSet = new DataSet();
        final DataSetMetadata metadata = new DataSetMetadata();
        metadata.setRowMetadata(rowMetadata);
        dataSet.setMetadata(metadata);
        dataSet.setRecords(records.stream());

        // when
        NodeUtils.receive(pipeline, row1, row2);

        // then
        assertThat(output.getReceivedMetadata().size(), CoreMatchers.is(2));
        assertThat(output.getReceivedRows().get(1), CoreMatchers.is(row2));
        assertThat(output.getReceivedMetadata().get(1), CoreMatchers.is(rowMetadata));
        assertThat(output.getReceivedSignals().get(0), is(END_OF_STREAM));
    }

    @Test
    public void testCancelledPipeline() throws Exception {
        // given
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row1 = new DataSetRow(rowMetadata);
        final DataSetRow row2 = new DataSetRow(rowMetadata);
        final List<DataSetRow> records = new ArrayList<>();
        records.add(row1);
        records.add(row2);
        final Pipeline pipeline = new Pipeline(NodeBuilder.source(of(row1, row2)).to(output).build());

        final DataSet dataSet = new DataSet();
        final DataSetMetadata metadata = new DataSetMetadata();
        metadata.setRowMetadata(rowMetadata);
        dataSet.setMetadata(metadata);
        dataSet.setRecords(records.stream());

        // when
        final RuntimeNode runtimeNode = pipeline.accept(new RuntimeNodeVisitor());
        runtimeNode.signal(Signal.STOP);
        NodeUtils.receive(pipeline, row1, row2);

        // then
        assertThat(output.getReceivedRows().size(), CoreMatchers.is(1));
        assertThat(output.getReceivedRows().get(0), CoreMatchers.is(row1));
        assertThat(output.getReceivedMetadata().get(0), CoreMatchers.is(rowMetadata));
        assertThat(output.getReceivedSignals().get(0), is(END_OF_STREAM));
    }

    @Test
    public void testSignals() throws Exception {
        // Given
        final TestNode output = new TestNode();
        final Node node = NodeBuilder.source(empty()).to(output).build();

        for (final Signal signal : Signal.values()) {
            // when
            node.accept(new RuntimeNodeVisitor()).signal(signal);

            // then
            assertEquals(signal, output.getReceivedSignals().get(0));
        }
    }

    @Test
    public void testVisitorAndToString() throws Exception {
        final Node node = NodeBuilder.source(empty()) //
                .to(new BasicNode()) //
                .dispatchTo(new BasicNode()) //
                .to(new ActionNode(new RunnableAction(), new ActionContext(new TransformationContext()))) //
                .to(output) //
                .build();
        final Pipeline pipeline = new Pipeline(node);
        final NodeClassVisitor visitor = new NodeClassVisitor();

        // when
        pipeline.accept(visitor);

        // then
        final Class[] expectedClasses = { Pipeline.class, SourceNode.class, BasicLink.class, BasicNode.class, CloneLink.class,
                ActionNode.class };
        assertThat(visitor.getTraversedClasses(), CoreMatchers.hasItems(expectedClasses));
        assertNotNull(pipeline.toString());
    }

    @Test
    public void testCleanUp() throws Exception {
        // Given
        final TransformationContext transformationContext = new TransformationContext();
        final ActionContext actionContext = transformationContext.create((r, ac) -> r, new RowMetadata());
        final AtomicInteger wasDestroyed = new AtomicInteger(0);

        Destroyable destroyable = new Destroyable() {
            @Override
            public void destroy() {
                wasDestroyed.incrementAndGet();
            }
        };
        actionContext.get("test1", p -> destroyable);
        actionContext.get("test2", p -> destroyable);
        final Node node = NodeBuilder.source(empty()) //
                .to(new BasicNode()) //
                .to(new CleanUpNode(transformationContext)) //
                .to(output) //
                .build();

        // when
        node.accept(new RuntimeNodeVisitor()).signal(END_OF_STREAM);

        // then
        assertEquals(2, wasDestroyed.get());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMultiRowAsInput() throws Exception {
        // given
        final Pipeline pipeline = new Pipeline(NodeBuilder.source(empty()).to(output).build());

        final DataSetRow row1 = new DataSetRow(new HashMap<>());
        final DataSetRow row2 = new DataSetRow(new HashMap<>());
        final RowMetadata metadata1 = new RowMetadata(new ArrayList<>());
        final RowMetadata metadata2 = new RowMetadata(new ArrayList<>());

        final DataSetRow[] rows = new DataSetRow[] { row1, row2 };
        final RowMetadata[] metadatas = new RowMetadata[] { metadata1, metadata2 };

        // when
        pipeline.accept(new RuntimeNodeVisitor()).receive(rows, metadatas);

        // then : should throw UnsupportedOperationException
    }

    // Equivalent for a DisposableBean (has a public destroy() method).
    public interface Destroyable { //NOSONAR

        void destroy();
    }

}
