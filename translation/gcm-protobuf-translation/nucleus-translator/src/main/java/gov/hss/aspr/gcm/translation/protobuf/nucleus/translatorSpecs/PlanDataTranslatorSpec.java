package gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanDataInput;
import nucleus.PlanData;

public class PlanDataTranslatorSpec extends ProtobufTranslatorSpec<PlanDataInput, PlanData> {

    @Override
    protected PlanData convertInputObject(PlanDataInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getData());
    }

    @Override
    protected PlanDataInput convertAppObject(PlanData appObject) {
        return PlanDataInput.newBuilder().setData(this.translatorCore.getAnyFromObject(appObject)).build();
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
