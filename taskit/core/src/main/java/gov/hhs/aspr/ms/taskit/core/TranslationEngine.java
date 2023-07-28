package gov.hhs.aspr.ms.taskit.core;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        @Override
        public int hashCode() {
            return Objects.hash(classToTranslationSpecMap, translationSpecs);
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

            if (!Objects.equals(classToTranslationSpecMap, other.classToTranslationSpecMap)) {
                return false;
            }

            if (!Objects.equals(translationSpecs, other.translationSpecs)) {
                return false;
            }

            return true;

        }

    }

    public static class Builder {
        protected Data data;

        protected Builder(Data data) {
            this.data = data;
        }

        private <I, A> void validateTranslationSpec(TranslationSpec<I, A> translationSpec) {
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
         *                          If this method is called directly. You should
         *                          instead be calling the child method in the child
         *                          TranslationEngine that extends this class
         */
        public TranslationEngine build() {
            throw new RuntimeException("Tried to call build on abstract Translation Engine");
        }

        /**
         * Adds the given {@link TranslationSpec} to the internal
         * classToTranslationSpecMap
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATION_SPEC}
         *                           if the given translationSpec is null</li>
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATION_SPEC_APP_CLASS}
         *                           if the given translationSpecs getAppClass method
         *                           returns null</li>
         *                           <li>{@linkplain CoreTranslationError#NULL_TRANSLATION_SPEC_INPUT_CLASS}
         *                           if the given translationSpecs getInputClass method
         *                           returns null</li>
         *                           <li>{@linkplain CoreTranslationError#DUPLICATE_TRANSLATION_SPEC}
         *                           if the given translationSpec is already known</li>
         * 
         * @param <I> the input object type
         * @param <A> the app object type
         */
        public <I, A> Builder addTranslationSpec(TranslationSpec<I, A> translationSpec) {
            validateTranslationSpec(translationSpec);

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
        /*
         * Calling init on a translationSpec causes the hashCode of the translationSpec
         * to change.
         * Because of this, before calling init, we need to remove them from the
         * translationSpecs Set
         * then initialize them, then add them back to the set. Set's aren't happy when
         * the hash code of the objects in them change
         */
        List<BaseTranslationSpec> copyOfTranslationSpecs = new ArrayList<>(this.data.translationSpecs);

        this.data.translationSpecs.clear();

        for (BaseTranslationSpec translationSpec : copyOfTranslationSpecs) {
            translationSpec.init(this);
            this.data.translationSpecs.add(translationSpec);
        }

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
     * checks to verify all the translationSpecs have been initialized.
     * 
     * @throws RuntimeException
     *                          There should not be a case where all
     *                          translationSpecs are initialized, so if one of them
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

    public Set<BaseTranslationSpec> getTranslationSpecs() {
        return this.data.translationSpecs;
    }

    /**
     * abstract method that must be implemented by child TranslatorCores that
     * defines how to write to output files
     */
    protected abstract <U, M extends U> void writeOutput(Writer writer, M appObject, Optional<Class<U>> superClass);

    /**
     * abstract method that must be implemented by child TranslatorCores that
     * defines how to read from input files
     */
    protected abstract <T, U> T readInput(Reader reader, Class<U> inputClassRef);

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
     *                           <li>{@linkplain CoreTranslationError#NULL_OBJECT_FOR_TRANSLATION}
     *                           if the passed in object is null
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATION_SPEC}
     *                           if no translationSpec was provided for the given
     *                           objects class
     */
    public <T> T convertObject(Object object) {
        if (object == null) {
            throw new ContractException(CoreTranslationError.NULL_OBJECT_FOR_TRANSLATION);
        }
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
     *                           <li>{@linkplain CoreTranslationError#NULL_OBJECT_FOR_TRANSLATION}
     *                           if the passed in object is null
     *                           <li>{@linkplain CoreTranslationError#NULL_CLASS_REF}
     *                           if the passed in parentClassRef is null
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATION_SPEC}
     *                           if no translationSpec was provided for the given
     *                           objects class
     */
    public <T, M extends U, U> T convertObjectAsSafeClass(M object, Class<U> parentClassRef) {
        if (object == null) {
            throw new ContractException(CoreTranslationError.NULL_OBJECT_FOR_TRANSLATION);
        }

        if (parentClassRef == null) {
            throw new ContractException(CoreTranslationError.NULL_CLASS_REF);
        }

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
     *                           <li>{@linkplain CoreTranslationError#NULL_OBJECT_FOR_TRANSLATION}
     *                           if the passed in object is null
     *                           <li>{@linkplain CoreTranslationError#NULL_CLASS_REF}
     *                           if the passed in objectClassRef is null
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_TRANSLATION_SPEC}
     *                           if no translationSpec was provided for the given
     *                           objects class
     */
    public <T, M, U> T convertObjectAsUnsafeClass(M object, Class<U> objectClassRef) {
        if (object == null) {
            throw new ContractException(CoreTranslationError.NULL_OBJECT_FOR_TRANSLATION);
        }

        if (objectClassRef == null) {
            throw new ContractException(CoreTranslationError.NULL_CLASS_REF);
        }

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

    @Override
    public int hashCode() {
        return Objects.hash(data, isInitialized);
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
        TranslationEngine other = (TranslationEngine) obj;

        if (isInitialized != other.isInitialized) {
            return false;
        }
        return Objects.equals(data, other.data);
    }

}
