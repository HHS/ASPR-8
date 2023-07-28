package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties;

import gov.hhs.aspr.ms.taskit.core.TranslatorId;

/**
 * TranslatorId for the GlobalProperties Translator
 */
public final class GlobalPropertiesTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new GlobalPropertiesTranslatorId();

    private GlobalPropertiesTranslatorId() {
    }
}
