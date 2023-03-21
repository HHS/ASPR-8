package gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.MaterialsProducerIdInput;
import plugins.materials.support.MaterialsProducerId;

public class MaterialsProducerIdTranslatorSpec
        extends AObjectTranslatorSpec<MaterialsProducerIdInput, MaterialsProducerId> {

    @Override
    protected MaterialsProducerId convertInputObject(MaterialsProducerIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected MaterialsProducerIdInput convertAppObject(MaterialsProducerId simObject) {
        return MaterialsProducerIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public MaterialsProducerIdInput getDefaultInstanceForInputObject() {
        return MaterialsProducerIdInput.getDefaultInstance();
    }

    @Override
    public Class<MaterialsProducerId> getAppObjectClass() {
        return MaterialsProducerId.class;
    }

    @Override
    public Class<MaterialsProducerIdInput> getInputObjectClass() {
        return MaterialsProducerIdInput.class;
    }

}
