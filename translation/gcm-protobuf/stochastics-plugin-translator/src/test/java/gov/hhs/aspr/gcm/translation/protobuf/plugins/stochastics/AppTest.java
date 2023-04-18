package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.input.StochasticsPluginDataInput;
import gov.hhs.aspr.translation.core.TranslatorController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.support.Well;
import plugins.stochastics.support.WellState;
import plugins.stochastics.testsupport.StochasticsTestPluginFactory;
import plugins.stochastics.testsupport.TestRandomGeneratorId;

public class AppTest {
    Path basePath = getCurrentDir();
    Path inputFilePath = basePath.resolve("json");
    Path outputFilePath = makeOutputDir();

    private Path getCurrentDir() {
        try {
            return Path.of(this.getClass().getClassLoader().getResource("").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Path makeOutputDir() {
        Path path = basePath.resolve("json/output");

        path.toFile().mkdirs();

        return path;
    }

    @Test
    public void testStochasticsTranslator() {
        String fileName = "pluginData.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(StochasticsTranslator.getTranslator())
                .addInputFilePath(inputFilePath.resolve(fileName), StochasticsPluginDataInput.class)
                .addOutputFilePath(outputFilePath.resolve(fileName), StochasticsPluginData.class)
                .build();

        translatorController.readInput();

        StochasticsPluginData actualPluginData = translatorController.getObject(StochasticsPluginData.class);

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