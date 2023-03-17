package gov.hhs.aspr.gcm.translation.core;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;

public abstract class AEnumTranslatorSpec<I extends ProtocolMessageEnum, S> implements ITranslatorSpec {
    protected TranslatorCore translator;
    private boolean initialized = false;

    public void init(TranslatorCore translator) {
        this.translator = translator;
        this.initialized = true;
    }

    public interface EnumInstance {
        ProtocolMessageEnum getFromString(String string);
    }

    protected void checkInit() {
        if (!this.initialized) {
            throw new RuntimeException("Translator not initialized. Did you forget to call init()?");
        }
    }

    public <T> T convert(Message message) {
        throw new RuntimeException("Tried to convert an message on a enummessage translator");
    }
    
    @SuppressWarnings("unchecked")
    public I convert(Object obj) {
        checkInit();
        
        if (!(this.getSimObjectClass().isAssignableFrom(obj.getClass()))) {
            throw new RuntimeException("Object is not a " + this.getSimObjectClass().getName());
        }
        
        return this.convertSimObject((S) obj);
    }
    
    @SuppressWarnings("unchecked")
    public S convert(ProtocolMessageEnum protocolMessageEnum) {
        checkInit();
        if (protocolMessageEnum.getDescriptorForType() != this.getDescriptorForInputObject()) {
            throw new RuntimeException("Message is not a " + this.getDescriptorForInputObject().getName());
        }

        return this.convertInputObject((I) protocolMessageEnum);
    }

    protected abstract S convertInputObject(I inputObject);

    protected abstract I convertSimObject(S simObject);

    public abstract EnumDescriptor getDescriptorForInputObject();

    public abstract EnumInstance getEnumInstance();

    public abstract Class<S> getSimObjectClass();

    public abstract Class<I> getInputObjectClass();
}
