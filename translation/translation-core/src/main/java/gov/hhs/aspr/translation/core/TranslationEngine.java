package gov.hhs.aspr.translation.core;

import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import util.errors.ContractException;

/**
 * Main Translator Class
 * 
 * Initializes all {@link TranslationSpec}s and maintains a mapping between the
 * translationSpec and it's respective classes
 * 
 * This is an Abstract class, meaning that for a given translation library
 * (Fasterxml, Protobuf, etc) must have a custom implemented TranslationEngine
 * 
 */
public abstract class TranslationEngine {

    private final Data data;
    protected boolean debug = false;
    protected boolean isInitialized = false;

    protected TranslationEngine(Data data) {
        this.data = data;
    }

    protected static class Data {
        protected final Map<Class<?>, BaseTranslationSpec> classToTranslationSpecMap = new LinkedHashMap<>();
        protected final Set<BaseTranslationSpec> translationSpecs = new LinkedHashSet<>();

        protected Data() {
        }
    }

    public static class Builder {
        protected Data data;

        protected Builder(Data data) {
            this.data = data;
        }

        private <I, A> void validateTranslatorSpec(TranslationSpec<I, A> translationSpec) {
            if (translationSpec == null) {
                throw new ContractException(CoreTranslationError.NULL_TRANSLATION_SPEC);
            }

            if (translationSpec.getAppObjectClass() == null) {
                throw new ContractException(CoreTranslationError.NULL_TRANSLATION_SPEC_APP_CLASS);
            }

            if (translationSpec.getInputObjectClass() == null) {
                throw new ContractException(CoreTranslationError.NULL_TRANSLATION_SPEC_INPUT_CLASS);
            }

            if (this.data.translationSpecs.contains(translationSpec)) {
                throw new ContractException(CoreTranslationError.DUPLICATE_TRANSLATION_SPEC);
            }
        }

        /**
         * Builder for the TranslationEngine
         * 
         * <li>Note: Calling this specific method will result in a RuntimeException
         * 
         * @throws RuntimeException
         *                          <li>If this method is called directly. You should
         *                          istead be calling the child method in the child
         *                          TranslationEngine that extends this class
         */
        public TranslationEngine build() {
            throw new RuntimeException("Tried to call build on abstract Translator Core");
        }

        /**
         * Adds the given {@link TranslationSpec} to the internal
         * classToTranslatorSpecMap
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATION_SPEC}
         *                           if the given translationSpec is null</li>
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATION_SPEC_APP_CLASS}
         *                           if the given translatorSpecs getAppClass method
         *                           returns null</li>
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATION_SPEC_INPUT_CLASS}
         *                           if the given translatorSpecs getInputClass method
         *                           returns null</li>
         *                           <li>{@linkplain CoreTranslationError#DUPLICATE_TRANSLATION_SPEC}
         *                           if the given translationSpec is already known</li>
         * 
         * @param <I> the input object type
         * @param <A> the app object type
         */
        public <I, A> Builder addTranslatorSpec(TranslationSpec<I, A> translationSpec) {
            validateTranslatorSpec(translationSpec);

            this.data.classToTranslationSpecMap.put(translationSpec.getInputObjectClass(),
                    translationSpec);
            this.data.classToTranslationSpecMap.put(translationSpec.getAppObjectClass(), translationSpec);

            this.data.translationSpecs.add(translationSpec);

            return this;
        }
    }

    /**
     * Returns a new instance of Builder
     */
    public static Builder builder() {
        return new Builder(new Data());
    }

    /**
     * Initializes the translationEngine by calling init on each translationSpec
     * added
     * in the builder
     */
    public void init() {
        this.data.translationSpecs.forEach((translationSpec) -> translationSpec.init(this));

        this.isInitialized = true;
    }

    /**
     * returns whether this translationEngine is initialized or not
     */
    public boolean isInitialized() {
        return this.isInitialized;
    }

    //

    /**
     * checks to verify all the translatorSpecs have been initialized.
     * 
     * @throws RuntimeException
     *                          <li>There should not be a case where all
     *                          translatorSpecs are initialized, so if one of them
     *                          isn't, something went very wrong.
     */
    protected void translationSpecsAreInitialized() {

        for (BaseTranslationSpec translationSpec : this.data.translationSpecs) {
            if (!translationSpec.isInitialized()) {
                throw new RuntimeException(translationSpec.getClass().getName()
                        + " was not properly initialized, be sure to call super()");
            }
        }

    }

    /**
     * abstract method that must be implemented by child TranslatorCores that
     * defines how to write to output files
     */
    public abstract <U, M extends U> void writeOutput(Writer writer, M appObject, Optional<Class<U>> superClass);

    /**
     * abstract method that must be implemented by child TranslatorCores that
     * defines how to read from input files
     */
    public abstract <T, U> T readInput(Reader reader, Class<U> inputClassRef);

    /**
     * Given an object, uses the class of the object to obtain the translationSpec
     * and then calls {@link TranslationSpec#convert(Object)}
     * <li>this conversion method will be used approx ~90% of the
     * time
     * 
     * @param <T> the return type after converting
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATION_SPEC}
     *                           if no translationSpec was provided for the given
     *                           objects class
     */
    public <T> T convertObject(Object object) {
        return getTranslationSpecForClass(object.getClass()).convert(object);
    }

    /**
     * Given an object, uses the parent class of the object to obtain the
     * translationSpec
     * and then calls {@link TranslationSpec#convert(Object)}
     * <li>This method call is safe in the sense that the type parameters ensure
     * that the passed in object is actually a child of the passed in parentClassRef
     * <li>this conversion method will be used approx ~7% of the
     * time
     * 
     * @param <T> the return type after converting
     * @param <M> the type of the object; extends U
     * @param <U> the parent type of the object and the class for which
     *            translationSpec you want to use
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATION_SPEC}
     *                           if no translationSpec was provided for the given
     *                           objects class
     */
    public <T, M extends U, U> T convertObjectAsSafeClass(M object, Class<U> parentClassRef) {
        return getTranslationSpecForClass(parentClassRef).convert(object);
    }

    /**
     * Given an object, uses the passed in class to obtain the
     * translationSpec
     * and then calls {@link TranslationSpec#convert(Object)}
     * <li>This method call is unsafe in the sense that the type parameters do not
     * ensure
     * any relationship between the passed in object and the passed in classRef.
     * <li>A common use case for using this conversion method would be to call a
     * translationSpec that will wrap the given object in another object.
     * 
     * <li>this conversion method will be used approx ~3% of the
     * time
     * 
     * @param <T> the return type after converting
     * @param <M> the type of the object
     * @param <U> the type of the class for which translationSpec you want to use
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATION_SPEC}
     *                           if no translationSpec was provided for the given
     *                           objects class
     */
    public <T, M, U> T convertObjectAsUnsafeClass(M object, Class<U> objectClassRef) {
        return getTranslationSpecForClass(objectClassRef).convert(object);
    }

    /**
     * Given a classRef, returns the translationSpec associated with that class, if
     * it is known
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATION_SPEC}
     *                           if no translationSpec for the given class was found
     */
    protected BaseTranslationSpec getTranslationSpecForClass(Class<?> classRef) {
        if (this.data.classToTranslationSpecMap.containsKey(classRef)) {
            return this.data.classToTranslationSpecMap.get(classRef);
        }
        throw new ContractException(CoreTranslationError.UNKNOWN_TRANSLATION_SPEC, classRef.getName());
    }

}
