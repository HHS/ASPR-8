package gov.hhs.aspr.gcm.translation.plugins.stochastics;

import gov.hhs.aspr.gcm.translation.core.TranslatorId;

public final class StochasticsTranslatorId implements TranslatorId {
    public final static TranslatorId PLUGIN_BUNDLE_ID = new StochasticsTranslatorId();

    private StochasticsTranslatorId() {
    }
}
