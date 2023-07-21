package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups;

import gov.hhs.aspr.translation.core.TranslatorId;

/** 
 * TranslatorId for the Groups Translator
 */
public class GroupsTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new GroupsTranslatorId();

    private GroupsTranslatorId() {
    }
}
