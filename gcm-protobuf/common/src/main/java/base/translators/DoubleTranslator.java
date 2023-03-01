package base.translators;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DoubleValue;

import base.AbstractTranslator;

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
        return double.class;
    }

    @Override
    public Class<DoubleValue> getInputObjectClass() {
        return DoubleValue.class;
    }
}
