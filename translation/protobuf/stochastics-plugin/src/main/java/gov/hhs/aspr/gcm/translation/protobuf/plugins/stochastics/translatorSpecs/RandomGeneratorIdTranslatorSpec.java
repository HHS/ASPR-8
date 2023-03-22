package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.stochastics.support.RandomNumberGeneratorId;

public class RandomGeneratorIdTranslatorSpec
        extends AbstractTranslatorSpec<RandomNumberGeneratorIdInput, RandomNumberGeneratorId> {

    @Override
    protected RandomNumberGeneratorId convertInputObject(RandomNumberGeneratorIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected RandomNumberGeneratorIdInput convertAppObject(RandomNumberGeneratorId simObject) {
        return RandomNumberGeneratorIdInput.newBuilder()
                .setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public RandomNumberGeneratorIdInput getDefaultInstanceForInputObject() {
        return RandomNumberGeneratorIdInput.getDefaultInstance();
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
