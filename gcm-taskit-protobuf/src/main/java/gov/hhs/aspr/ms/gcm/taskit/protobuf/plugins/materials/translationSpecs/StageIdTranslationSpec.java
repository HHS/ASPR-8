package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.support.input.StageIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.materials.support.StageId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain StageIdInput} and
 * {@linkplain StageId}
 */
public class StageIdTranslationSpec extends ProtobufTranslationSpec<StageIdInput, StageId> {

    @Override
    protected StageId convertInputObject(StageIdInput inputObject) {
        return new StageId(inputObject.getId());
    }

    @Override
    protected StageIdInput convertAppObject(StageId appObject) {
        return StageIdInput.newBuilder().setId(appObject.getValue()).build();
    }

    @Override
    public Class<StageId> getAppObjectClass() {
        return StageId.class;
    }

    @Override
    public Class<StageIdInput> getInputObjectClass() {
        return StageIdInput.class;
    }

}
