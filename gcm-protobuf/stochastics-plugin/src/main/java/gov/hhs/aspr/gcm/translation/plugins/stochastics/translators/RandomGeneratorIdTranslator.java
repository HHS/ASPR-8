package gov.hhs.aspr.gcm.translation.plugins.stochastics.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.RandomNumberGeneratorIdInput;
import plugins.stochastics.support.RandomNumberGeneratorId;

public class RandomGeneratorIdTranslator extends AbstractTranslator<RandomNumberGeneratorIdInput, RandomNumberGeneratorId> {

    @Override
    protected RandomNumberGeneratorId convertInputObject(RandomNumberGeneratorIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected RandomNumberGeneratorIdInput convertSimObject(RandomNumberGeneratorId simObject) {
        return RandomNumberGeneratorIdInput.newBuilder()
                .setId(this.translator.getAnyFromObject(simObject)).build();
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
