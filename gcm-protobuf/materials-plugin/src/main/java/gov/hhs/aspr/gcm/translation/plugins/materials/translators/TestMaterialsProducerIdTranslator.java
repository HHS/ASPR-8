package gov.hhs.aspr.gcm.translation.plugins.materials.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;

import gov.hhs.aspr.gcm.translation.core.AbstractEnumTranslator;

import com.google.protobuf.ProtocolMessageEnum;

import plugins.materials.input.TestMaterialsProducerIdInput;
import plugins.materials.testsupport.TestMaterialsProducerId;

public class TestMaterialsProducerIdTranslator
        extends AbstractEnumTranslator<TestMaterialsProducerIdInput, TestMaterialsProducerId> {

    @Override
    protected TestMaterialsProducerId convertInputObject(TestMaterialsProducerIdInput inputObject) {
        return TestMaterialsProducerId.valueOf(inputObject.name());
    }

    @Override
    protected TestMaterialsProducerIdInput convertSimObject(TestMaterialsProducerId simObject) {
        return TestMaterialsProducerIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestMaterialsProducerIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestMaterialsProducerIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestMaterialsProducerId> getSimObjectClass() {
        return TestMaterialsProducerId.class;
    }

    @Override
    public Class<TestMaterialsProducerIdInput> getInputObjectClass() {
        return TestMaterialsProducerIdInput.class;
    }

}
