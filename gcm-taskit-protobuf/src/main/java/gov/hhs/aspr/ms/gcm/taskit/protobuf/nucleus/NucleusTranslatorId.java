package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus;

import gov.hhs.aspr.ms.taskit.core.TranslatorId;

/**
 * TranslatorId for the Nucleus Translator
 */
public class NucleusTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new NucleusTranslatorId();

    private NucleusTranslatorId() {
    }
}
