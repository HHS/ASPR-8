package core;

import java.io.Writer;

import nucleus.PluginData;

public class WriterContext {
    private final TranslatorController translatorController;

    public WriterContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <T extends PluginData> void writePluginDataOutput(Writer writer, T pluginData) {
        this.translatorController.writePluginDataInput(writer, pluginData);
    }

    public void writeJson(Writer writer, Object simObject) {
        this.translatorController.writeJson(writer, simObject);
    }
}
