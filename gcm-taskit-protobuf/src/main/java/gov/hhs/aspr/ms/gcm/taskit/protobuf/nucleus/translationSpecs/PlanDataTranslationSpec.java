package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.PlanDataInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import nucleus.PlanData;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain PlanDataInput} and
 * {@linkplain PlanData}
 */
public class PlanDataTranslationSpec extends ProtobufTranslationSpec<PlanDataInput, PlanData> {

    @Override
    protected PlanData convertInputObject(PlanDataInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getData());
    }

    @Override
    protected PlanDataInput convertAppObject(PlanData appObject) {
        return PlanDataInput.newBuilder().setData(this.translationEngine.getAnyFromObject(appObject)).build();
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
