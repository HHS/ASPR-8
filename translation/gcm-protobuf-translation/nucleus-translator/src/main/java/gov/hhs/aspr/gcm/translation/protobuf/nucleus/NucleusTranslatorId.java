package gov.hhs.aspr.gcm.translation.protobuf.nucleus;

import gov.hhs.aspr.translation.core.TranslatorId;

public class NucleusTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new NucleusTranslatorId();

    private NucleusTranslatorId() {
    }
}
