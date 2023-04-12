package gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlannerInput;
import nucleus.PlanData;
import nucleus.PlanQueueData;
import nucleus.Planner;

public class PlanQueueDataTranslatorSpec extends AbstractTranslatorSpec<PlanQueueDataInput, PlanQueueData> {

    @Override
    protected PlanQueueData convertInputObject(PlanQueueDataInput inputObject) {
        PlanQueueData.Builder builder = PlanQueueData.builder();

        builder.setTime(inputObject.getTime());

        if (inputObject.hasActive()) {
            builder.setActive(inputObject.getActive());
        }

        Object key = this.translator.getObjectFromAny(inputObject.getKey());
        builder.setKey(key);

        PlanData planData = this.translator.convertInputObject(inputObject.getPlanData(), PlanData.class);
        builder.setPlanData(planData);

        Planner planner = this.translator.convertInputEnum(inputObject.getPlanner());
        builder.setPlanner(planner);

        builder.setPlannerId(inputObject.getPlannerId()).setArrivalId(inputObject.getArrivalId());

        return builder.build();
    }

    @Override
    protected PlanQueueDataInput convertAppObject(PlanQueueData simObject) {
        PlanQueueDataInput.Builder builder = PlanQueueDataInput.newBuilder();

        builder
                .setActive(simObject.isActive())
                .setArrivalId(simObject.getArrivalId())
                .setPlannerId(simObject.getPlannerId())
                .setTime(simObject.getTime());

        PlannerInput plannerInput = this.translator.convertSimObject(simObject.getPlanner());
        builder.setPlanner(plannerInput);

        PlanDataInput planDataInput = this.translator.convertSimObject(simObject.getPlanData(), PlanData.class);
        builder.setPlanData(planDataInput);

        builder.setKey(this.translator.getAnyFromObject(simObject.getKey()));

        return builder.build();
    }

    @Override
    public PlanQueueDataInput getDefaultInstanceForInputObject() {
        return PlanQueueDataInput.getDefaultInstance();
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
