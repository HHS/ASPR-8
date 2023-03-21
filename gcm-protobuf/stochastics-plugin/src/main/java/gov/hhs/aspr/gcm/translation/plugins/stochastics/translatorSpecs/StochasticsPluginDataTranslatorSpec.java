package gov.hhs.aspr.gcm.translation.plugins.stochastics.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.input.StochasticsPluginDataInput;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.RandomNumberGeneratorId;

public class StochasticsPluginDataTranslatorSpec
        extends AObjectTranslatorSpec<StochasticsPluginDataInput, StochasticsPluginData> {

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
    protected StochasticsPluginDataInput convertAppObject(StochasticsPluginData simObject) {
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
    public StochasticsPluginDataInput getDefaultInstanceForInputObject() {
        return StochasticsPluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<StochasticsPluginData> getAppObjectClass() {
        return StochasticsPluginData.class;
    }

    @Override
    public Class<StochasticsPluginDataInput> getInputObjectClass() {
        return StochasticsPluginDataInput.class;
    }

}
