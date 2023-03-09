package gov.hhs.aspr.gcm.gcmprotobuf.stochastics.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.stochastics.input.RandomNumberGeneratorIdInput;
import plugins.stochastics.support.RandomNumberGeneratorId;

public class RandomGeneratorIdTranslator extends AbstractTranslator<RandomNumberGeneratorIdInput, RandomNumberGeneratorId> {

    @Override
    protected RandomNumberGeneratorId convertInputObject(RandomNumberGeneratorIdInput inputObject) {
        return (RandomNumberGeneratorId) this.translator.getObjectFromAny(inputObject.getRandomNumberGeneratorId());
    }

    @Override
    protected RandomNumberGeneratorIdInput convertSimObject(RandomNumberGeneratorId simObject) {
        return RandomNumberGeneratorIdInput.newBuilder()
                .setRandomNumberGeneratorId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return RandomNumberGeneratorIdInput.getDescriptor();
    }

    @Override
    public RandomNumberGeneratorIdInput getDefaultInstanceForInputObject() {
        return RandomNumberGeneratorIdInput.getDefaultInstance();
    }

    @Override
    public Class<RandomNumberGeneratorId> getSimObjectClass() {
        return RandomNumberGeneratorId.class;
    }

    @Override
    public Class<RandomNumberGeneratorIdInput> getInputObjectClass() {
        return RandomNumberGeneratorIdInput.class;
    }
}
