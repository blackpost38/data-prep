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

import java.util.function.Predicate;

import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.row.InvalidMarker;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.actions.Providers;
import org.talend.dataprep.transformation.pipeline.Node;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;

public class InvalidDetectionNode extends ColumnFilteredNode {

    private transient InvalidMarker invalidMarker;

    private transient Analyzer<Analyzers.Result> configuredAnalyzer;

    private transient AnalyzerService analyzerService;

    public InvalidDetectionNode(final Predicate<? super ColumnMetadata> filter) {
        super(filter);
    }

    public InvalidMarker getInvalidMarker() {
        if (configuredAnalyzer == null) {
            this.configuredAnalyzer = getAnalyzerService().build(filteredColumns, AnalyzerService.Analysis.QUALITY);
            this.invalidMarker = new InvalidMarker(filteredColumns, configuredAnalyzer);
        }
        return invalidMarker;
    }

    private AnalyzerService getAnalyzerService() {
        if (analyzerService == null) {
            this.analyzerService = Providers.get(AnalyzerService.class);
        }
        return analyzerService;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitNode(this);
    }

    @Override
    public Node copyShallow() {
        return new InvalidDetectionNode(filter);
    }

}
