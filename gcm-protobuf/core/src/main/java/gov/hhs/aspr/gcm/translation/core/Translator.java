package gov.hhs.aspr.gcm.translation.core;

import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.Descriptors.Descriptor;

public abstract class Translator<I extends Message, S> implements ITranslator {
    protected TranslatorCore translator;
    private boolean initialized = false;

    public void init(TranslatorCore translator) {
        this.translator = translator;
        this.initialized = true;
    }

    protected void checkInit() {
        if (!this.initialized) {
            throw new RuntimeException("Translator not initialized. Did you forget to call init()?");
        }
    }

    @SuppressWarnings("unchecked")
    public S convert(Message message) {
        checkInit();
        if (message.getDescriptorForType() != this.getDescriptorForInputObject()) {
            throw new RuntimeException("Message is not a " + this.getDescriptorForInputObject().getName());
        }

        return this.convertInputObject((I) message);
    }

    @SuppressWarnings("unchecked")
    public I convert(Object obj) {
        checkInit();

        if (!(this.getSimObjectClass().isAssignableFrom(obj.getClass()))) {
            throw new RuntimeException("Object is not a " + this.getSimObjectClass().getName());
        }

        return this.convertSimObject((S) obj);
    }

    public <T> T convert(ProtocolMessageEnum protocolMessageEnum) {
        throw new RuntimeException("Tried to convert an enummessage on a message translator");
    }

    protected abstract S convertInputObject(I inputObject);

    protected abstract I convertSimObject(S simObject);

    public abstract Descriptor getDescriptorForInputObject();

    public abstract I getDefaultInstanceForInputObject();

    public abstract Class<S> getSimObjectClass();

    public abstract Class<I> getInputObjectClass();
}
