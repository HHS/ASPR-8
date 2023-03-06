package plugins.stochastics;

import com.google.protobuf.Message;

import base.ReaderContext;
import base.PluginBundle;
import base.TranslatorContext;
import base.WriterContext;
import nucleus.PluginData;
import plugins.stochastics.translators.StochasticsPluginDataTranslator;

public class StochasticsPluginBundle extends PluginBundle {

    public StochasticsPluginBundle(String inputFileName, String outputFilename, Message pluginDataMessage) {
        super(inputFileName, outputFilename, pluginDataMessage);
    }

    public StochasticsPluginBundle() {
        super();
    }

    public void init(TranslatorContext translatorContext) {
        translatorContext.addTranslator(new StochasticsPluginDataTranslator());
    }

    public void readPluginDataInput(ReaderContext parserContext) {
        parserContext.readPluginDataInput(this.reader, this.pluginDataMessage.newBuilderForType());
    }

    public void readJson(ReaderContext parserContext) {
        throw new UnsupportedOperationException("This Bundle only has PluginDataInput to parse");
    }

    @Override
    public void writePluginDataOutput(WriterContext writerContext, PluginData pluginData) {
        writerContext.writePluginDataOutput(writer, pluginData);
    }

    @Override
    public void writeJson(WriterContext writerContext, Object simObject) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeJson'");
    }

}
