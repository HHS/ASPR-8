package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import gov.hhs.aspr.translation.core.TranslatorId;

/** 
 * TranslatorId for the Properties Translator
 */
public final class PropertiesTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new PropertiesTranslatorId();

    private PropertiesTranslatorId() {
    }
}
