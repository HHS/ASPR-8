package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.Int64Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;

public class Int64TranslatorSpec extends ProtobufTranslatorSpec<Int64Value, Long> {

    @Override
    protected Long convertInputObject(Int64Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected Int64Value convertAppObject(Long appObject) {
        return Int64Value.of(appObject);
    }

    @Override
    public Class<Long> getAppObjectClass() {
        return Long.class;
    }

    @Override
    public Class<Int64Value> getInputObjectClass() {
        return Int64Value.class;
    }
}
