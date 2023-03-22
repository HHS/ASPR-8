package gov.hss.aspr.gcm.translation.protobuf.nucleus;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorId;

public class NucleusTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new NucleusTranslatorId();

    private NucleusTranslatorId() {
    }
}
