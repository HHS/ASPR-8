package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import gov.hhs.aspr.translation.core.TranslatorId;

/** 
 * TranslatorId for the PersonProperties Translator
 */
public final class PersonPropertiesTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new PersonPropertiesTranslatorId();

    private PersonPropertiesTranslatorId() {
    }
}
