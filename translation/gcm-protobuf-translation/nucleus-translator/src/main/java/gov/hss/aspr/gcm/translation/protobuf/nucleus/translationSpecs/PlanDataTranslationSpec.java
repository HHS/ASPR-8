package gov.hss.aspr.gcm.translation.protobuf.nucleus.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanDataInput;
import nucleus.PlanData;

public class PlanDataTranslationSpec extends ProtobufTranslationSpec<PlanDataInput, PlanData> {

    @Override
    protected PlanData convertInputObject(PlanDataInput inputObject) {
        return this.translationEnine.getObjectFromAny(inputObject.getData());
    }

    @Override
    protected PlanDataInput convertAppObject(PlanData appObject) {
        return PlanDataInput.newBuilder().setData(this.translationEnine.getAnyFromObject(appObject)).build();
    }

    @Override
    public Class<PlanData> getAppObjectClass() {
        return PlanData.class;
    }

    @Override
    public Class<PlanDataInput> getInputObjectClass() {
        return PlanDataInput.class;
    }

}
