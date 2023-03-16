package gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.MaterialIdInput;
import plugins.materials.support.MaterialId;


public class MaterialIdTranslatorSpec extends AObjectTranslatorSpec<MaterialIdInput, MaterialId> {

    @Override
    protected MaterialId convertInputObject(MaterialIdInput inputObject) {
       return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected MaterialIdInput convertSimObject(MaterialId simObject) {
        return MaterialIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return MaterialIdInput.getDescriptor();
    }

    @Override
    public MaterialIdInput getDefaultInstanceForInputObject() {
       return MaterialIdInput.getDefaultInstance();
    }

    @Override
    public Class<MaterialId> getSimObjectClass() {
        return MaterialId.class;
    }

    @Override
    public Class<MaterialIdInput> getInputObjectClass() {
       return MaterialIdInput.class;
    }
    
}
