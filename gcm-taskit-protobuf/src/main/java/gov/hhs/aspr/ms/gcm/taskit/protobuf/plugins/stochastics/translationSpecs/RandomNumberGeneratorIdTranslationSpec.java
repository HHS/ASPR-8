package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.support.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.RandomNumberGeneratorId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain RandomNumberGeneratorIdInput} and
 * {@linkplain RandomNumberGeneratorId}
 */
public class RandomNumberGeneratorIdTranslationSpec
        extends ProtobufTranslationSpec<RandomNumberGeneratorIdInput, RandomNumberGeneratorId> {

    @Override
    protected RandomNumberGeneratorId convertInputObject(RandomNumberGeneratorIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected RandomNumberGeneratorIdInput convertAppObject(RandomNumberGeneratorId appObject) {
        return RandomNumberGeneratorIdInput.newBuilder()
                .setId(this.translationEngine.getAnyFromObject(appObject)).build();
    }

    @Override
    public Class<RandomNumberGeneratorId> getAppObjectClass() {
        return RandomNumberGeneratorId.class;
    }

    @Override
    public Class<RandomNumberGeneratorIdInput> getInputObjectClass() {
        return RandomNumberGeneratorIdInput.class;
    }
}
