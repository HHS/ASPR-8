package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.input.EqualityInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.support.Equality;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain EqualityInput} and
 * {@linkplain Equality}
 */
public class EqualityTranslationSpec extends ProtobufTranslationSpec<EqualityInput, Equality> {

    @Override
    protected Equality convertInputObject(EqualityInput inputObject) {
        return Equality.valueOf(inputObject.name());
    }

    @Override
    protected EqualityInput convertAppObject(Equality appObject) {
        return EqualityInput.valueOf(appObject.name());
    }

    @Override
    public Class<Equality> getAppObjectClass() {
        return Equality.class;
    }

    @Override
    public Class<EqualityInput> getInputObjectClass() {
        return EqualityInput.class;
    }

}
