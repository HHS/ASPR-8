package common;

import com.google.protobuf.Message;

import base.ReaderContext;
import base.PluginBundleOld;
import base.TranslatorContext;
import base.WriterContext;
import common.translators.PropertyDefinitionTranslator;
import nucleus.PluginData;

public class PropertiesPluginBundleOld extends PluginBundleOld {

    public PropertiesPluginBundleOld(String inputFileName, String outputFileName, Message pluginDataMessage) {
        super(inputFileName, outputFileName, pluginDataMessage);
        this.hasPluginData = false;
    }

    public PropertiesPluginBundleOld() {
        super();
        this.hasPluginData = false;
    }

    public void init(TranslatorContext translatorContext) {
        translatorContext.addTranslator(new PropertyDefinitionTranslator());
    }

    public void readPluginDataInput(ReaderContext parserContext) {
        throw new RuntimeException("Properties Plugin Bundle does not have a PluginDataInput type to parse.");
    }

    public void readJson(ReaderContext parserContext) {
        parserContext.readJson(this.reader, this.pluginDataMessage.newBuilderForType());
    }

    public void writePluginDataOutput(WriterContext writerContext, PluginData pluginData) {
        throw new RuntimeException("Properties Plugin Bundle does not have a PluginDataInput type to write.");
    }

    public void writeJson(WriterContext writerContext, Object simObject) {
        writerContext.writeJson(writer, simObject);
    }

}
