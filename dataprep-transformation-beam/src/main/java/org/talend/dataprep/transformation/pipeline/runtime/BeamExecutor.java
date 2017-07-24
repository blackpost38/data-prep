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

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.actions.DataSetRowAction;
import org.talend.dataprep.transformation.actions.common.RunnableAction;
import org.talend.dataprep.transformation.actions.context.ActionContext;
import org.talend.dataprep.transformation.actions.context.TransformationContext;
import org.talend.dataprep.transformation.pipeline.node.ActionNode;
import org.talend.dataprep.transformation.pipeline.node.CompileNode;
import org.talend.dataprep.transformation.pipeline.node.LocalSourceNode;
import org.talend.dataprep.transformation.pipeline.node.SourceNode;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class BeamExecutor extends ExecutorVisitor<PCollection<DataSetRow>> implements Serializable {

    private transient Pipeline root;

    private transient PCollection<DataSetRow> pipeline;

    @Override
    public Runnable toRunnable() {
        return () -> root.run();
    }

    @Override
    public PCollection<DataSetRow> visitSource(SourceNode sourceNode) {
        PipelineOptions options = PipelineOptionsFactory.create();
        this.root = Pipeline.create(options);
        this.pipeline = root.apply(AvroIO.read(DataSetRow.class).from(sourceNode.getSourceUrl()));

        super.visitSource(sourceNode);
        return pipeline;
    }

    @Override
    public PCollection<DataSetRow> visitLocalSource(LocalSourceNode localSourceNode) {
        final List<DataSetRow> rows = localSourceNode.getSource().collect(Collectors.toList());

        final PipelineOptions options = PipelineOptionsFactory.create();
        this.root = Pipeline.create(options);
        this.pipeline = root.apply(Create.of(rows));

        super.visitLocalSource(localSourceNode);
        return pipeline;
    }

    @Override
    public PCollection<DataSetRow> visitAction(ActionNode actionNode) {
        final RunnableAction action = actionNode.getAction();
        pipeline = pipeline.apply("action-" + action.getName(), ParDo.of(new Apply(action)));
        super.visitAction(actionNode);
        return pipeline;
    }

    @Override
    public PCollection<DataSetRow> visitCompile(CompileNode compileNode) {
        final RunnableAction action = compileNode.getAction();
        pipeline = pipeline.apply("compile-" + action.getName(), ParDo.of(new Compile(action)));
        super.visitCompile(compileNode);
        return pipeline;
    }

    private static class Apply extends DoFn<DataSetRow, DataSetRow> {

        private final RunnableAction action;

        private Apply(RunnableAction action) {
            this.action = action;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            DataSetRow row = c.element();
            DataSetRowAction rowAction = action.getRowAction();
            rowAction.apply(row, new ActionContext(new TransformationContext()));

            c.output(row);
        }
    }

    private static class Compile extends DoFn<DataSetRow, DataSetRow> {

        private final RunnableAction action;

        private Compile(RunnableAction action) {
            this.action = action;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            DataSetRow row = c.element();
            DataSetRowAction rowAction = action.getRowAction();
            rowAction.compile(new ActionContext(new TransformationContext()));

            c.output(row);
        }
    }
}
