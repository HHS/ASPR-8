package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.materials.support.MaterialsProducerId;

public class MaterialsProducerIdTranslatorSpec
        extends ProtobufTranslationSpec<MaterialsProducerIdInput, MaterialsProducerId> {

    @Override
    protected MaterialsProducerId convertInputObject(MaterialsProducerIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected MaterialsProducerIdInput convertAppObject(MaterialsProducerId appObject) {
        return MaterialsProducerIdInput.newBuilder().setId(this.translatorCore.getAnyFromObject(appObject)).build();
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
