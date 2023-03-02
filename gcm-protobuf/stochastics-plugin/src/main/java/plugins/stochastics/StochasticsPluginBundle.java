package plugins.stochastics;

import com.google.protobuf.Message;

import base.ParserContext;
import base.PluginBundle;
import base.TranslatorContext;
import plugins.stochastics.translators.StochasticsPluginDataTranslator;

public class StochasticsPluginBundle extends PluginBundle {

    public StochasticsPluginBundle(String inputFileName, Message pluginDataMessage) {
        super(inputFileName, pluginDataMessage);
    }

    public StochasticsPluginBundle() {
        super();
    }

    public void init(TranslatorContext translatorContext) {
        translatorContext.addTranslator(new StochasticsPluginDataTranslator());
    }

    public void readPluginDataInput(ParserContext parserContext) {
        parserContext.parsePluginDataInput(this.reader, this.pluginDataMessage.newBuilderForType());
    }

    public void readJson(ParserContext parserContext) {
        throw new UnsupportedOperationException("This Bundle only has PluginDataInput to parse");
    }

}
