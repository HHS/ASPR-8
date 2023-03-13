package gov.hhs.aspr.gcm.gcmprotobuf.stochastics.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractEnumTranslator;
import plugins.stochastics.input.TestRandomGeneratorIdInput;
import plugins.stochastics.testsupport.TestRandomGeneratorId;

public class TestRandomGeneratorIdTranslator
        extends AbstractEnumTranslator<TestRandomGeneratorIdInput, TestRandomGeneratorId> {

    @Override
    protected TestRandomGeneratorId convertInputObject(TestRandomGeneratorIdInput inputObject) {
        return TestRandomGeneratorId.valueOf(inputObject.name());
    }

    @Override
    protected TestRandomGeneratorIdInput convertSimObject(TestRandomGeneratorId simObject) {
        return TestRandomGeneratorIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestRandomGeneratorIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestRandomGeneratorIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestRandomGeneratorId> getSimObjectClass() {
        return TestRandomGeneratorId.class;
    }

    @Override
    public Class<TestRandomGeneratorIdInput> getInputObjectClass() {
        return TestRandomGeneratorIdInput.class;
    }

}
