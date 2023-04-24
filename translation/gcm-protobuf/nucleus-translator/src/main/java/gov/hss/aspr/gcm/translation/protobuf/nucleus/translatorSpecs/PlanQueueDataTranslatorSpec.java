package gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlannerInput;
import nucleus.PlanData;
import nucleus.PlanQueueData;
import nucleus.Planner;

public class PlanQueueDataTranslatorSpec extends ProtobufTranslatorSpec<PlanQueueDataInput, PlanQueueData> {

    @Override
    protected PlanQueueData convertInputObject(PlanQueueDataInput inputObject) {
        PlanQueueData.Builder builder = PlanQueueData.builder();

        builder.setTime(inputObject.getTime());

        if (inputObject.hasActive()) {
            builder.setActive(inputObject.getActive());
        }

        Object key = this.translatorCore.getObjectFromAny(inputObject.getKey());
        builder.setKey(key);

        PlanData planData = this.translatorCore.convertObject(inputObject.getPlanData());
        builder.setPlanData(planData);

        Planner planner = this.translatorCore.convertObject(inputObject.getPlanner());
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

        PlannerInput plannerInput = this.translatorCore.convertObject(appObject.getPlanner());
        builder.setPlanner(plannerInput);

        PlanDataInput planDataInput = this.translatorCore.convertObjectAsSafeClass(appObject.getPlanData(), PlanData.class);
        builder.setPlanData(planDataInput);

        builder.setKey(this.translatorCore.getAnyFromObject(appObject.getKey()));

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
