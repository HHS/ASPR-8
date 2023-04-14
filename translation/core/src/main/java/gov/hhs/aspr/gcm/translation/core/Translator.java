package gov.hhs.aspr.gcm.translation.core;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class Translator {
    private final Data data;

    private Translator(Data data) {
        this.data = data;
    }

    private static class Data {
        private TranslatorId translatorId;
        private Consumer<TranslatorContext> initializer;
        private final Set<TranslatorId> dependencies = new LinkedHashSet<>();

        private Data() {

        }

    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        public Translator build() {

            if (this.data.translatorId == null) {
                throw new RuntimeException("No TranslatorId was set for this Translator");
            }

            return new Translator(data);
        }

        public Builder setTranslatorId(TranslatorId translatorId) {
            this.data.translatorId = translatorId;

            return this;
        }

        public Builder setInitializer(Consumer<TranslatorContext> initConsumer) {
            this.data.initializer = initConsumer;

            return this;
        }

        public Builder addDependency(TranslatorId dependency) {
            this.data.dependencies.add(dependency);

            return this;
        }

    }

    public Consumer<TranslatorContext> getInitializer() {
        return this.data.initializer;
    }

    public TranslatorId getTranslatorId() {
        return this.data.translatorId;
    }

    public Set<TranslatorId> getTranslatorDependencies() {
        return this.data.dependencies;
    }

}
