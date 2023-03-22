package gov.hhs.aspr.gcm.translation.protobuf.core;

import java.io.Writer;

import nucleus.PluginData;

public class WriterContext {
    private final TranslatorController translatorController;
    private final Integer scenarioId;

    public WriterContext(TranslatorController translatorController, Integer scenarioId) {
        this.translatorController = translatorController;
        this.scenarioId = scenarioId;
    }

    public WriterContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
        this.scenarioId = 0;
    }

    public <T extends PluginData> void writePluginDataOutput(Writer writer, T pluginData) {
        this.translatorController.writePluginDataOutput(writer, pluginData);
    }

    public void writeJsonOutput(Writer writer, Object simObject) {
        this.translatorController.writeJsonOutput(writer, simObject);
    }

    public Integer getScenarioId() {
        return this.scenarioId;
    }
}
