package gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanDataInput;
import nucleus.PlanData;

public class PlanDataTranslatorSpec extends AbstractProtobufTranslatorSpec<PlanDataInput, PlanData> {

    @Override
    protected PlanData convertInputObject(PlanDataInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getData());
    }

    @Override
    protected PlanDataInput convertAppObject(PlanData simObject) {
        return PlanDataInput.newBuilder().setData(this.translator.getAnyFromObject(simObject)).build();
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
