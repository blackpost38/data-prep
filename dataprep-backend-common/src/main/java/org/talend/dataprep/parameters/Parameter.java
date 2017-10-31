// ============================================================================
//
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

package org.talend.dataprep.parameters;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.talend.dataprep.i18n.ActionsBundle.parameterDescription;
import static org.talend.dataprep.i18n.ActionsBundle.parameterLabel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.i18n.ActionsBundle;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bean that models action parameter.
 */
public class Parameter implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    /** The parameter name. */
    private String name;

    /** The parameter type. */
    private String type;

    /** The parameter default value. */
    private String defaultValue;

    /** True if the parameter is not displayed to the user. */
    private boolean implicit;

    /** True if the parameter can be blank. */
    private boolean canBeBlank;

    /** Provides a hint to user on how to fill parameter (e.g "http://" for a url, "mm/dd/yy" for a date). */
    private String placeHolder;

    /** The configuration. */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> configuration = new HashMap<>();

    private String label;

    private String description;

    public Parameter() {
    }

    /**
     * Minimal default constructor.
     *
     * @param name The parameter name.
     * @param type The parameter type.
     */
    public Parameter(String name, ParameterType type) {
        this(name, type, null, false, true);
    }

    /**
     * Full constructor.
     *
     * @param name The parameter name.
     * @param type The parameter type.
     * @param defaultValue the parameter default value.
     * @param implicit true if the parameter is implicit.
     * @param canBeBlank True if the parameter can be blank.
     */
    public Parameter(final String name, final ParameterType type, final String defaultValue, final boolean implicit,
            final boolean canBeBlank) {
        this(name, type, defaultValue, implicit, canBeBlank,
                StringUtils.EMPTY, ActionsBundle.parameterLabel(null, Locale.ENGLISH, name), ActionsBundle.parameterDescription(null, Locale.ENGLISH,
                        name));
    }

    public Parameter(final String name, final ParameterType type, final String defaultValue, final boolean implicit,
            final boolean canBeBlank, String placeHolder, String label, String description) {
        this.name = name;
        this.placeHolder = placeHolder;
        this.type = type == null ? null : type.asString();
        this.defaultValue = defaultValue;
        this.implicit = implicit;
        this.canBeBlank = canBeBlank;
        this.label = label;
        this.description = description;
    }

    void addConfiguration(String name, Object configuration) {
        this.configuration.put(name, configuration);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getDefault() {
        return defaultValue;
    }

    public boolean isImplicit() {
        return implicit;
    }

    public boolean isCanBeBlank() {
        return canBeBlank;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return implicit == parameter.implicit && canBeBlank == parameter.canBeBlank && Objects.equals(name, parameter.name)
                && Objects.equals(type, parameter.type) && Objects.equals(defaultValue, parameter.defaultValue) && Objects.equals(
                placeHolder, parameter.placeHolder) && Objects.equals(configuration, parameter.configuration) && Objects.equals(
                label, parameter.label) && Objects.equals(description, parameter.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, defaultValue, implicit, canBeBlank, placeHolder, configuration, label, description);
    }

    public static class ParameterBuilder {

        private String name;

        private ParameterType type;

        private String defaultValue = null;

        private boolean implicit = false;

        private boolean canBeBlank = true;

        private String placeHolder = EMPTY;

        private String label;

        private String description;

        public ParameterBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ParameterBuilder setType(ParameterType type) {
            this.type = type;
            return this;
        }

        public ParameterBuilder setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public ParameterBuilder setImplicit(boolean implicit) {
            this.implicit = implicit;
            return this;
        }

        public ParameterBuilder setCanBeBlank(boolean canBeBlank) {
            this.canBeBlank = canBeBlank;
            return this;
        }

        public ParameterBuilder setPlaceHolder(String placeHolder) {
            this.placeHolder = placeHolder;
            return this;
        }

        public ParameterBuilder setLabel(String label) {
            this.label = label;
            return this;
        }

        public ParameterBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Parameter createParameter(Object action, Locale locale) {
            // TODO this forces to have i18n for theses 2 fields
            if (label == null) {
                try {
                    label = parameterLabel(action, locale, name);
                } catch (Exception e) {
                }
            }
            if (description == null) {
                try {
                    description = parameterDescription(action, locale, name);
                } catch (Exception e) {
                }
            }
            return new Parameter(name, type, defaultValue, implicit, canBeBlank, placeHolder, label, description);
        }

    }
}
