package base;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;

import nucleus.PluginData;

public class TranslatorController {
    private final Data data;
    private MasterTranslator masterTranslator;
    private final List<PluginData> pluginDatas = new ArrayList<>();
    private final List<Object> objects = new ArrayList<>();
    private final Map<Class<?>, PluginBundle> simObjectClassToPluginBundleMap = new LinkedHashMap<>();
    private PluginBundle focalBundle = null;

    private TranslatorController(Data data) {
        this.data = data;
    }

    private static class Data {
        private MasterTranslator.Builder masterTranslatorBuilder = MasterTranslator.builder();
        private final List<PluginBundle> pluginBundles = new ArrayList<>();

        private Data() {
        }
    }

    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        public TranslatorController build() {
            return new TranslatorController(this.data);
        }

        public Builder addBundle(PluginBundle pluginBundle) {
            this.data.pluginBundles.add(pluginBundle);
            return this;
        }

        public <I extends Message, S> Builder addCustomTranslator(AbstractTranslator<I, S> customTranslator) {
            this.data.masterTranslatorBuilder.addCustomTranslator(customTranslator);
            return this;
        }

        public Builder setIgnoringUnknownFields(boolean ignoringUnknownFields) {
            this.data.masterTranslatorBuilder.setIgnoringUnknownFields(ignoringUnknownFields);
            return this;
        }

        public Builder setIncludingDefaultValueFields(boolean includingDefaultValueFields) {
            this.data.masterTranslatorBuilder.setIncludingDefaultValueFields(includingDefaultValueFields);
            return this;
        }

    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    protected <I extends Message, S> void addTranslator(AbstractTranslator<I, S> translator) {
        this.data.masterTranslatorBuilder.addCustomTranslator(translator);
    }

    protected <U extends Message.Builder> void readPluginDataInput(Reader reader, U builder) {
        PluginData pluginData = this.masterTranslator.readJson(reader, builder);

        this.pluginDatas.add(pluginData);
        this.simObjectClassToPluginBundleMap.put(pluginData.getClass(), focalBundle);
    }

    protected <U extends Message.Builder> void readJson(Reader reader, U builder) {
        Object simObject = this.masterTranslator.readJson(reader, builder);

        this.objects.add(simObject);
        this.simObjectClassToPluginBundleMap.put(simObject.getClass(), focalBundle);
    }

    protected <T extends PluginData> void writePluginDataInput(Writer writer, T pluginData) {
        this.masterTranslator.printJson(writer, pluginData);
    }

    protected void writeJson(Writer writer, Object simObject) {
        this.masterTranslator.printJson(writer, simObject);
    }

    // temporary pass through method
    public MasterTranslator getMasterTranslator() {
        if (this.masterTranslator == null) {
            throw new RuntimeException("master translator is null");
        }

        return this.masterTranslator;
    }

    public TranslatorController loadInput() {

        TranslatorContext translatorContext = new TranslatorContext(this);

        for (PluginBundle pluginBundle : this.data.pluginBundles) {
            this.focalBundle = pluginBundle;
            pluginBundle.init(translatorContext);
            this.focalBundle = null;
        }

        this.masterTranslator = this.data.masterTranslatorBuilder.build();

        this.masterTranslator.init();

        ReaderContext parserContext = new ReaderContext(this);

        for (PluginBundle pluginBundle : this.data.pluginBundles) {
            this.focalBundle = pluginBundle;
            if (!pluginBundle.isDependency()) {
                if (pluginBundle.hasPluginData()) {
                    pluginBundle.readPluginDataInput(parserContext);
                } else {
                    pluginBundle.readJson(parserContext);
                }
            }
            this.focalBundle = null;
        }

        return this;
    }

    public void writeOutput() {
        WriterContext writerContext = new WriterContext(this);

        for (PluginData pluginData : this.pluginDatas) {
            PluginBundle pluginBundle = this.simObjectClassToPluginBundleMap.get(pluginData.getClass());
            pluginBundle.writePluginDataOutput(writerContext, pluginData);
        }

        for (Object simObject : this.objects) {
            PluginBundle pluginBundle = this.simObjectClassToPluginBundleMap.get(simObject.getClass());
            pluginBundle.writeJson(writerContext, simObject);
        }
    }

    public List<PluginData> getPluginDatas() {
        return this.pluginDatas;
    }

    public List<Object> getObjects() {
        return this.objects;
    }
}
