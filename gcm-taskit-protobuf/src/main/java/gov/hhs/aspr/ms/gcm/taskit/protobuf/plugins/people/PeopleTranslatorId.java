package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people;

import gov.hhs.aspr.ms.taskit.core.TranslatorId;

/**
 * TranslatorId for the People Translator
 */
public final class PeopleTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new PeopleTranslatorId();

    private PeopleTranslatorId() {
    }
}
