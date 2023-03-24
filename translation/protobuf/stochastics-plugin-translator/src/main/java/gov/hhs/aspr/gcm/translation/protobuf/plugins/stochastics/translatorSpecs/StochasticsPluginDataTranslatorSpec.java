package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.StochasticsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.RandomNumberGeneratorId;

public class StochasticsPluginDataTranslatorSpec
        extends AbstractTranslatorSpec<StochasticsPluginDataInput, StochasticsPluginData> {

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
