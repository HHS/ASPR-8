package gov.hhs.aspr.gcm.gcmprotobuf.personproperties.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractEnumTranslator;
import plugins.personproperties.input.TestPersonPropertyIdInput;
import plugins.personproperties.testsupport.TestPersonPropertyId;

public class TestPersonPropertyIdTranslator
        extends AbstractEnumTranslator<TestPersonPropertyIdInput, TestPersonPropertyId> {

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
