package gov.hhs.aspr.gcm.translation.plugins.stochastics.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.Translator;
import plugins.stochastics.StochasticsPluginData;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.StochasticsPluginDataInput;
import plugins.stochastics.support.RandomNumberGeneratorId;

public class StochasticsPluginDataTranslator
        extends Translator<StochasticsPluginDataInput, StochasticsPluginData> {

    @Override
    protected StochasticsPluginData convertInputObject(StochasticsPluginDataInput inputObject) {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        builder.setSeed(inputObject.getSeed());

        for (RandomNumberGeneratorIdInput randomGenIdInput : inputObject.getRandomNumberGeneratorIdsList()) {
            RandomNumberGeneratorId generatorId = this.translator.convertInputObject(randomGenIdInput);
            builder.addRandomGeneratorId(generatorId);
        }

        return builder.build();
    }

    @Override
    protected StochasticsPluginDataInput convertSimObject(StochasticsPluginData simObject) {
        StochasticsPluginDataInput.Builder builder = StochasticsPluginDataInput.newBuilder();

        builder.setSeed(simObject.getSeed());

        for (RandomNumberGeneratorId randomNumberGeneratorId : simObject.getRandomNumberGeneratorIds()) {
            RandomNumberGeneratorIdInput randomNumberGeneratorIdInput = this.translator
                    .convertSimObject(randomNumberGeneratorId, RandomNumberGeneratorId.class);
            builder.addRandomNumberGeneratorIds(randomNumberGeneratorIdInput);
        }

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return StochasticsPluginDataInput.getDescriptor();
    }

    @Override
    public StochasticsPluginDataInput getDefaultInstanceForInputObject() {
        return StochasticsPluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<StochasticsPluginData> getSimObjectClass() {
        return StochasticsPluginData.class;
    }

    @Override
    public Class<StochasticsPluginDataInput> getInputObjectClass() {
        return StochasticsPluginDataInput.class;
    }

}
