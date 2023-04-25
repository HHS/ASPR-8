package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.stochastics.support.RandomNumberGeneratorId;

public class RandomGeneratorIdTranslationSpec
        extends ProtobufTranslationSpec<RandomNumberGeneratorIdInput, RandomNumberGeneratorId> {

    @Override
    protected RandomNumberGeneratorId convertInputObject(RandomNumberGeneratorIdInput inputObject) {
        return this.translationEnine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected RandomNumberGeneratorIdInput convertAppObject(RandomNumberGeneratorId appObject) {
        return RandomNumberGeneratorIdInput.newBuilder()
                .setId(this.translationEnine.getAnyFromObject(appObject)).build();
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
