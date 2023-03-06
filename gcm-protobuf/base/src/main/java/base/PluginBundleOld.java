package base;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Paths;

import com.google.protobuf.Message;

import nucleus.PluginData;

public abstract class PluginBundleOld {
    protected Reader reader = null;
    protected Writer writer = null;
    protected Message pluginDataMessage;
    protected boolean isDependency = false;
    protected boolean hasPluginData = true;

    public PluginBundleOld(String inputFileName, String outputFileName, Message pluginDataMessage) {
        try {
            this.reader = new FileReader(Paths.get(inputFileName).toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to create Reader");
        }
        try {
            this.writer = new FileWriter(Paths.get(outputFileName).toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Writer");
        }
        this.pluginDataMessage = pluginDataMessage;
    }

    public PluginBundleOld() {
        this.isDependency = true;
    }

    public abstract void init(TranslatorContext translatorContext);

    public abstract void readPluginDataInput(ReaderContext parserContext);

    public abstract void readJson(ReaderContext parserContext);

    public abstract void writePluginDataOutput(WriterContext writerContext, PluginData pluginData);

    public abstract void writeJson(WriterContext writerContext, Object simObject);

    public boolean hasPluginData() {
        return this.hasPluginData;
    }

    public boolean isDependency() {
        return this.isDependency;
    }
}
