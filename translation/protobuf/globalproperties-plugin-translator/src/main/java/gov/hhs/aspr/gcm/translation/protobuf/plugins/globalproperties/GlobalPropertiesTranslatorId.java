package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import gov.hhs.aspr.gcm.translation.core.TranslatorId;

public final class GlobalPropertiesTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new GlobalPropertiesTranslatorId();

    private GlobalPropertiesTranslatorId() {
    }
}
