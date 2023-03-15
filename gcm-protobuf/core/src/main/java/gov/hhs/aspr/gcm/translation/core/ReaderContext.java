package gov.hhs.aspr.gcm.translation.core;

import java.io.Reader;

import com.google.protobuf.Message;

public class ReaderContext {
    private final TranslatorController translatorController;

    public ReaderContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <U extends Message.Builder> void readPluginDataInput(Reader reader, U builder) {
        this.translatorController.readPluginDataInput(reader, builder);
    }

    public <U extends Message.Builder> void readJsonInput(Reader reader, U builder) {
        this.translatorController.readJsonInput(reader, builder);
    }
}
