package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports;

import gov.hhs.aspr.translation.core.TranslatorId;

/** 
 * TranslatorId for the Reports Translator
 */
public final class ReportsTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new ReportsTranslatorId();

    private ReportsTranslatorId() {
    }
}
