package core.translators;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Descriptors.Descriptor;

import core.AbstractTranslator;

public class BooleanTranslator extends AbstractTranslator<BoolValue, Boolean> {

    @Override
    protected Boolean convertInputObject(BoolValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected BoolValue convertSimObject(Boolean simObject) {
        return BoolValue.of(simObject);
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return BoolValue.getDescriptor();
    }

    @Override
    public BoolValue getDefaultInstanceForInputObject() {
        return BoolValue.getDefaultInstance();
    }

    @Override
    public Class<Boolean> getSimObjectClass() {
        return Boolean.class;
    }

    @Override
    public Class<BoolValue> getInputObjectClass() {
        return BoolValue.class;
    }
}