package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.Int32Value;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;

public class Int32TranslatorSpec extends AbstractProtobufTranslatorSpec<Int32Value, Integer> {

    @Override
    protected Integer convertInputObject(Int32Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected Int32Value convertAppObject(Integer simObject) {
        return Int32Value.of(simObject);
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
