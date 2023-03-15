package gov.hhs.aspr.gcm.translation.core.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;

import com.google.protobuf.UInt64Value;

public class UInt64Translator extends AObjectTranslatorSpec<UInt64Value, Long> {

    @Override
    protected Long convertInputObject(UInt64Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected UInt64Value convertSimObject(Long simObject) {
        return UInt64Value.of(simObject);
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return UInt64Value.getDescriptor();
    }

    @Override
    public UInt64Value getDefaultInstanceForInputObject() {
        return UInt64Value.getDefaultInstance();
    }

    @Override
    public Class<Long> getSimObjectClass() {
        return Long.class;
    }

    @Override
    public Class<UInt64Value> getInputObjectClass() {
        return UInt64Value.class;
    }
}
