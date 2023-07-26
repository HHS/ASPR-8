package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.PlannerInput;
import nucleus.Planner;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain PlannerInput} and
 * {@linkplain Planner}
 */
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
