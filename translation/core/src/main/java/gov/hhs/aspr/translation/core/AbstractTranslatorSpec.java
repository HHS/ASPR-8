package gov.hhs.aspr.translation.core;

public abstract class AbstractTranslatorSpec<I, S> implements ITranslatorSpec {
    private boolean initialized = false;

    public <T extends TranslatorCore> void init(T translator) {
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

        if ((this.getAppObjectClass().isAssignableFrom(obj.getClass()))) {
            return (T) this.convertAppObject((S) obj);
        }

        if ((this.getInputObjectClass().isAssignableFrom(obj.getClass()))) {
            return (T) this.convertInputObject((I) obj);
        }
        throw new RuntimeException("Object is not a " + this.getAppObjectClass().getName() + " and it is not a "
                + this.getInputObjectClass().getName());

    }

    protected abstract S convertInputObject(I inputObject);

    protected abstract I convertAppObject(S simObject);

    public abstract Class<S> getAppObjectClass();

    public abstract Class<I> getInputObjectClass();
}