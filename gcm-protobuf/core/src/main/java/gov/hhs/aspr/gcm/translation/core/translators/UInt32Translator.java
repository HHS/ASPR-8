package gov.hhs.aspr.gcm.translation.core.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.ObjectTranslator;

import com.google.protobuf.UInt32Value;

public class UInt32Translator extends ObjectTranslator<UInt32Value, Integer> {

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
        return Integer.class;
    }

    @Override
    public Class<UInt32Value> getInputObjectClass() {
        return UInt32Value.class;
    }
}