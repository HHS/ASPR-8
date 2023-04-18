package gov.hss.aspr.gcm.translation.protobuf.nucleus.simObjects.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.example.input.ExamplePlanDataInput;
import gov.hss.aspr.gcm.translation.protobuf.nucleus.simObjects.ExamplePlanData;

public class ExamplePlanDataTranslatorSpec extends AbstractProtobufTranslatorSpec<ExamplePlanDataInput, ExamplePlanData> {

    @Override
    protected ExamplePlanData convertInputObject(ExamplePlanDataInput inputObject) {
        return new ExamplePlanData(inputObject.getPlanTime());
    }

    @Override
    protected ExamplePlanDataInput convertAppObject(ExamplePlanData simObject) {
        return ExamplePlanDataInput.newBuilder().setPlanTime(simObject.getPlanTime()).build();
    }

    @Override
    public Class<ExamplePlanData> getAppObjectClass() {
        return ExamplePlanData.class;
    }

    @Override
    public Class<ExamplePlanDataInput> getInputObjectClass() {
        return ExamplePlanDataInput.class;
    }

}
