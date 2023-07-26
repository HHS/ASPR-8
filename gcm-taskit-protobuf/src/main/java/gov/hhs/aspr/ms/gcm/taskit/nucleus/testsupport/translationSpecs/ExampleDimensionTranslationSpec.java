package gov.hhs.aspr.gcm.translation.protobuf.nucleus.testsupport.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.example.input.ExampleDimensionInput;
import gov.hhs.aspr.gcm.translation.protobuf.nucleus.testsupport.ExampleDimension;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

public class ExampleDimensionTranslationSpec extends ProtobufTranslationSpec<ExampleDimensionInput, ExampleDimension> {

    @Override
    protected ExampleDimension convertInputObject(ExampleDimensionInput inputObject) {
        return new ExampleDimension(inputObject.getLevelName());
    }

    @Override
    protected ExampleDimensionInput convertAppObject(ExampleDimension appObject) {
        return ExampleDimensionInput.newBuilder()
                .setLevelName(appObject.getLevelName())
                .build();
    }

    @Override
    public Class<ExampleDimension> getAppObjectClass() {
        return ExampleDimension.class;
    }

    @Override
    public Class<ExampleDimensionInput> getInputObjectClass() {
        return ExampleDimensionInput.class;
    }

}
