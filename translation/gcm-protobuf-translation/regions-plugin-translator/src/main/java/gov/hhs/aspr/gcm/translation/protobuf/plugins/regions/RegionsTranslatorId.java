package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions;

import gov.hhs.aspr.translation.core.TranslatorId;

/** 
 * TranslatorId for the Regions Translator
 */
public class RegionsTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new RegionsTranslatorId();

    private RegionsTranslatorId() {
    }
}
