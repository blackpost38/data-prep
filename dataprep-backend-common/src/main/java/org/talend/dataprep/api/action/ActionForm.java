/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.dataprep.api.action;

import java.util.List;

import org.talend.dataprep.parameters.Parameter;

/**
 * User-oriented representation of an action.
 */
public class ActionForm {

    /** Action unique id. Non i18n. */
    private String name;

    // TODO: should be an enum value
    /** Action category unique id. Non i18n. */
    private String category;

    /** Action parameters to build the form. */
    private List<Parameter> parameters;

    /** Short description of what the action does. */
    private String description;

    /** Action title. */
    private String label;

    // TODO: should be an URL object
    /** Action documentation URL. */
    private String docUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDocUrl() {
        return docUrl;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }
}
