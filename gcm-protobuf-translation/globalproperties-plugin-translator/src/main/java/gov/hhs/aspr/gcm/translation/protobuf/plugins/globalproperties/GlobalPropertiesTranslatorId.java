package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties;

import gov.hhs.aspr.translation.core.TranslatorId;

/** 
 * TranslatorId for the GlobalProperties Translator
 */
public final class GlobalPropertiesTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new GlobalPropertiesTranslatorId();

    private GlobalPropertiesTranslatorId() {
    }
}
