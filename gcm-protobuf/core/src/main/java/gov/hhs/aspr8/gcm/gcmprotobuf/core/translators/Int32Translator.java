package gov.hhs.aspr8.gcm.gcmprotobuf.core.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr8.gcm.gcmprotobuf.core.AbstractTranslator;

import com.google.protobuf.Int32Value;

public class Int32Translator extends AbstractTranslator<Int32Value, Integer> {

    @Override
    protected Integer convertInputObject(Int32Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected Int32Value convertSimObject(Integer simObject) {
        return Int32Value.of(simObject);
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return Int32Value.getDescriptor();
    }

    @Override
    public Int32Value getDefaultInstanceForInputObject() {
        return Int32Value.getDefaultInstance();
    }

    @Override
    public Class<Integer> getSimObjectClass() {
        return Integer.class;
    }

    @Override
    public Class<Int32Value> getInputObjectClass() {
        return Int32Value.class;
    }
}
