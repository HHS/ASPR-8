package gov.hhs.aspr.gcm.translation.plugins.regions.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;

import gov.hhs.aspr.gcm.translation.core.AbstractEnumTranslator;

import com.google.protobuf.ProtocolMessageEnum;

import plugins.regions.input.TestRegionIdInput;
import plugins.regions.testsupport.TestRegionId;

public class TestRegionIdTranslator extends AbstractEnumTranslator<TestRegionIdInput, TestRegionId> {

    @Override
    protected TestRegionId convertInputObject(TestRegionIdInput inputObject) {
        return TestRegionId.valueOf(inputObject.name());
    }

    @Override
    protected TestRegionIdInput convertSimObject(TestRegionId simObject) {
        return TestRegionIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestRegionIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestRegionIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestRegionId> getSimObjectClass() {
        return TestRegionId.class;
    }

    @Override
    public Class<TestRegionIdInput> getInputObjectClass() {
        return TestRegionIdInput.class;
    }

}
