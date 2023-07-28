package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import com.google.protobuf.BoolValue;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java Boolean to a
 * Protobuf {@link BoolValue} type and vice versa
 */
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