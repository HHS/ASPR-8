package base;

import java.io.Reader;

import com.google.protobuf.Message;

public class ParserContext {
    private final TranslatorController translatorController;

    public ParserContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <U extends Message.Builder> void parsePluginDataInput(Reader reader, U builder) {
        this.translatorController.parsePluginDataInput(reader, builder);
    }

    public <U extends Message.Builder> void parseJson(Reader reader, U builder) {
        this.translatorController.parseJson(reader, builder);
    }
}
