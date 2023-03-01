package base.translators;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.UInt32Value;

import base.AbstractTranslator;

public class UInt32Translator extends AbstractTranslator<UInt32Value, Integer> {

    @Override
    protected Integer convertInputObject(UInt32Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected UInt32Value convertSimObject(Integer simObject) {
        return UInt32Value.of(simObject);
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return UInt32Value.getDescriptor();
    }

    @Override
    public UInt32Value getDefaultInstanceForInputObject() {
        return UInt32Value.getDefaultInstance();
    }

    @Override
    public Class<Integer> getSimObjectClass() {
        return int.class;
    }

    @Override
    public Class<UInt32Value> getInputObjectClass() {
        return UInt32Value.class;
    }
}