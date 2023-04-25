package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.materials.support.MaterialsProducerPropertyId;

public class MaterialsProducerPropertyIdTranslatorSpec
        extends ProtobufTranslationSpec<MaterialsProducerPropertyIdInput, MaterialsProducerPropertyId> {

    @Override
    protected MaterialsProducerPropertyId convertInputObject(MaterialsProducerPropertyIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected MaterialsProducerPropertyIdInput convertAppObject(MaterialsProducerPropertyId appObject) {
        return MaterialsProducerPropertyIdInput.newBuilder().setId(this.translatorCore.getAnyFromObject(appObject)).build();
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
