// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.qa.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

/**
 * Used to share data within steps.
 */
@Component
public class FeatureContext {

    /**
     * Prefix used to build storage key, for fullrun references.
     */
    public static final String FULL_RUN_PREFIX = "fullrun-";

    private Map<String, String> datasetIdByName = new HashMap<>();

    private Map<String, String> preparationIdByName = new HashMap<>();

    private Map<String, File> tempFileByName = new HashMap<>();

    /**
     * All object store on a feature execution.
     */
    private Map<String, Object> featureContext = new HashMap<>();

    /**
     * Store a new dataset reference. In order to delete it later.
     *
     * @param id the dataset id.
     * @param name the dataset name.
     */
    public void storeDatasetRef(@NotNull String id, @NotNull String name) {
        datasetIdByName.put(name, id);
    }

    /**
     * Store a new preparation reference. In order to delete it later.
     *
     * @param id the preparation id.
     * @param name the preparation name.
     */
    public void storePreparationRef(@NotNull String id, @NotNull String name) {
        preparationIdByName.put(name, id);
    }

    /**
     * Store a temporary {@link File}.
     *
     * @param file the temporary {@link File} to store.
     */
    public void storeTempFile(@NotNull String filename, @NotNull File file) {
        tempFileByName.put(filename, file);
    }

    /**
     * List all created dataset id.
     *
     * @return a {@link List} of all created dataset id.
     */
    @NotNull
    public List<String> getDatasetIds() {
        return new ArrayList<>(datasetIdByName.values());
    }

    /**
     * List all created preparation id.
     *
     * @return a {@link List} of all created preparation id.
     */
    @NotNull
    public List<String> getPreparationIds() {
        return new ArrayList<>(preparationIdByName.values());
    }

    /**
     * Get the id of a stored dataset.
     *
     * @param datasetName the name of the searched dataset.
     * @return the dataset id.
     */
    @Nullable
    public String getDatasetId(@NotNull String datasetName) {
        return datasetIdByName.get(datasetName);
    }

    /**
     * Get the id of a stored preparation.
     *
     * @param preparationName the name of the searched preparation.
     * @return the preparation id.
     */
    @Nullable
    public String getPreparationId(@NotNull String preparationName) {
        return preparationIdByName.get(preparationName);
    }

    /**
     * Get a stored temporary {@link File}.
     *
     * @param fileName the stored temporary {@link File}.
     * @return the temporary stored {@link File}.
     */
    @Nullable
    public File getTempFile(@NotNull String fileName) {
        return tempFileByName.get(fileName);
    }

    /**
     * Clear the list of dataset.
     */
    public void clearDataset() {
        datasetIdByName.clear();
    }

    /**
     * Clear the list of preparation.
     */
    public void clearPreparation() {
        preparationIdByName.clear();
    }

    /**
     * Clear the list of temporary {@link File}.
     */
    public void clearTempFile() {
        tempFileByName.clear();
    }

    public void storeObject(@NotNull String key, @NotNull Object object) {
        featureContext.put(key, object);
    }

    public void removeObject(@NotNull String key) {
        featureContext.remove(key);
    }

    public Object getObject(@NotNull String key) {
        return featureContext.get(key);
    }

    public void clearObject() {
        featureContext.clear();
    }
}
