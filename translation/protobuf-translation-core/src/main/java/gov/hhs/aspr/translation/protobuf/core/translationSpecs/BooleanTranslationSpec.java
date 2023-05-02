package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.BoolValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

public class BooleanTranslationSpec extends ProtobufTranslationSpec<BoolValue, Boolean> {

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