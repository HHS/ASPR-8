package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.input.RandomNumberGeneratorIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.input.RandomNumberGeneratorMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.input.StochasticsPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.input.WellStateInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.stochastics.datamanagers.StochasticsPluginData;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.WellState;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain StochasticsPluginDataInput} and
 * {@linkplain StochasticsPluginData}
 */
public class StochasticsPluginDataTranslationSpec
        extends ProtobufTranslationSpec<StochasticsPluginDataInput, StochasticsPluginData> {

    @Override
    protected StochasticsPluginData convertInputObject(StochasticsPluginDataInput inputObject) {
        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        WellState wellState = this.translationEngine.convertObject(inputObject.getWellState());

        builder.setMainRNGState(wellState);

        for (RandomNumberGeneratorMapInput randomGenIdInput : inputObject.getRandomNumberGeneratorIdsList()) {
            RandomNumberGeneratorId generatorId = this.translationEngine
                    .convertObject(randomGenIdInput.getRandomNumberGeneratorId());
            WellState generatorWellState = this.translationEngine.convertObject(randomGenIdInput.getWellState());
            builder.addRNG(generatorId, generatorWellState);
        }

        return builder.build();
    }

    @Override
    protected StochasticsPluginDataInput convertAppObject(StochasticsPluginData appObject) {
        StochasticsPluginDataInput.Builder builder = StochasticsPluginDataInput.newBuilder();

        WellStateInput wellStateInput = this.translationEngine.convertObject(appObject.getWellState());
        builder.setWellState(wellStateInput);

        for (RandomNumberGeneratorId randomNumberGeneratorId : appObject.getRandomNumberGeneratorIds()) {
            RandomNumberGeneratorIdInput randomNumberGeneratorIdInput = this.translationEngine
                    .convertObjectAsSafeClass(randomNumberGeneratorId, RandomNumberGeneratorId.class);
            WellStateInput generatorWellStateInput = this.translationEngine
                    .convertObject(appObject.getWellState(randomNumberGeneratorId));

            builder.addRandomNumberGeneratorIds(
                    RandomNumberGeneratorMapInput.newBuilder().setWellState(generatorWellStateInput)
                            .setRandomNumberGeneratorId(randomNumberGeneratorIdInput).build());
        }

        return builder.build();
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
