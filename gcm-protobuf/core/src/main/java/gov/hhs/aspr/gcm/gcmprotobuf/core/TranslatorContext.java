package gov.hhs.aspr.gcm.gcmprotobuf.core;

import com.google.protobuf.Message;

public class TranslatorContext {

    private final TranslatorController translatorController;

    public TranslatorContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <I extends Message, S> void addTranslator(AbstractTranslator<I, S> translator) {
        this.translatorController.addTranslator(translator);
    }
}
