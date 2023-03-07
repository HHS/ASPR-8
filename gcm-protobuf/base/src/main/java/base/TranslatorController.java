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
        private final List<PluginBundleOld> pluginBundlesOld = new ArrayList<>();
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

        public Builder addBundleOld(PluginBundleOld pluginBundle) {
            this.data.pluginBundlesOld.add(pluginBundle);
            return this;
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

    public TranslatorController readInput() {
        TranslatorContext translatorContext = new TranslatorContext(this);

        for (PluginBundle pluginBundle : this.data.pluginBundles) {
            this.focalBundle = pluginBundle;
            pluginBundle.getInitializer().accept(translatorContext);
            this.focalBundle = null;
        }

        this.masterTranslator = this.data.masterTranslatorBuilder.build();

        this.masterTranslator.init();

        ReaderContext readerContext = new ReaderContext(this);

        for (PluginBundle pluginBundle : this.data.pluginBundles) {
            this.focalBundle = pluginBundle;
            if (!pluginBundle.hasInput())
                continue;
            if (pluginBundle.inputIsPluginData()) {
                pluginBundle.readPluginDataInput(readerContext);
                continue;
            }
            pluginBundle.readInput(readerContext);

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
            pluginBundle.writeOutput(writerContext, simObject);
        }
    }

    public List<PluginData> getPluginDatas() {
        return this.pluginDatas;
    }

    public List<Object> getObjects() {
        return this.objects;
    }
}
