package gov.hhs.aspr.translation.protobuf.core;

import gov.hhs.aspr.translation.core.TranslationSpec;
import gov.hhs.aspr.translation.core.TranslationEngine;

public abstract class ProtobufTranslationSpec<I, A> extends TranslationSpec<I, A> {
    protected ProtobufTranslatorCore translatorCore;

    public void init(TranslationEngine translatorCore) {
        super.init(translatorCore);
        this.translatorCore = (ProtobufTranslatorCore) translatorCore;
    }
}
