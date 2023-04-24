package gov.hhs.aspr.translation.protobuf.core;

import gov.hhs.aspr.translation.core.TranslatorSpec;
import gov.hhs.aspr.translation.core.TranslatorCore;

public abstract class ProtobufTranslatorSpec<I, A> extends TranslatorSpec<I, A> {
    protected ProtobufTranslatorCore translatorCore;

    public void init(TranslatorCore translatorCore) {
        super.init(translatorCore);
        this.translatorCore = (ProtobufTranslatorCore) translatorCore;
    }
}
