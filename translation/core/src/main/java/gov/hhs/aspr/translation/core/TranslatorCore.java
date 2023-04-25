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
 * translatorSpec and it's respective classes
 * 
 * This is an Abstract class, meaning that for a given translation library
 * (Fasterxml, Protobuf, etc) must have a custom implemented TranslatorCore
 * 
 */
public abstract class TranslatorCore {

    private final Data data;
    protected boolean debug = false;
    protected boolean isInitialized = false;

    protected TranslatorCore(Data data) {
        this.data = data;
    }

    protected static class Data {
        protected final Map<Class<?>, BaseTranslationSpec> classToTranslatorSpecMap = new LinkedHashMap<>();
        protected final Set<BaseTranslationSpec> translatorSpecs = new LinkedHashSet<>();

        protected Data() {
        }
    }

    protected static class Builder {
        protected Data data;

        protected Builder(Data data) {
            this.data = data;
        }

        private <I, A> void validateTranslatorSpec(TranslationSpec<I, A> translatorSpec) {
            if (translatorSpec == null) {
                throw new ContractException(CoreTranslationError.NULL_TRANSLATOR_SPEC);
            }

            if (translatorSpec.getAppObjectClass() == null) {
                throw new ContractException(CoreTranslationError.NULL_TRANSLATOR_SPEC_APP_CLASS);
            }

            if (translatorSpec.getInputObjectClass() == null) {
                throw new ContractException(CoreTranslationError.NULL_TRANSLATOR_SPEC_INPUT_CLASS);
            }

            if (this.data.translatorSpecs.contains(translatorSpec)) {
                throw new ContractException(CoreTranslationError.DUPLICATE_TRANSLATOR_SPEC);
            }
        }

        /**
         * Builder for the TranslatorCore
         * 
         * <li>Note: Calling this specific method will result in a RuntimeException
         * 
         * @throws RuntimeException
         *                          <li>If this method is called directly. You should
         *                          istead be calling the child method in the child
         *                          TranslatorCore that extends this class
         */
        public TranslatorCore build() {
            throw new RuntimeException("Tried to call build on abstract Translator Core");
        }

        /**
         * Adds the given {@link TranslationSpec} to the internal
         * classToTranslatorSpecMap
         * 
         * @throws ContractException
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATOR_SPEC}
         *                           if the given translatorSpec is null</li>
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATOR_SPEC_APP_CLASS}
         *                           if the given translatorSpecs getAppClass method
         *                           returns null</li>
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATOR_SPEC_INPUT_CLASS}
         *                           if the given translatorSpecs getInputClass method
         *                           returns null</li>
         *                           <li>{@linkplain CoreTranslationError#DUPLICATE_TRANSLATOR_SPEC}
         *                           if the given translatorSpec is already known</li>
         * 
         * @param <I> the input object type
         * @param <A> the app object type
         */
        public <I, A> Builder addTranslatorSpec(TranslationSpec<I, A> translatorSpec) {
            validateTranslatorSpec(translatorSpec);

            this.data.classToTranslatorSpecMap.put(translatorSpec.getInputObjectClass(),
                    translatorSpec);
            this.data.classToTranslatorSpecMap.put(translatorSpec.getAppObjectClass(), translatorSpec);

            this.data.translatorSpecs.add(translatorSpec);

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
     * Initializes the translatorCore by calling init on each translatorSpec added
     * in the builder
     */
    public void init() {
        this.data.translatorSpecs.forEach((translatorSpec) -> translatorSpec.init(this));

        this.isInitialized = true;
    }

    /**
     * returns whether this translatorCore is initialized or not
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
    void translatorSpecsAreInitialized() {

        for (BaseTranslationSpec translatorSpec : this.data.translatorSpecs) {
            if (!translatorSpec.isInitialized()) {
                throw new RuntimeException("TranslatoSpec class was not properly initialized, be sure to call super()");
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
     * Given an object, uses the class of the object to obtain the translatorSpec
     * and then calls {@link TranslationSpec#convert(Object)}
     * <li>this conversion method will be used approx ~90% of the
     * time
     * 
     * @param <T> the return type after converting
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATOR_SPEC}
     *                           if no translatorSpec was provided for the given
     *                           objects class
     */
    public <T> T convertObject(Object object) {
        return getTranslatorForClass(object.getClass()).convert(object);
    }

    /**
     * Given an object, uses the parent class of the object to obtain the
     * translatorSpec
     * and then calls {@link TranslationSpec#convert(Object)}
     * <li>This method call is safe in the sense that the type parameters ensure
     * that the passed in object is actually a child of the passed in parentClassRef
     * <li>this conversion method will be used approx ~7% of the
     * time
     * 
     * @param <T> the return type after converting
     * @param <M> the type of the object; extends U
     * @param <U> the parent type of the object and the class for which
     *            translatorSpec you want to use
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATOR_SPEC}
     *                           if no translatorSpec was provided for the given
     *                           objects class
     */
    public <T, M extends U, U> T convertObjectAsSafeClass(M object, Class<U> parentClassRef) {
        return getTranslatorForClass(parentClassRef).convert(object);
    }

    /**
     * Given an object, uses the passed in class to obtain the
     * translatorSpec
     * and then calls {@link TranslationSpec#convert(Object)}
     * <li>This method call is unsafe in the sense that the type parameters do not
     * ensure
     * any relationship between the passed in object and the passed in classRef.
     * <li>A common use case for using this conversion method would be to call a
     * translatorSpec that will wrap the given object in another object.
     * 
     * <li>this conversion method will be used approx ~3% of the
     * time
     * 
     * @param <T> the return type after converting
     * @param <M> the type of the object
     * @param <U> the type of the class for which translatorSpec you want to use
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATOR_SPEC}
     *                           if no translatorSpec was provided for the given
     *                           objects class
     */
    public <T, M, U> T convertObjectAsUnsafeClass(M object, Class<U> objectClassRef) {
        return getTranslatorForClass(objectClassRef).convert(object);
    }

    /**
     * Given a classRef, returns the translatorSpec associated with that class, if
     * it is known
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATOR_SPEC}
     *                           if no translatorSpec for the given class was found
     */
    protected BaseTranslationSpec getTranslatorForClass(Class<?> classRef) {
        if (this.data.classToTranslatorSpecMap.containsKey(classRef)) {
            return this.data.classToTranslatorSpecMap.get(classRef);
        }
        throw new ContractException(CoreTranslationError.UNKNOWN_TRANSLATOR_SPEC, classRef.getName());
    }

}
