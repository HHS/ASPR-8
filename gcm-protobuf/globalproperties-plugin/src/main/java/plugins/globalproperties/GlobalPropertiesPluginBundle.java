package plugins.globalproperties;

import com.google.protobuf.Message;

import base.ParserContext;
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

    public void readPluginDataInput(ParserContext parserContext) {
        parserContext.parsePluginDataInput(this.reader, this.pluginDataMessage.newBuilderForType());
    }

    public void readJson(ParserContext parserContext) {
        throw new UnsupportedOperationException("This Bundle only has PluginDataInput to parse");
    }

}
