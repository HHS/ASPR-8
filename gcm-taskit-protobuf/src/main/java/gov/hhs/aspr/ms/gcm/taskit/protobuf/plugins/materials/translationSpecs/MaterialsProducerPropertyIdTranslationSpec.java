package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.MaterialsProducerPropertyIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain MaterialsProducerPropertyIdInput} and
 * {@linkplain MaterialsProducerPropertyId}
 */
public class MaterialsProducerPropertyIdTranslationSpec
        extends ProtobufTranslationSpec<MaterialsProducerPropertyIdInput, MaterialsProducerPropertyId> {

    @Override
    protected MaterialsProducerPropertyId convertInputObject(MaterialsProducerPropertyIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected MaterialsProducerPropertyIdInput convertAppObject(MaterialsProducerPropertyId appObject) {
        return MaterialsProducerPropertyIdInput.newBuilder().setId(this.translationEngine.getAnyFromObject(appObject))
                .build();
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
