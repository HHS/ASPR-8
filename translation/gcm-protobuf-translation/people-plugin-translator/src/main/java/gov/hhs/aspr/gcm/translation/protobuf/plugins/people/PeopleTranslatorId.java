package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import gov.hhs.aspr.translation.core.TranslatorId;

/** 
 * TranslatorId for the People Translator
 */
public final class PeopleTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new PeopleTranslatorId();

    private PeopleTranslatorId() {
    }
}
