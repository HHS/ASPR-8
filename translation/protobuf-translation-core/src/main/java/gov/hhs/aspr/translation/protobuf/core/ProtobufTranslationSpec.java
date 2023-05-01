package gov.hhs.aspr.translation.protobuf.core;

import gov.hhs.aspr.translation.core.TranslationSpec;
import gov.hhs.aspr.translation.core.TranslationEngine;

public abstract class ProtobufTranslationSpec<I, A> extends TranslationSpec<I, A> {
    protected ProtobufTranslationEngine translationEngine;

    public void init(TranslationEngine translationEngine) {
        super.init(translationEngine);
        this.translationEngine = (ProtobufTranslationEngine) translationEngine;
    }
}
