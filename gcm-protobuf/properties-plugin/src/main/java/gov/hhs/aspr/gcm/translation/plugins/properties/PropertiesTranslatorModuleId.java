package gov.hhs.aspr.gcm.translation.plugins.properties;

import gov.hhs.aspr.gcm.translation.core.TranslatorModuleId;

public final class PropertiesTranslatorModuleId implements TranslatorModuleId {
    public final static TranslatorModuleId TRANSLATOR_MODULE_ID = new PropertiesTranslatorModuleId();

    private PropertiesTranslatorModuleId() {
    }
}
