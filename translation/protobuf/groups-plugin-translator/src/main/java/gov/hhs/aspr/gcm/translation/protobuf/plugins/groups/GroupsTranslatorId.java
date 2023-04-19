package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups;

import gov.hhs.aspr.gcm.translation.core.TranslatorId;

public class GroupsTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new GroupsTranslatorId();

    private GroupsTranslatorId() {
    }
}
