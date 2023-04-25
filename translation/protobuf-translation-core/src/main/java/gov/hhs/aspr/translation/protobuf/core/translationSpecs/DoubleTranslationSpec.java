package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.DoubleValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

public class DoubleTranslationSpec extends ProtobufTranslationSpec<DoubleValue, Double> {

    @Override
    protected Double convertInputObject(DoubleValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected DoubleValue convertAppObject(Double appObject) {
        return DoubleValue.of(appObject);
    }

    @Override
    public Class<Double> getAppObjectClass() {
        return Double.class;
    }

    @Override
    public Class<DoubleValue> getInputObjectClass() {
        return DoubleValue.class;
    }
}
