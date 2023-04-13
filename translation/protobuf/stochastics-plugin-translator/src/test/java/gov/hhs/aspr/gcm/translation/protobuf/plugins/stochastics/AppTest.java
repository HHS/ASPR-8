package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.StochasticsPluginDataInput;
import nucleus.PluginData;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.Well;
import plugins.stochastics.support.WellState;
import plugins.stochastics.testsupport.StochasticsTestPluginFactory;
import plugins.stochastics.testsupport.TestRandomGeneratorId;

public class AppTest {

    @Test
    public void testStochasticsTranslator() {

        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("stochastics-plugin-translator")) {
            basePath = basePath.resolve("stochastics-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "pluginData.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(StochasticsTranslator.getTranslator())
                .addReader(inputFilePath.resolve(fileName), StochasticsPluginDataInput.class)
                .addWriter(outputFilePath.resolve(fileName), StochasticsPluginData.class)
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        StochasticsPluginData actualPluginData = (StochasticsPluginData) pluginDatas.get(0);

        long seed = 524805676405822016L;

        StochasticsPluginData stochasticsPluginDataIn = StochasticsTestPluginFactory
                .getStandardStochasticsPluginData(seed);

        StochasticsDataManager stochasticsDataManager = new StochasticsDataManager(stochasticsPluginDataIn);

        StochasticsPluginData.Builder builder = StochasticsPluginData.builder();

        for (RandomNumberGeneratorId randomNumberGeneratorId : stochasticsDataManager.getRandomNumberGeneratorIds()) {
            Well wellRNG = (Well) stochasticsDataManager.getRandomGeneratorFromId(randomNumberGeneratorId);
            if (randomNumberGeneratorId == TestRandomGeneratorId.DASHER
                    || randomNumberGeneratorId == TestRandomGeneratorId.CUPID) {
                builder.addRNG(randomNumberGeneratorId,
                        WellState.builder().setSeed(wellRNG.getWellState().getSeed()).build());
            } else {
                builder.addRNG(randomNumberGeneratorId, wellRNG.getWellState());
            }
        }
        builder.setMainRNGState(((Well) stochasticsDataManager.getRandomGenerator()).getWellState());

        StochasticsPluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getWellState(), actualPluginData.getWellState());

        Set<TestRandomGeneratorId> expectedRandomGeneratorIds = EnumSet.allOf(TestRandomGeneratorId.class);
        assertFalse(expectedRandomGeneratorIds.isEmpty());

        Set<RandomNumberGeneratorId> actualsGeneratorIds = actualPluginData.getRandomNumberGeneratorIds();

        assertEquals(expectedRandomGeneratorIds, actualsGeneratorIds);

        for (RandomNumberGeneratorId randomNumberGeneratorId : actualsGeneratorIds) {
            assertEquals(expectedPluginData.getWellState(randomNumberGeneratorId),
                    actualPluginData.getWellState(randomNumberGeneratorId));
        }

        translatorController.writeOutput();

    }
}