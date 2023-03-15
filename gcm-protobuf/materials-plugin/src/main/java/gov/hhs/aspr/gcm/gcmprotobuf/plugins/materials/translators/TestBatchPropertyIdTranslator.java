package gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractEnumTranslator;
import plugins.materials.input.TestBatchPropertyIdInput;
import plugins.materials.testsupport.TestBatchPropertyId;

public class TestBatchPropertyIdTranslator
        extends AbstractEnumTranslator<TestBatchPropertyIdInput, TestBatchPropertyId> {

    @Override
    protected TestBatchPropertyId convertInputObject(TestBatchPropertyIdInput inputObject) {
        return TestBatchPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestBatchPropertyIdInput convertSimObject(TestBatchPropertyId simObject) {
        return TestBatchPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestBatchPropertyIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestBatchPropertyIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestBatchPropertyId> getSimObjectClass() {
        return TestBatchPropertyId.class;
    }

    @Override
    public Class<TestBatchPropertyIdInput> getInputObjectClass() {
        return TestBatchPropertyIdInput.class;
    }

}
