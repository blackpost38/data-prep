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

import java.io.Serializable;

import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.row.DataSetRow;
import org.talend.dataprep.transformation.pipeline.Signal;

/**
 * Links together {@link RuntimeNode nodes}. Each implementation is responsible for handling correct
 * {@link #emit(DataSetRow, RowMetadata)} and {@link #signal(Signal)} methods.
 */
public interface RuntimeLink extends Serializable {

    /**
     * Emits a new row and its corresponding metadata.
     *
     * @param row A {@link DataSetRow row} to emit to the next {@link RuntimeNode}.
     * @param metadata The {@link RowMetadata row metadata} to be used by the next {@link RuntimeNode}.
     */
    void emit(DataSetRow row, RowMetadata metadata);

    /**
     * Emits a new row and its corresponding metadata.
     *
     * @param rows An array of {@link DataSetRow row} to emit to the next {@link RuntimeNode}.
     * @param metadatas The array of {@link RowMetadata row metadata} to be used by the next {@link RuntimeNode}.
     */
    void emit(DataSetRow[] rows, RowMetadata[] metadatas);

    /**
     * Sends a {@link Signal event} to the {@link RuntimeNode}. Signals are data-independent events to indicate external
     * events (such as end of the stream).
     *
     * @param signal A {@link Signal signal} to be sent to the pipeline.
     */
    void signal(Signal signal);

}
