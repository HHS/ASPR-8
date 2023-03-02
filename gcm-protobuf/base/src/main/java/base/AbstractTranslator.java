package base;

import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.Descriptor;

public abstract class AbstractTranslator<I extends Message, S> implements ITranslator {
    protected MasterTranslator translator;
    private boolean initialized = false;

    public void init(MasterTranslator translator) {
        this.translator = translator;
        this.initialized = true;
    }

    protected void checkInit() {
        if (!this.initialized) {
            throw new RuntimeException("Translator not initialized. Did you forget to call init()?");
        }
    }

    @SuppressWarnings("unchecked")
    public Object convert(Message message) {
        checkInit();
        if (message.getDescriptorForType() != this.getDescriptorForInputObject()) {
            throw new RuntimeException("Message is not a " + this.getDescriptorForInputObject().getName());
        }

        return this.convertInputObject((I) message);
    }

    @SuppressWarnings("unchecked")
    public Message convert(Object obj) {
        checkInit();

        if (!(this.getSimObjectClass().isAssignableFrom(obj.getClass()))) {
            throw new RuntimeException("Object is not a " + this.getSimObjectClass().getName());
        }
        
        return this.convertSimObject((S) obj);
    }

    protected abstract S convertInputObject(I inputObject);

    protected abstract I convertSimObject(S simObject);

    public abstract Descriptor getDescriptorForInputObject();

    public abstract I getDefaultInstanceForInputObject();

    public abstract Class<S> getSimObjectClass();

    public abstract Class<I> getInputObjectClass();
}
