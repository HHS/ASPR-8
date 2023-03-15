package gov.hhs.aspr.gcm.translation.core.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslator;

import com.google.protobuf.Int64Value;

public class Int64Translator extends AbstractTranslator<Int64Value, Long> {

    @Override
    protected Long convertInputObject(Int64Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected Int64Value convertSimObject(Long simObject) {
        return Int64Value.of(simObject);
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return Int64Value.getDescriptor();
    }

    @Override
    public Int64Value getDefaultInstanceForInputObject() {
        return Int64Value.getDefaultInstance();
    }

    @Override
    public Class<Long> getSimObjectClass() {
        return Long.class;
    }

    @Override
    public Class<Int64Value> getInputObjectClass() {
        return Int64Value.class;
    }
}
