package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties;

import gov.hhs.aspr.ms.taskit.core.TranslatorId;

/**
 * TranslatorId for the Properties Translator
 */
public final class PropertiesTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new PropertiesTranslatorId();

    private PropertiesTranslatorId() {
    }
}
