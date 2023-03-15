package gov.hhs.aspr.gcm.translation.plugins.personproperties.translatorSpecs;

import com.google.protobuf.Descriptors.EnumDescriptor;

import gov.hhs.aspr.gcm.translation.core.AEnumTranslatorSpec;

import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.translation.plugins.personproperties.input.TestPersonPropertyIdInput;
import plugins.personproperties.testsupport.TestPersonPropertyId;

public class TestPersonPropertyIdTranslator
        extends AEnumTranslatorSpec<TestPersonPropertyIdInput, TestPersonPropertyId> {

    @Override
    protected TestPersonPropertyId convertInputObject(TestPersonPropertyIdInput inputObject) {
        return TestPersonPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestPersonPropertyIdInput convertSimObject(TestPersonPropertyId simObject) {
        return TestPersonPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestPersonPropertyIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestPersonPropertyIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestPersonPropertyId> getSimObjectClass() {
        return TestPersonPropertyId.class;
    }

    @Override
    public Class<TestPersonPropertyIdInput> getInputObjectClass() {
        return TestPersonPropertyIdInput.class;
    }

}
