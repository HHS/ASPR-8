package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractEnumTranslator;
import plugins.groups.input.TestGroupTypeIdInput;
import plugins.groups.testsupport.TestGroupTypeId;

public class TestGroupTypeIdTranslator extends AbstractEnumTranslator<TestGroupTypeIdInput, TestGroupTypeId> {

    @Override
    protected TestGroupTypeId convertInputObject(TestGroupTypeIdInput inputObject) {
        return TestGroupTypeId.valueOf(inputObject.name());
    }

    @Override
    protected TestGroupTypeIdInput convertSimObject(TestGroupTypeId simObject) {
        return TestGroupTypeIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestGroupTypeIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestGroupTypeIdInput.valueOf(string);
            }
            
        };
    }

    @Override
    public Class<TestGroupTypeId> getSimObjectClass() {
        return TestGroupTypeId.class;
    }

    @Override
    public Class<TestGroupTypeIdInput> getInputObjectClass() {
        return TestGroupTypeIdInput.class;
    }

}
