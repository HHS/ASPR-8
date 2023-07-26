package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.PlanDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.PlannerInput;
import nucleus.PlanData;
import nucleus.PlanQueueData;
import nucleus.Planner;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain PlanQueueDataInput} and
 * {@linkplain PlanQueueData}
 */
public class PlanQueueDataTranslationSpec extends ProtobufTranslationSpec<PlanQueueDataInput, PlanQueueData> {

    @Override
    protected PlanQueueData convertInputObject(PlanQueueDataInput inputObject) {
        PlanQueueData.Builder builder = PlanQueueData.builder();

        builder.setTime(inputObject.getTime());

        if (inputObject.hasActive() && inputObject.getActive() == false) {
            builder.setActive(false);
        }

        Object key = this.translationEngine.getObjectFromAny(inputObject.getKey());
        builder.setKey(key);

        PlanData planData = this.translationEngine.convertObject(inputObject.getPlanData());
        builder.setPlanData(planData);

        Planner planner = this.translationEngine.convertObject(inputObject.getPlanner());
        builder.setPlanner(planner);

        builder.setPlannerId(inputObject.getPlannerId()).setArrivalId(inputObject.getArrivalId());

        return builder.build();
    }

    @Override
    protected PlanQueueDataInput convertAppObject(PlanQueueData appObject) {
        PlanQueueDataInput.Builder builder = PlanQueueDataInput.newBuilder();

        builder
                .setActive(appObject.isActive())
                .setArrivalId(appObject.getArrivalId())
                .setPlannerId(appObject.getPlannerId())
                .setTime(appObject.getTime());

        PlannerInput plannerInput = this.translationEngine.convertObject(appObject.getPlanner());
        builder.setPlanner(plannerInput);

        PlanDataInput planDataInput = this.translationEngine.convertObjectAsSafeClass(appObject.getPlanData(),
                PlanData.class);
        builder.setPlanData(planDataInput);

        builder.setKey(this.translationEngine.getAnyFromObject(appObject.getKey()));

        return builder.build();
    }

    @Override
    public Class<PlanQueueData> getAppObjectClass() {
        return PlanQueueData.class;
    }

    @Override
    public Class<PlanQueueDataInput> getInputObjectClass() {
        return PlanQueueDataInput.class;
    }

}
