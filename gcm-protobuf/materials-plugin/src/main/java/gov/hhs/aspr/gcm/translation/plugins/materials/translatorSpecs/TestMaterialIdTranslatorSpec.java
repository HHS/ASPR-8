package gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs;

import com.google.protobuf.Descriptors.EnumDescriptor;

import gov.hhs.aspr.gcm.translation.core.AEnumTranslatorSpec;

import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.translation.plugins.materials.input.TestMaterialIdInput;
import plugins.materials.testsupport.TestMaterialId;

public class TestMaterialIdTranslatorSpec
        extends AEnumTranslatorSpec<TestMaterialIdInput, TestMaterialId> {

    @Override
    protected TestMaterialId convertInputObject(TestMaterialIdInput inputObject) {
        return TestMaterialId.valueOf(inputObject.name());
    }

    @Override
    protected TestMaterialIdInput convertSimObject(TestMaterialId simObject) {
        return TestMaterialIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestMaterialIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestMaterialIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestMaterialId> getSimObjectClass() {
        return TestMaterialId.class;
    }

    @Override
    public Class<TestMaterialIdInput> getInputObjectClass() {
        return TestMaterialIdInput.class;
    }

}
