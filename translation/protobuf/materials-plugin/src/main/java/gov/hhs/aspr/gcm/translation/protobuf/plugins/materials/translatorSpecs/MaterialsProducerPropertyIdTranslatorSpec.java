package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.materials.input.MaterialsProducerPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.materials.support.MaterialsProducerPropertyId;

public class MaterialsProducerPropertyIdTranslatorSpec
        extends AbstractTranslatorSpec<MaterialsProducerPropertyIdInput, MaterialsProducerPropertyId> {

    @Override
    protected MaterialsProducerPropertyId convertInputObject(MaterialsProducerPropertyIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected MaterialsProducerPropertyIdInput convertAppObject(MaterialsProducerPropertyId simObject) {
        return MaterialsProducerPropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public MaterialsProducerPropertyIdInput getDefaultInstanceForInputObject() {
        return MaterialsProducerPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<MaterialsProducerPropertyId> getAppObjectClass() {
        return MaterialsProducerPropertyId.class;
    }

    @Override
    public Class<MaterialsProducerPropertyIdInput> getInputObjectClass() {
        return MaterialsProducerPropertyIdInput.class;
    }

}
