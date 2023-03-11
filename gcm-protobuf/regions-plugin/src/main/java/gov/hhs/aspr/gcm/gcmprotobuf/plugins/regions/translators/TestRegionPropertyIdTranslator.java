package gov.hhs.aspr.gcm.gcmprotobuf.plugins.regions.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractEnumTranslator;
import plugins.regions.input.TestRegionPropertyIdInput;
import plugins.regions.testsupport.TestRegionPropertyId;

public class TestRegionPropertyIdTranslator extends AbstractEnumTranslator<TestRegionPropertyIdInput, TestRegionPropertyId> {

    @Override
    protected TestRegionPropertyId convertInputObject(TestRegionPropertyIdInput inputObject) {
        return TestRegionPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestRegionPropertyIdInput convertSimObject(TestRegionPropertyId simObject) {
        return TestRegionPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestRegionPropertyIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestRegionPropertyIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestRegionPropertyId> getSimObjectClass() {
        return TestRegionPropertyId.class;
    }

    @Override
    public Class<TestRegionPropertyIdInput> getInputObjectClass() {
        return TestRegionPropertyIdInput.class;
    }

}
