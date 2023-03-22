package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorId;

public final class PropertiesTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new PropertiesTranslatorId();

    private PropertiesTranslatorId() {
    }
}
