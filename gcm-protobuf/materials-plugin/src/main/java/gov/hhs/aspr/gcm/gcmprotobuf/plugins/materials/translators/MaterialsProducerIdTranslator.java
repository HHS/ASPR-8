package gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.materials.input.MaterialsProducerIdInput;
import plugins.materials.support.MaterialsProducerId;


public class MaterialsProducerIdTranslator extends AbstractTranslator<MaterialsProducerIdInput, MaterialsProducerId> {

    @Override
    protected MaterialsProducerId convertInputObject(MaterialsProducerIdInput inputObject) {
       return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected MaterialsProducerIdInput convertSimObject(MaterialsProducerId simObject) {
        return MaterialsProducerIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return MaterialsProducerIdInput.getDescriptor();
    }

    @Override
    public MaterialsProducerIdInput getDefaultInstanceForInputObject() {
       return MaterialsProducerIdInput.getDefaultInstance();
    }

    @Override
    public Class<MaterialsProducerId> getSimObjectClass() {
        return MaterialsProducerId.class;
    }

    @Override
    public Class<MaterialsProducerIdInput> getInputObjectClass() {
       return MaterialsProducerIdInput.class;
    }
    
}
