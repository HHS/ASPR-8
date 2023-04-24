package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.stochastics.support.RandomNumberGeneratorId;

public class RandomGeneratorIdTranslatorSpec
        extends ProtobufTranslatorSpec<RandomNumberGeneratorIdInput, RandomNumberGeneratorId> {

    @Override
    protected RandomNumberGeneratorId convertInputObject(RandomNumberGeneratorIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected RandomNumberGeneratorIdInput convertAppObject(RandomNumberGeneratorId appObject) {
        return RandomNumberGeneratorIdInput.newBuilder()
                .setId(this.translatorCore.getAnyFromObject(appObject)).build();
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
