package gov.hhs.aspr.gcm.translation.protobuf.nucleus.simObjects.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.example.input.ExamplePlanDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.simObjects.ExamplePlanData;

public class ExamplePlanDataTranslationSpec extends ProtobufTranslationSpec<ExamplePlanDataInput, ExamplePlanData> {

    @Override
    protected ExamplePlanData convertInputObject(ExamplePlanDataInput inputObject) {
        return new ExamplePlanData(inputObject.getPlanTime());
    }

    @Override
    protected ExamplePlanDataInput convertAppObject(ExamplePlanData appObject) {
        return ExamplePlanDataInput.newBuilder().setPlanTime(appObject.getPlanTime()).build();
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
