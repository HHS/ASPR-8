package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.BoolValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;

public class BooleanTranslatorSpec extends ProtobufTranslatorSpec<BoolValue, Boolean> {

    @Override
    protected Boolean convertInputObject(BoolValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected BoolValue convertAppObject(Boolean appObject) {
        return BoolValue.of(appObject);
    }

    @Override
    public Class<Boolean> getAppObjectClass() {
        return Boolean.class;
    }

    @Override
    public Class<BoolValue> getInputObjectClass() {
        return BoolValue.class;
    }
}