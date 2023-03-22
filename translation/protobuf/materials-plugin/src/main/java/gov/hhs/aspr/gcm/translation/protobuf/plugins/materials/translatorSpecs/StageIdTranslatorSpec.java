package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.materials.input.StageIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.materials.support.StageId;

public class StageIdTranslatorSpec extends AbstractTranslatorSpec<StageIdInput, StageId> {

    @Override
    protected StageId convertInputObject(StageIdInput inputObject) {
        return new StageId(inputObject.getId());
    }

    @Override
    protected StageIdInput convertAppObject(StageId simObject) {
        return StageIdInput.newBuilder().setId(simObject.getValue()).build();
    }

    @Override
    public StageIdInput getDefaultInstanceForInputObject() {
        return StageIdInput.getDefaultInstance();
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
