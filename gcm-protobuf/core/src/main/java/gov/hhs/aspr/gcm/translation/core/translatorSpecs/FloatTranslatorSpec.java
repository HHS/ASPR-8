package gov.hhs.aspr.gcm.translation.core.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;

import com.google.protobuf.FloatValue;

public class FloatTranslatorSpec extends AObjectTranslatorSpec<FloatValue, Float> {

    @Override
    protected Float convertInputObject(FloatValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected FloatValue convertSimObject(Float simObject) {
        return FloatValue.of(simObject);
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return FloatValue.getDescriptor();
    }

    @Override
    public FloatValue getDefaultInstanceForInputObject() {
        return FloatValue.getDefaultInstance();
    }

    @Override
    public Class<Float> getSimObjectClass() {
        return Float.class;
    }

    @Override
    public Class<FloatValue> getInputObjectClass() {
        return FloatValue.class;
    }
}
