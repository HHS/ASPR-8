package gov.hhs.aspr.gcm.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.Int32Value;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;

public class Int32TranslatorSpec extends AbstractTranslatorSpec<Int32Value, Integer> {

    @Override
    protected Integer convertInputObject(Int32Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected Int32Value convertAppObject(Integer simObject) {
        return Int32Value.of(simObject);
    }

    @Override
    public Int32Value getDefaultInstanceForInputObject() {
        return Int32Value.getDefaultInstance();
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