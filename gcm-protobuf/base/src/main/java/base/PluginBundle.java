package base;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;

import com.google.protobuf.Message;

public abstract class PluginBundle {
    protected Reader reader = null;
    protected Message pluginDataMessage;
    protected boolean isDependency = false;
    protected boolean hasPluginData = true;

    public PluginBundle(String inputFileName, Message pluginDataMessage) {
        try {
            this.reader = new FileReader(Paths.get(inputFileName).toFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.pluginDataMessage = pluginDataMessage;
    }

    public PluginBundle() {
        this.isDependency = true;
    }

    public abstract void init(TranslatorContext translatorContext);

    public abstract void readPluginDataInput(ParserContext parserContext);

    public abstract void readJson(ParserContext parserContext);

    public boolean hasPluginData() {
        return this.hasPluginData;
    }

    public boolean isDependency() {
        return this.isDependency;
    }
}
