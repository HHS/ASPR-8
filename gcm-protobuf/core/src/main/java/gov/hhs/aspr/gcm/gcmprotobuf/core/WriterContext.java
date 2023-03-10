package gov.hhs.aspr.gcm.gcmprotobuf.core;

import java.io.Writer;

import nucleus.PluginData;

public class WriterContext {
    private final TranslatorController translatorController;

    public WriterContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <T extends PluginData> void writePluginDataOutput(Writer writer, T pluginData) {
        this.translatorController.writePluginDataOutput(writer, pluginData);
    }

    public void writeOutput(Writer writer, Object simObject) {
        this.translatorController.writeOutput(writer, simObject);
    }
}
