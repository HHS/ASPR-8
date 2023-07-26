package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs;

import java.time.LocalDate;

import com.google.type.Date;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.PlanQueueDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.SimulationStateInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.nucleus.PlanQueueData;
import gov.hhs.aspr.ms.gcm.nucleus.SimulationState;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain SimulationStateInput} and
 * {@linkplain SimulationState}
 */
public class SimulationStateTranslationSpec extends ProtobufTranslationSpec<SimulationStateInput, SimulationState> {

    @Override
    protected SimulationState convertInputObject(SimulationStateInput inputObject) {
        SimulationState.Builder builder = SimulationState.builder();

        builder.setStartTime(inputObject.getStartTime());

        if (inputObject.hasBaseDate()) {
            LocalDate LocalDate = this.translationEngine.convertObject(inputObject.getBaseDate());
            builder.setBaseDate(LocalDate);
        }

        builder.setPlanningQueueArrivalId(inputObject.getPlanningQueueArrivalId());

        for (PlanQueueDataInput planQueueDataInput : inputObject.getPlanQueueDatasList()) {
            PlanQueueData planQueueData = this.translationEngine.convertObject(planQueueDataInput);

            builder.addPlanQueueData(planQueueData);
        }
        return builder.build();
    }

    @Override
    protected SimulationStateInput convertAppObject(SimulationState appObject) {
        SimulationStateInput.Builder builder = SimulationStateInput.newBuilder();

        builder.setStartTime(appObject.getStartTime());

        Date date = this.translationEngine.convertObject(appObject.getBaseDate());
        builder.setBaseDate(date);

        builder.setPlanningQueueArrivalId(appObject.getPlanningQueueArrivalId());

        for (PlanQueueData planQueueData : appObject.getPlanQueueDatas()) {
            PlanQueueDataInput planQueueDataInput = this.translationEngine.convertObject(planQueueData);

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
