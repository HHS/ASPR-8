package gov.hhs.aspr.translation.core;

import java.util.Objects;

public abstract class TranslatorSpec<I, S> implements ITranslatorSpec {
    private boolean initialized = false;

    public <T extends TranslatorCore> void init(T translatorCore) {
        this.initialized = true;
    }

    protected void checkInit() {
        if (!this.initialized) {
            throw new RuntimeException("Translator not initialized.");
        }
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(Object obj) {
        checkInit();

        if ((this.getAppObjectClass() == obj.getClass())) {
            return (T) this.convertAppObject((S) obj);
        }

        if ((this.getInputObjectClass() == obj.getClass())) {
            return (T) this.convertInputObject((I) obj);
        }

        if ((this.getAppObjectClass().isAssignableFrom(obj.getClass()))) {
            return (T) this.convertAppObject((S) obj);
        }

        if ((this.getInputObjectClass().isAssignableFrom(obj.getClass()))) {
            return (T) this.convertInputObject((I) obj);
        }

        throw new RuntimeException("Object is not a " + this.getAppObjectClass().getName() + " and it is not a "
                + this.getInputObjectClass().getName());

    }

    @Override
    public int hashCode() {
        return Objects.hash(initialized);
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

        @SuppressWarnings("rawtypes")
        TranslatorSpec other = (TranslatorSpec) obj;

        if (getAppObjectClass() == other.getAppObjectClass()) {
            return false;
        }

        if (getInputObjectClass() == other.getInputObjectClass()) {
            return false;
        }

        return initialized == other.initialized;
    }

    protected abstract S convertInputObject(I inputObject);

    protected abstract I convertAppObject(S appObject);

    public abstract Class<S> getAppObjectClass();

    public abstract Class<I> getInputObjectClass();
}
