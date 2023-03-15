package gov.hhs.aspr.gcm.gcmprotobuf.globalproperties.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractEnumTranslator;
import plugins.globalproperties.input.TestGlobalPropertyIdInput;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;

public class TestGlobalPropertyIdTranslator
        extends AbstractEnumTranslator<TestGlobalPropertyIdInput, TestGlobalPropertyId> {

    @Override
    protected TestGlobalPropertyId convertInputObject(TestGlobalPropertyIdInput inputObject) {
        return TestGlobalPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestGlobalPropertyIdInput convertSimObject(TestGlobalPropertyId simObject) {
        return TestGlobalPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestGlobalPropertyIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestGlobalPropertyIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestGlobalPropertyId> getSimObjectClass() {
        return TestGlobalPropertyId.class;
    }

    @Override
    public Class<TestGlobalPropertyIdInput> getInputObjectClass() {
        return TestGlobalPropertyIdInput.class;
    }

}