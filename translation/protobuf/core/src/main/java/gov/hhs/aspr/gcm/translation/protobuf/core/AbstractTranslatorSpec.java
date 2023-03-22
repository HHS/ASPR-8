package gov.hhs.aspr.gcm.translation.protobuf.core;

public abstract class AbstractTranslatorSpec<I, S> implements ITranslatorSpec {
    protected TranslatorCore translator;
    private boolean initialized = false;

    public void init(TranslatorCore translator) {
        this.translator = translator;
        this.initialized = true;
    }

    protected void checkInit() {
        if (!this.initialized) {
            throw new RuntimeException("Translator not initialized.");
        }
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

    public abstract I getDefaultInstanceForInputObject();

    public abstract Class<S> getAppObjectClass();

    public abstract Class<I> getInputObjectClass();
}
