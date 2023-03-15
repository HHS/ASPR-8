package gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.groups.simobjects.SimpleGroupTypeId;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.SimpleGroupTypeIdInput;

public class SimpleGroupTypeIdTranslator extends AObjectTranslatorSpec<SimpleGroupTypeIdInput, SimpleGroupTypeId> {

    @Override
    protected SimpleGroupTypeId convertInputObject(SimpleGroupTypeIdInput inputObject) {
        return new SimpleGroupTypeId(inputObject.getValue());
    }

    @Override
    protected SimpleGroupTypeIdInput convertSimObject(SimpleGroupTypeId simObject) {
        return SimpleGroupTypeIdInput.newBuilder().setValue(simObject.getValue().toString()).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return SimpleGroupTypeIdInput.getDescriptor();
    }

    @Override
    public SimpleGroupTypeIdInput getDefaultInstanceForInputObject() {
        return SimpleGroupTypeIdInput.getDefaultInstance();
    }

    @Override
    public Class<SimpleGroupTypeId> getSimObjectClass() {
        return SimpleGroupTypeId.class;
    }

    @Override
    public Class<SimpleGroupTypeIdInput> getInputObjectClass() {
        return SimpleGroupTypeIdInput.class;
    }

}
