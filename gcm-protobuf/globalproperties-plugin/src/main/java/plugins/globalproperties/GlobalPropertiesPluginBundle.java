package plugins.globalproperties;

import com.google.protobuf.Message;

import base.PluginBundle;
import base.TranslatorContext;
import plugins.globalproperties.translators.GlobalPropertiesPluginDataTranslator;

public class GlobalPropertiesPluginBundle extends PluginBundle {

    public GlobalPropertiesPluginBundle(String inputFileName, Message pluginDataMessage) {
        super(inputFileName, pluginDataMessage);
    }

    public GlobalPropertiesPluginBundle() {
        super();
    }

    public void init(TranslatorContext translatorContext) {
        translatorContext.addTranslator(new GlobalPropertiesPluginDataTranslator());
    }

    public void readPluginDataInput(TranslatorContext translatorContext) {
        translatorContext.parsePluginDataInput(this.reader, this.pluginDataMessage.newBuilderForType());
    }

    public void readJson(TranslatorContext translatorContext) {
        throw new UnsupportedOperationException("This Bundle only has PluginDataInput to parse");
    }

}
