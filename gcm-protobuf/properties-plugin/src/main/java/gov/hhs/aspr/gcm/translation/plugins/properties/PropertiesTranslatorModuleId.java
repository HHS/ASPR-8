package gov.hhs.aspr.gcm.translation.plugins.properties;

import gov.hhs.aspr.gcm.translation.core.TranslatorId;

public final class PropertiesTranslatorModuleId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_MODULE_ID = new PropertiesTranslatorModuleId();

    private PropertiesTranslatorModuleId() {
    }
}
