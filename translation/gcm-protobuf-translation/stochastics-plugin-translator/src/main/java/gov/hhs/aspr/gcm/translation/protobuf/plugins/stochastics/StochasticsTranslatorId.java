package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import gov.hhs.aspr.translation.core.TranslatorId;

/** 
 * TranslatorId for the Stochastics Translator
 */
public final class StochasticsTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new StochasticsTranslatorId();

    private StochasticsTranslatorId() {
    }
}
