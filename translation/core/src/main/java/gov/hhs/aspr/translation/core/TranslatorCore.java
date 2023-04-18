package gov.hhs.aspr.translation.core;

import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class TranslatorCore {

    private final Data data;
    protected boolean debug = false;
    protected boolean isInitialized = false;

    protected TranslatorCore(Data data) {
        this.data = data;
    }

    protected static class Data {
        protected final Map<Class<?>, ITranslatorSpec> classToTranslatorSpecMap = new LinkedHashMap<>();
        protected final Set<ITranslatorSpec> translatorSpecs = new LinkedHashSet<>();

        protected Data() {
        }
    }

    public static class Builder {
        protected Data data;

        protected Builder(Data data) {
            this.data = data;
        }

        public TranslatorCore build() {
            throw new RuntimeException("Tried to call build on abstract Translator Core");
        }

        public <I, S> Builder addTranslatorSpec(AbstractTranslatorSpec<I, S> translatorSpec) {
            this.data.classToTranslatorSpecMap.putIfAbsent(translatorSpec.getInputObjectClass(),
                    translatorSpec);
            this.data.classToTranslatorSpecMap.putIfAbsent(translatorSpec.getAppObjectClass(), translatorSpec);

            this.data.translatorSpecs.add(translatorSpec);

            return this;
        }
    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    public void init() {
        this.data.translatorSpecs.forEach((translatorSpec) -> translatorSpec.init(this));

        this.isInitialized = true;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    void translatorSpecsAreInitialized() {

        for (ITranslatorSpec translatorSpec : this.data.translatorSpecs) {
            if (!translatorSpec.isInitialized()) {
                throw new RuntimeException("TranslatoSpec class was not properly initialized, be sure to call super()");
            }
        }

    }

    public abstract <M extends U, U> void writeOutput(Writer writer, M simObject, Optional<Class<U>> superClass);

    public abstract <T, U> T readInput(Reader reader, Class<U> inputClassRef);

    public <T extends U, U> T convertInputObject(Object inputObject, Class<U> superClass) {
        T convertInputObject = convertInputObject(inputObject);

        return convertInputObject;
    }

    public <T> T convertInputObject(Object inputObject) {
        return getTranslatorForClass(inputObject.getClass()).convert(inputObject);
    }

    public <T, M extends U, U> T convertSimObject(M simObject, Class<U> superClass) {
        return getTranslatorForClass(superClass).convert(simObject);
    }

    public <T> T convertSimObject(Object simObject) {
        return getTranslatorForClass(simObject.getClass()).convert(simObject);
    }

    protected ITranslatorSpec getTranslatorForClass(Class<?> classRef) {
        if (this.data.classToTranslatorSpecMap.containsKey(classRef)) {
            return this.data.classToTranslatorSpecMap.get(classRef);
        }
        throw new RuntimeException(
                "No TranslatorSpec was provided for message type: " + classRef.getName());
    }

    public Builder getCloneBuilder() {
        return new Builder(data);
    }

}
