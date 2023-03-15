package gov.hhs.aspr.gcm.translation.core.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslator;

import com.google.protobuf.DoubleValue;

public class DoubleTranslator extends AbstractTranslator<DoubleValue, Double> {

    @Override
    protected Double convertInputObject(DoubleValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected DoubleValue convertSimObject(Double simObject) {
        return DoubleValue.of(simObject);
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return DoubleValue.getDescriptor();
    }

    @Override
    public DoubleValue getDefaultInstanceForInputObject() {
        return DoubleValue.getDefaultInstance();
    }

    @Override
    public Class<Double> getSimObjectClass() {
        return Double.class;
    }

    @Override
    public Class<DoubleValue> getInputObjectClass() {
        return DoubleValue.class;
    }
}
