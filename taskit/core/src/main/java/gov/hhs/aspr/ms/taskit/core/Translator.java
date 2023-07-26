package gov.hhs.aspr.ms.taskit.core;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import util.errors.ContractException;

/**
 * The Translator class serves as a wrapper around one or more
 * {@link BaseTranslationSpec}(s)
 * 
 * and assists in adding those translationSpecs to the {@link TranslationEngine}
 */
public final class Translator {
    private final Data data;

    private Translator(Data data) {
        this.data = data;
    }

    protected static class Data {
        private TranslatorId translatorId;
        private Consumer<TranslatorContext> initializer;
        private final Set<TranslatorId> dependencies = new LinkedHashSet<>();

        protected Data() {
        }

        @Override
        public int hashCode() {
            return Objects.hash(translatorId, dependencies);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Data other = (Data) obj;
            return Objects.equals(translatorId, other.translatorId) && Objects.equals(dependencies, other.dependencies);
        }

    }

    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        private void validate() {
            if (this.data.translatorId == null) {
                throw new ContractException(CoreTranslationError.NULL_TRANSLATOR_ID);
            }
            if (this.data.initializer == null) {
                throw new ContractException(CoreTranslationError.NULL_INIT_CONSUMER);
            }
        }

        /**
         * Builds the Translator
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATOR_ID}
         *                           if the translatorId was not set</li>
         *                           <li>{@linkplain CoreTranslationError#NULL_INIT_CONSUMER}
         *                           if the initConsumer was not set</li>
         */
        public Translator build() {
            validate();

            return new Translator(data);
        }

        /**
         * Sets the translatorId
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATOR_ID}
         *                           if the translatorId is null</li>
         */
        public Builder setTranslatorId(TranslatorId translatorId) {
            if (translatorId == null) {
                throw new ContractException(CoreTranslationError.NULL_TRANSLATOR_ID);
            }

            this.data.translatorId = translatorId;

            return this;
        }

        /**
         * Sets the initialization callback for the translator
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_INIT_CONSUMER}
         *                           if the initConsumer is null</li>
         */
        public Builder setInitializer(Consumer<TranslatorContext> initConsumer) {
            if (initConsumer == null) {
                throw new ContractException(CoreTranslationError.NULL_INIT_CONSUMER);
            }

            this.data.initializer = initConsumer;

            return this;
        }

        /**
         * Adds the given TranslatorId as a dependency for this Translator
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_DEPENDENCY}
         *                           if the dependecy is null</li>
         *                           <li>{@linkplain CoreTranslationError#DUPLICATE_DEPENDENCY}
         *                           if the dependecy has already been added</li>
         */
        public Builder addDependency(TranslatorId dependency) {
            if (dependency == null) {
                throw new ContractException(CoreTranslationError.NULL_DEPENDENCY);
            }

            if (this.data.dependencies.contains(dependency)) {
                throw new ContractException(CoreTranslationError.DUPLICATE_DEPENDENCY);
            }

            this.data.dependencies.add(dependency);

            return this;
        }

    }

    /**
     * Creates a new Builder for a Translator
     */
    public static Builder builder() {
        return new Builder(new Data());
    }

    /**
     * Returns the Initialization Consumer
     */
    public Consumer<TranslatorContext> getInitializer() {
        return this.data.initializer;
    }

    /**
     * Returns the TranslatorId
     */
    public TranslatorId getTranslatorId() {
        return this.data.translatorId;
    }

    /**
     * Returns the set of Dependencies
     */
    public Set<TranslatorId> getTranslatorDependencies() {
        return this.data.dependencies;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Translator other = (Translator) obj;
        // Objects.equals will automatically return if a == b, so not need for check
        return Objects.equals(data, other.data);
    }

}
