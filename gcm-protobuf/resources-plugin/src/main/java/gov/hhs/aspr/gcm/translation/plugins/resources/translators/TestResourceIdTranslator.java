package gov.hhs.aspr.gcm.translation.plugins.resources.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;

import gov.hhs.aspr.gcm.translation.core.AbstractEnumTranslator;

import com.google.protobuf.ProtocolMessageEnum;

import plugins.resources.input.TestResourceIdInput;
import plugins.resources.testsupport.TestResourceId;


public class TestResourceIdTranslator extends AbstractEnumTranslator<TestResourceIdInput, TestResourceId> {

    @Override
    protected TestResourceId convertInputObject(TestResourceIdInput inputObject) {
        return TestResourceId.valueOf(inputObject.name());
    }

    @Override
    protected TestResourceIdInput convertSimObject(TestResourceId simObject) {
        return TestResourceIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestResourceIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestResourceIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestResourceId> getSimObjectClass() {
        return TestResourceId.class;
    }

    @Override
    public Class<TestResourceIdInput> getInputObjectClass() {
        return TestResourceIdInput.class;
    }

}
