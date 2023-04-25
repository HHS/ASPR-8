package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.UInt32Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;

public class UInt32TranslatorSpec extends ProtobufTranslatorSpec<UInt32Value, Integer> {

    @Override
    protected Integer convertInputObject(UInt32Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected UInt32Value convertAppObject(Integer appObject) {
        return UInt32Value.of(appObject);
    }

    @Override
    public Class<Integer> getAppObjectClass() {
        return Integer.class;
    }

    @Override
    public Class<UInt32Value> getInputObjectClass() {
        return UInt32Value.class;
    }
}