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

package org.talend.dataprep.api.service.settings.views.api.list;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.talend.dataprep.api.service.settings.actions.provider.PreparationActions.PREPARATION_FOLDER_FETCH;

import org.talend.dataprep.api.service.settings.views.api.ViewSettings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * List settings.
 * It contains the list details configuration and its toolbar configuration.
 * see https://talend.github.io/react-talend-components/?selectedKind=List&selectedStory=table%20%28default%29&full=0&down=1&left=1&panelRight=0&downPanel=kadirahq%2Fstorybook-addon-actions%2Factions-panel
 */
@JsonInclude(NON_NULL)
public class ListSettings implements ViewSettings {

    public static final String VIEW_TYPE = TYPE_LIST;

    /**
     * The key in the views dictionary
     */
    @JsonIgnore
    private String id;

    /**
     * An action (identifier) to perform on component mount
     */
    private String didMountActionCreator;

    /**
     * The list details configuration
     */
    private ListDetailsSettings list;

    /**
     * The toolbar configuration
     */
    private ToolbarDetailsSettings toolbar;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDidMountActionCreator() {
        return didMountActionCreator;
    }

    public void setDidMountActionCreator(final String didMountActionCreator) {
        this.didMountActionCreator = didMountActionCreator;
    }

    public ListDetailsSettings getList() {
        return list;
    }

    public void setList(final ListDetailsSettings list) {
        this.list = list;
    }

    public ToolbarDetailsSettings getToolbar() {
        return toolbar;
    }

    public void setToolbar(final ToolbarDetailsSettings toolbar) {
        this.toolbar = toolbar;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(final ListSettings viewSettings) {
        return builder() //
                .id(viewSettings.getId()) //
                .didMountActionCreator(PREPARATION_FOLDER_FETCH.getId())
                .list(viewSettings.getList()) //
                .toolbar(viewSettings.getToolbar());
    }

    public static class Builder {

        private String id;

        private String didMountActionCreator;

        private ListDetailsSettings list;

        private ToolbarDetailsSettings toolbar;

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder didMountActionCreator(final String didMountActionCreator) {
            this.didMountActionCreator = didMountActionCreator;
            return this;
        }

        public Builder list(final ListDetailsSettings list) {
            this.list = list;
            return this;
        }

        public Builder toolbar(final ToolbarDetailsSettings toolbar) {
            this.toolbar = toolbar;
            return this;
        }

        public ListSettings build() {
            final ListSettings settings = new ListSettings();
            settings.setId(this.id);
            settings.setDidMountActionCreator(this.didMountActionCreator);
            settings.setList(this.list);
            settings.setToolbar(this.toolbar);
            return settings;
        }
    }
}
