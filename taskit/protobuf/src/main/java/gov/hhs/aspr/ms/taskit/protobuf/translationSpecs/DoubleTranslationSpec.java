package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import com.google.protobuf.DoubleValue;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java Double to a
 * Protobuf {@link DoubleValue} type and vice versa
 */
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
