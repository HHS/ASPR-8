package gov.hhs.aspr.translation.core.testsupport;

import gov.hhs.aspr.translation.core.TranslationEngine;
import gov.hhs.aspr.translation.core.TranslationSpec;

public abstract class TestTranslationSpec<I, A> extends TranslationSpec<I, A> {
    protected TestTranslationEngine translationEngine;

    public void init(TranslationEngine translationEngine) {
        super.init(translationEngine);
        this.translationEngine = (TestTranslationEngine) translationEngine;
    }
}
