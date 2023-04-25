package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.Int32Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

public class Int32TranslationSpec extends ProtobufTranslationSpec<Int32Value, Integer> {

    @Override
    protected Integer convertInputObject(Int32Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected Int32Value convertAppObject(Integer appObject) {
        return Int32Value.of(appObject);
    }

    @Override
    public Class<Integer> getAppObjectClass() {
        return Integer.class;
    }

    @Override
    public Class<Int32Value> getInputObjectClass() {
        return Int32Value.class;
    }
}
