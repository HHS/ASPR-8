package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import gov.hhs.aspr.translation.core.TranslatorId;

/** 
 * TranslatorId for the Regions Translator
 */
public class ResourcesTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new ResourcesTranslatorId();

    private ResourcesTranslatorId() {
    }
}
