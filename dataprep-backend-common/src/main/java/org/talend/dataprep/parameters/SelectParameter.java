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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Parameter that should be displayed as a select box in the UI.
 */
public class SelectParameter extends Parameter {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    private final boolean isRadio;

    /** The select items. */
    @JsonIgnore // will be part of the Parameter#configuration
    private List<Item> items;

    /** True if multiple items can be selected. */
    @JsonIgnore // will be part of the Parameter#configuration
    private boolean multiple;

    /**
     * Private constructor to ensure the use of builder.
     *
     * @param name The parameter name.
     * @param defaultValue The parameter default value.
     * @param implicit True if the parameter is implicit.
     * @param canBeBlank True if the parameter can be blank.
     * @param items List of items for this select parameter.
     * @param multiple True if multiple selection is allowed.
     * @param isRadio <code>true</code> if the rendering code should prefer radio buttons instead of drop down list.
     */
    private SelectParameter(String name, String defaultValue, boolean implicit, boolean canBeBlank, List<Item> items,
            boolean multiple, boolean isRadio, String label, String description) {
        super(name, ParameterType.SELECT, defaultValue, implicit, canBeBlank, EMPTY, label, description);
        this.isRadio = isRadio;
        setItems(items);
        setMultiple(multiple);
    }

    public boolean isRadio() {
        return isRadio;
    }

    public List<Item> getItems() {
        return items;
    }

    private void setItems(List<Item> items) {
        addConfiguration("values", items);
        this.items = items;
    }

    public boolean isMultiple() {
        return multiple;
    }

    private void setMultiple(boolean multiple) {
        addConfiguration("multiple", multiple);
        this.multiple = multiple;
    }

    /**
     * Builder used to simplify the syntax of creation.
     */
    public static class Builder {

        /** List of items. */
        private final List<Item> items = new ArrayList<>();

        /** True if the selection is multiple. */
        private final boolean multiple = false;

        /** The parameter name. */
        private String name = "";

        /** The parameter default value. */
        private String defaultValue = "";

        /** True if the parameter is not displayed to the user. */
        private boolean implicit = false;

        /** True if the parameter can be blank. */
        private boolean canBeBlank = false;

        /** True if rendering should prefer radio buttons to render parameters choices */
        private boolean isRadio = false;

        private String label;

        private String description;
        /**
         * @return A SelectParameter builder.
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Set the name of the select parameter.
         *
         * @param name the name of the select parameter.
         * @return the builder to carry on building the column.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the defaultValue of the select parameter.
         *
         * @param defaultValue the default value of the select parameter.
         * @return the builder to carry on building the column.
         */
        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Set the implicit of the select parameter.
         *
         * @param implicit true if the parameter is implicit.
         * @return the builder to carry on building the column.
         */
        public Builder implicit(boolean implicit) {
            this.implicit = implicit;
            return this;
        }

        /**
         * Set the canBeBlank of the select parameter.
         *
         * @param canBeBlank true if the parameter is implicit.
         * @return the builder to carry on building the column.
         */
        public Builder canBeBlank(boolean canBeBlank) {
            this.canBeBlank = canBeBlank;
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @param parameter the item optional parameter.
         * @return the builder to carry on building the column.
         */
        public Builder item(String value, Parameter... parameter) {
            this.items.add(Item.Builder.builder().value(value).inlineParameters(Arrays.asList(parameter)).build(locale));
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         */
        public Builder item(String value) {
            final Item item = Item.Builder.builder().value(value).build(locale);
            this.items.add(item);
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @param label the item label
         * @return the builder to carry on building the column.
         */
        public Builder item(String value, String label) {
            final Item item = Item.Builder.builder().value(value).label(label).build(locale);
            this.items.add(item);
            return this;
        }

        /**
         * Add an 'constant' item (an item with a value, but no label translation) to the select parameter builder. Unlike the
         * {@link #item(String, String)} the second parameter
         * is <b>not</b> a key to a i18n label but a constant label to be taken as is.
         *
         * @param value the item value.
         * @param text the item (constant) label
         * @return the builder to carry on building the column.
         */
        public Builder constant(String value, String text) {
            final Item item = Item.Builder.builder().value(value).text(text).build(locale);
            this.items.add(item);
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @param parameter the item optional parameter.
         * @return the builder to carry on building the column.
         */
        public Builder item(String value, String label, Parameter... parameter) {
            this.items.add(Item.Builder.builder().value(value).label(label).inlineParameters(Arrays.asList(parameter)).build(
                    locale));
            return this;
        }

        /**
         * Add all items to the select parameter builder.
         *
         * @param items the item name.
         * @return the builder to carry on building the column.
         */
        public Builder items(List<Item> items) {
            this.items.addAll(items);
            return this;
        }

        public Builder radio(boolean isRadio) {
            this.isRadio = isRadio;
            return this;
        }

        public Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Build the column with the previously entered values.
         *
         * @return the built column metadata.
         * @param action
         * @param locale
         */
        public SelectParameter build(Object action, Locale locale) {
            if (label == null) {
                label = parameterLabel(action, locale, name);
            }
            if (description == null) {
                description = parameterDescription(action, locale, name);
            }
            return new SelectParameter(name, defaultValue, implicit, canBeBlank, items, multiple, isRadio, label, description);
        }
    }

}
