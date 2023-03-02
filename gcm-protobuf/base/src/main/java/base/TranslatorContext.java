package base;

import java.io.Reader;

import com.google.protobuf.Message;

public class TranslatorContext {

    private final TranslatorController translatorController;

    public TranslatorContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <I extends Message, S> void addTranslator(AbstractTranslator<I, S> translator) {
        this.translatorController.addTranslator(translator);
    }

    public <U extends Message.Builder> void parsePluginDataInput(Reader reader, U builder) {
        this.translatorController.parsePluginDataInput(reader, builder);
    }

    public <U extends Message.Builder> void parseJson(Reader reader, U builder) {
        this.translatorController.parseJson(reader, builder);
    }
}
