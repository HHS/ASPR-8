package gov.hhs.aspr.gcm.translation.core;

import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class TranslatorCore {

    private final TranslatorCoreData data;
    protected boolean debug = false;
    protected boolean isInitialized = false;

    protected TranslatorCore(TranslatorCoreData data) {
        this.data = data;
    }

    protected static class TranslatorCoreData {
        public final Map<Class<?>, ITranslatorSpec> classToTranslatorSpecMap = new LinkedHashMap<>();
        public final Set<ITranslatorSpec> translatorSpecs = new LinkedHashSet<>();

        protected TranslatorCoreData() {
        }
    }

    public static class Builder {
        protected TranslatorCoreData data;

        protected Builder(TranslatorCoreData data) {
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
        return new Builder(new TranslatorCoreData());
    }

    public void init() {
        this.data.translatorSpecs.forEach((translatorSpec) -> translatorSpec.init(this));

        this.isInitialized = true;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    public abstract <M extends U, U> void writeJson(Writer writer, M simObject, Optional<Class<U>> superClass);

    public abstract <T, U> T readJson(Reader reader, Class<U> inputClassRef);

    public <T extends U, U, E extends Enum<E>> T convertInputEnum(Enum<E> inputEnum, Class<U> superClass) {
        T convertedEnum = convertInputEnum(inputEnum);

        // verify translated object can be casted to the super class
        superClassCastCheck(convertedEnum, superClass);

        return convertedEnum;
    }

    public <T, E extends Enum<E>> T convertInputEnum(Enum<E> inputEnum) {
        return getTranslatorForClass(inputEnum.getClass()).convert(inputEnum);
    }

    public <T extends U, U> T convertInputObject(Object inputObject, Class<U> superClass) {

        T convertInputObject = convertInputObject(inputObject);

        // verify translated object can be casted to the super class
        superClassCastCheck(convertInputObject, superClass);

        return convertInputObject;
    }

    public <T> T convertInputObject(Object inputObject) {
        return getTranslatorForClass(inputObject.getClass()).convert(inputObject);
    }

    public <T, M extends U, U> T convertSimObject(M simObject, Class<U> superClass) {

        superClassCastCheck(simObject, superClass);

        return getTranslatorForClass(superClass).convert(simObject);
    }

    public <T> T convertSimObject(Object simObject) {
        return getTranslatorForClass(simObject.getClass()).convert(simObject);
    }

    protected <U, M extends U> void superClassCastCheck(M object, Class<U> superClass) {
        validateObjectNotNull(object);

        try {
            // verify object can be casted to the super class
            superClass.cast(object);
        } catch (ClassCastException e) {
            throw new RuntimeException("Unable to cast:" + object.getClass() + " to: " + superClass, e);
        }
    }

    private void validateObjectNotNull(Object object) {
        if (object == null) {
            throw new RuntimeException("Object is null");
        }
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
