package gov.hhs.aspr.gcm.translation.plugins.globalproperties.translatorSpecs;

import com.google.protobuf.Descriptors.EnumDescriptor;

import gov.hhs.aspr.gcm.translation.core.AEnumTranslatorSpec;

import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.translation.plugins.globalproperties.input.TestGlobalPropertyIdInput;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;

public class TestGlobalPropertyIdTranslatorSpec
        extends AEnumTranslatorSpec<TestGlobalPropertyIdInput, TestGlobalPropertyId> {

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