package gov.hhs.aspr.gcm.translation.plugins.materials.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.ObjectTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.MaterialsProducerPropertyIdInput;
import plugins.materials.support.MaterialsProducerPropertyId;


public class MaterialsProducerPropertyIdTranslator extends ObjectTranslator<MaterialsProducerPropertyIdInput, MaterialsProducerPropertyId> {

    @Override
    protected MaterialsProducerPropertyId convertInputObject(MaterialsProducerPropertyIdInput inputObject) {
       return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected MaterialsProducerPropertyIdInput convertSimObject(MaterialsProducerPropertyId simObject) {
        return MaterialsProducerPropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return MaterialsProducerPropertyIdInput.getDescriptor();
    }

    @Override
    public MaterialsProducerPropertyIdInput getDefaultInstanceForInputObject() {
       return MaterialsProducerPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<MaterialsProducerPropertyId> getSimObjectClass() {
        return MaterialsProducerPropertyId.class;
    }

    @Override
    public Class<MaterialsProducerPropertyIdInput> getInputObjectClass() {
       return MaterialsProducerPropertyIdInput.class;
    }
    
}
