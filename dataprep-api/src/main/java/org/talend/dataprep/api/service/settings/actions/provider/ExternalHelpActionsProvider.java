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

package org.talend.dataprep.api.service.settings.actions.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.settings.actions.api.ActionSettings;
import org.talend.dataprep.help.CommunityLinksManager;
import org.talend.dataprep.help.DocumentationLinksManager;

import static org.talend.dataprep.api.service.settings.actions.api.ActionSettings.*;

@Component
public class ExternalHelpActionsProvider {

    @Autowired
    private CommunityLinksManager communityLinksManager;

    @Autowired
    private DocumentationLinksManager documentationLinksManager;

    public ActionSettings getExternalHelpAction() {
        return builder()
                .id("external:help")
                .name("Help")
                .icon("talend-question-circle")
                .type("@@external/OPEN_WINDOW")
                .payload(PAYLOAD_METHOD_KEY, "open")
                .payload(PAYLOAD_ARGS_KEY, new String[]{documentationLinksManager.getFuzzyUrl() + "header"})
                .build();
    }

    public ActionSettings getExternalCommunityAction() {
        return builder()
                .id("external:community")
                .name("Community")
                .type("@@external/OPEN_WINDOW")
                .payload(PAYLOAD_METHOD_KEY, "open")
                .payload(PAYLOAD_ARGS_KEY, new String[]{communityLinksManager.getCommunityUrl()})
                .build();
    }
}
