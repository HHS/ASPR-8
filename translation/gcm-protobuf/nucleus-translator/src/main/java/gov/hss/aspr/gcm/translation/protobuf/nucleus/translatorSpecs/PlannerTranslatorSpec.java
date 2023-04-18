package gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlannerInput;
import nucleus.Planner;

public class PlannerTranslatorSpec extends AbstractProtobufTranslatorSpec<PlannerInput, Planner> {

    @Override
    protected Planner convertInputObject(PlannerInput inputObject) {
        return Planner.valueOf(inputObject.name());
    }

    @Override
    protected PlannerInput convertAppObject(Planner simObject) {
        return PlannerInput.valueOf(simObject.name());
    }

    @Override
    public PlannerInput getDefaultInstanceForInputObject() {
        return PlannerInput.forNumber(0);
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
