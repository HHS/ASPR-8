package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups;

import gov.hhs.aspr.ms.taskit.core.TranslatorId;

/** 
 * TranslatorId for the Groups Translator
 */
public class GroupsTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new GroupsTranslatorId();

    private GroupsTranslatorId() {
    }
}
