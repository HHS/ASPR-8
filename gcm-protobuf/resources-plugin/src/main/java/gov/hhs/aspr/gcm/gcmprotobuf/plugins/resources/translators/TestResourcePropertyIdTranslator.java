package gov.hhs.aspr.gcm.gcmprotobuf.plugins.resources.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractEnumTranslator;
import plugins.resources.input.TestResourcePropertyIdInput;
import plugins.resources.testsupport.TestResourcePropertyId;


public class TestResourcePropertyIdTranslator extends AbstractEnumTranslator<TestResourcePropertyIdInput, TestResourcePropertyId> {

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
