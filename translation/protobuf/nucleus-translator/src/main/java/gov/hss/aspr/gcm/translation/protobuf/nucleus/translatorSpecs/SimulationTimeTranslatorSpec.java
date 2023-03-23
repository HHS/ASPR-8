package gov.hss.aspr.gcm.translation.protobuf.nucleus.translatorSpecs;

import java.time.LocalDate;

import com.google.type.Date;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.input.SimulationTimeInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import nucleus.SimulationTime;

public class SimulationTimeTranslatorSpec extends AbstractTranslatorSpec<SimulationTimeInput, SimulationTime> {

    @Override
    protected SimulationTime convertInputObject(SimulationTimeInput inputObject) {
        SimulationTime.Builder builder = SimulationTime.builder();

        builder.setStartTime(inputObject.getStartTime());

        if (inputObject.hasBaseDate()) {
            LocalDate LocalDate = this.translator.convertInputObject(inputObject.getBaseDate());
            builder.setBaseDate(LocalDate);
        }
        return builder.build();
    }

    @Override
    protected SimulationTimeInput convertAppObject(SimulationTime simObject) {
        SimulationTimeInput.Builder builder = SimulationTimeInput.newBuilder();

        builder.setStartTime(simObject.getStartTime());

        Date date = this.translator.convertSimObject(simObject.getBaseDate());
        builder.setBaseDate(date);

        return builder.build();
    }

    @Override
    public SimulationTimeInput getDefaultInstanceForInputObject() {
        return SimulationTimeInput.getDefaultInstance();
    }

    @Override
    public Class<SimulationTime> getAppObjectClass() {
        return SimulationTime.class;
    }

    @Override
    public Class<SimulationTimeInput> getInputObjectClass() {
        return SimulationTimeInput.class;
    }

}