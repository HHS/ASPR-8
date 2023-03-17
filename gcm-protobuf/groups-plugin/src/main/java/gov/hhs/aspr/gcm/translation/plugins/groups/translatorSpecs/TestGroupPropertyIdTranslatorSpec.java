package gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs;

import com.google.protobuf.Descriptors.EnumDescriptor;

import gov.hhs.aspr.gcm.translation.core.AEnumTranslatorSpec;

import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.translation.plugins.groups.input.TestGroupPropertyIdInput;
import plugins.groups.testsupport.TestGroupPropertyId;

public class TestGroupPropertyIdTranslatorSpec
        extends AEnumTranslatorSpec<TestGroupPropertyIdInput, TestGroupPropertyId> {

    @Override
    protected TestGroupPropertyId convertInputObject(TestGroupPropertyIdInput inputObject) {
        return TestGroupPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestGroupPropertyIdInput convertSimObject(TestGroupPropertyId simObject) {
        return TestGroupPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestGroupPropertyIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestGroupPropertyIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestGroupPropertyId> getSimObjectClass() {
        return TestGroupPropertyId.class;
    }

    @Override
    public Class<TestGroupPropertyIdInput> getInputObjectClass() {
        return TestGroupPropertyIdInput.class;
    }

}
