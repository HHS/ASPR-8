package gov.hss.aspr.gcm.translation.protobuf.nucleus.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlannerInput;
import nucleus.Planner;

public class PlannerTranslationSpec extends ProtobufTranslationSpec<PlannerInput, Planner> {

    @Override
    protected Planner convertInputObject(PlannerInput inputObject) {
        return Planner.valueOf(inputObject.name());
    }

    @Override
    protected PlannerInput convertAppObject(Planner appObject) {
        return PlannerInput.valueOf(appObject.name());
    }

    @Override
    public Class<Planner> getAppObjectClass() {
        return Planner.class;
    }

    @Override
    public Class<PlannerInput> getInputObjectClass() {
        return PlannerInput.class;
    }

}
