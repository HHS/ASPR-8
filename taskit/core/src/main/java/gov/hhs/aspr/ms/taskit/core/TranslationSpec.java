package gov.hhs.aspr.ms.taskit.core;

import java.util.Objects;

import util.errors.ContractException;

/**
 * Core implementation of the {@link BaseTranslationSpec} that must be
 * implemented by each needed translationSpec.
 * 
 * <li>Note: No reference to a {@link TranslationEngine} exists in this class,
 * and must be implemented by the implementing class.
 */
public abstract class TranslationSpec<I, A> implements BaseTranslationSpec {
    private boolean initialized = false;

    /**
     * Initializes this translationSpec.
     * 
     * All child TranslationSpecs must call super() otherwise there will be an
     * exception throw in the TranslationEngine
     */
    public <T extends TranslationEngine> void init(T translationEngine) {
        this.initialized = true;
    }

    protected void checkInit() {
        if (!this.initialized) {
            throw new ContractException(CoreTranslationError.UNITIALIZED_TRANSLATION_SPEC);
        }
    }

    /**
     * Returns the initialization state of this TranslationSpec
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    /**
     * The implementation of the {@link BaseTranslationSpec#convert(Object)} method
     * 
     * Given the object, determines which method should be called.
     * 
     * <li>It first checks if the object class is exactly equal to either the App or
     * Input Class and if so, calls the related method
     * <li>It then checks if the the object class is assinable from either the App
     * or Input Class and if so, calls the related method
     * <li>If no match can be found, an exception is throw
     * 
     * @param <T> the expected return type after translation/coversion
     * 
     * @throws ContractException
     *                           <ul>
     *                           <li>{@linkplain CoreTranslationError#UNKNOWN_OBJECT}
     *                           if no match can be found between the passed in
     *                           object and the given appClass and InputClass
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(Object obj) {
        checkInit();

        if ((this.getAppObjectClass() == obj.getClass())) {
            return (T) this.convertAppObject((A) obj);
        }

        if ((this.getInputObjectClass() == obj.getClass())) {
            return (T) this.convertInputObject((I) obj);
        }

        if ((this.getAppObjectClass().isAssignableFrom(obj.getClass()))) {
            return (T) this.convertAppObject((A) obj);
        }

        if ((this.getInputObjectClass().isAssignableFrom(obj.getClass()))) {
            return (T) this.convertInputObject((I) obj);
        }

        throw new ContractException(CoreTranslationError.UNKNOWN_OBJECT,
                "Object is not a " + this.getAppObjectClass().getName() + " and it is not a "
                        + this.getInputObjectClass().getName());

    }

    @Override
    public int hashCode() {
        return Objects.hash(initialized, getAppObjectClass(), getInputObjectClass());
    }

    @Override
    public boolean equals(Object obj) {
        // if same object, then equal
        if (this == obj) {
            return true;
        }
        // if obj is null, not equal
        if (obj == null) {
            return false;
        }
        // if obj is not an instance of TranslationSpec, not equal
        if (!(obj instanceof TranslationSpec)) {
            return false;
        }

        @SuppressWarnings("rawtypes")
        TranslationSpec other = (TranslationSpec) obj;

        // if different app class, not equal
        if (getAppObjectClass() != other.getAppObjectClass()) {
            return false;
        }

        // if different intput class, not equal
        if (getInputObjectClass() != other.getInputObjectClass()) {
            return false;
        }

        // if not both initialized, not equal
        return initialized == other.initialized;
    }

    /**
     * Given an inputObject, converts it to it's appObject equivalent
     */
    protected abstract A convertInputObject(I inputObject);

    /**
     * Given an appObject, converts it to it's inputObject equivalent
     */
    protected abstract I convertAppObject(A appObject);

    /**
     * Returns the class of the app object
     */
    public abstract Class<A> getAppObjectClass();

    /**
     * Returns the class of the input object
     */
    public abstract Class<I> getInputObjectClass();
}
