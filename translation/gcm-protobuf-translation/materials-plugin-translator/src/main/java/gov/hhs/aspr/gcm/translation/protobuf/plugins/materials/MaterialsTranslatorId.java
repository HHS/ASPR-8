package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials;

import gov.hhs.aspr.translation.core.TranslatorId;

/** 
 * TranslatorId for the Materials Translator
 */
public class MaterialsTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new MaterialsTranslatorId();

    private MaterialsTranslatorId() {
    }
}
