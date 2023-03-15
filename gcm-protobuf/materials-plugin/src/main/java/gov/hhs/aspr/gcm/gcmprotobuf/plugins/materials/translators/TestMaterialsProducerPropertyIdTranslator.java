package gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators;

import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.ProtocolMessageEnum;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractEnumTranslator;
import plugins.materials.input.TestMaterialsProducerPropertyIdInput;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;

public class TestMaterialsProducerPropertyIdTranslator
        extends AbstractEnumTranslator<TestMaterialsProducerPropertyIdInput, TestMaterialsProducerPropertyId> {

    @Override
    protected TestMaterialsProducerPropertyId convertInputObject(TestMaterialsProducerPropertyIdInput inputObject) {
        return TestMaterialsProducerPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestMaterialsProducerPropertyIdInput convertSimObject(TestMaterialsProducerPropertyId simObject) {
        return TestMaterialsProducerPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public EnumDescriptor getDescriptorForInputObject() {
        return TestMaterialsProducerPropertyIdInput.getDescriptor();
    }

    @Override
    public EnumInstance getEnumInstance() {
        return new EnumInstance() {

            @Override
            public ProtocolMessageEnum getFromString(String string) {
                return TestMaterialsProducerPropertyIdInput.valueOf(string);
            }

        };
    }

    @Override
    public Class<TestMaterialsProducerPropertyId> getSimObjectClass() {
        return TestMaterialsProducerPropertyId.class;
    }

    @Override
    public Class<TestMaterialsProducerPropertyIdInput> getInputObjectClass() {
        return TestMaterialsProducerPropertyIdInput.class;
    }

}
