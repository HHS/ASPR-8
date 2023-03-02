package common;

import com.google.protobuf.Message;

import base.ParserContext;
import base.PluginBundle;
import base.TranslatorContext;
import common.translators.PropertyDefinitionTranslator;

public class PropertiesPluginBundle extends PluginBundle {

    public PropertiesPluginBundle(String inputFileName, Message pluginDataMessage) {
        super(inputFileName, pluginDataMessage);
        this.hasPluginData = false;
    }

    public PropertiesPluginBundle() {
        super();
        this.hasPluginData = false;
    }

    public void init(TranslatorContext translatorContext) {
        translatorContext.addTranslator(new PropertyDefinitionTranslator());
    }

    public void readPluginDataInput(ParserContext parserContext) {
        throw new RuntimeException("Properties Plugin Bundle does not have a PluginDataInput type to parse.");
    }

    public void readJson(ParserContext parserContext) {
        parserContext.parseJson(this.reader, this.pluginDataMessage.newBuilderForType());
    }

}
