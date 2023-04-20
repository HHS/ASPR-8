package gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs;

import java.time.LocalDate;

import com.google.type.Date;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationStateInput;
import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import nucleus.PlanQueueData;
import nucleus.SimulationState;

public class SimulationStateTranslatorSpec extends AbstractProtobufTranslatorSpec<SimulationStateInput, SimulationState> {

    @Override
    protected SimulationState convertInputObject(SimulationStateInput inputObject) {
        SimulationState.Builder builder = SimulationState.builder();

        builder.setStartTime(inputObject.getStartTime());

        if (inputObject.hasBaseDate()) {
            LocalDate LocalDate = this.translator.convertInputObject(inputObject.getBaseDate());
            builder.setBaseDate(LocalDate);
        }

        builder.setPlanningQueueArrivalId(inputObject.getPlanningQueueArrivalId());

        for(PlanQueueDataInput planQueueDataInput : inputObject.getPlanQueueDatasList()) {
            PlanQueueData planQueueData = this.translator.convertInputObject(planQueueDataInput);

            builder.addPlanQueueData(planQueueData);
        }
        return builder.build();
    }

    @Override
    protected SimulationStateInput convertAppObject(SimulationState simObject) {
        SimulationStateInput.Builder builder = SimulationStateInput.newBuilder();

        builder.setStartTime(simObject.getStartTime());

        Date date = this.translator.convertSimObject(simObject.getBaseDate());
        builder.setBaseDate(date);

        builder.setPlanningQueueArrivalId(simObject.getPlanningQueueArrivalId());

        for(PlanQueueData planQueueData : simObject.getPlanQueueDatas()) {
            PlanQueueDataInput planQueueDataInput = this.translator.convertSimObject(planQueueData);

            builder.addPlanQueueDatas(planQueueDataInput);
        }

        return builder.build();
    }

    @Override
    public Class<SimulationState> getAppObjectClass() {
        return SimulationState.class;
    }

    @Override
    public Class<SimulationStateInput> getInputObjectClass() {
        return SimulationStateInput.class;
    }

}
