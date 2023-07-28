package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions;

import gov.hhs.aspr.ms.taskit.core.TranslatorId;

/**
 * TranslatorId for the Partitions Translator
 */
public class PartitionsTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new PartitionsTranslatorId();

    private PartitionsTranslatorId() {
    }
}
