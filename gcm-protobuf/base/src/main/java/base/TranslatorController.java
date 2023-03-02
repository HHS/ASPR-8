package base;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;

import nucleus.PluginData;

public class TranslatorController {
    private final Data data;
    private MasterTranslator masterTranslator;
    private final List<PluginData> pluginDatas = new ArrayList<>();
    private final List<Object> objects = new ArrayList<>();

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

        public Builder addBundle(PluginBundle pluginBundle) {
            this.data.pluginBundles.add(pluginBundle);
            return this;
        }

        public <I extends Message, S> Builder addCustomTranslator(AbstractTranslator<I, S> customTranslator) {
            this.data.masterTranslatorBuilder.addCustomTranslator(customTranslator);
            return this;
        }

        public TranslatorController build() {
            return new TranslatorController(this.data);
        }
    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    protected <I extends Message, S> void addTranslator(AbstractTranslator<I, S> translator) {
        this.data.masterTranslatorBuilder.addCustomTranslator(translator);
    }

    public <U extends Message.Builder> void parsePluginDataInput(Reader reader, U builder) {
        this.pluginDatas.add(this.masterTranslator.parseJson(reader, builder));
    }

    public <U extends Message.Builder> void parseJson(Reader reader, U builder) {
        this.objects.add(this.masterTranslator.parseJson(reader, builder));
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
            pluginBundle.init(translatorContext);
        }

        this.masterTranslator = this.data.masterTranslatorBuilder.build();

        this.masterTranslator.init();

        ParserContext parserContext = new ParserContext(this);

        for (PluginBundle pluginBundle : this.data.pluginBundles) {
            if (!pluginBundle.isDependency()) {
                if (pluginBundle.hasPluginData()) {
                    pluginBundle.readPluginDataInput(parserContext);
                } else {
                    pluginBundle.readJson(parserContext);
                }
            }
        }

        return this;
    }

    public List<PluginData> getPluginDatas() {
        return this.pluginDatas;
    }

    public List<Object> getObjects() {
        return this.objects;
    }
}
