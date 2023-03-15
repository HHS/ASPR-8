package gov.hhs.aspr.gcm.translation.plugins.resources.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;

import gov.hhs.aspr.gcm.translation.core.EnumTranslator;

import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.translation.plugins.resources.input.TestResourcePropertyIdInput;
import plugins.resources.testsupport.TestResourcePropertyId;


public class TestResourcePropertyIdTranslator extends EnumTranslator<TestResourcePropertyIdInput, TestResourcePropertyId> {

    @Override
    protected TestResourcePropertyId convertInputObject(TestResourcePropertyIdInput inputObject) {
        return TestResourcePropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestResourcePropertyIdInput convertSimObject(TestResourcePropertyId simObject) {
        return TestResourcePropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestResourcePropertyIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestResourcePropertyIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestResourcePropertyId> getSimObjectClass() {
        return TestResourcePropertyId.class;
    }

    @Override
    public Class<TestResourcePropertyIdInput> getInputObjectClass() {
        return TestResourcePropertyIdInput.class;
    }

}
