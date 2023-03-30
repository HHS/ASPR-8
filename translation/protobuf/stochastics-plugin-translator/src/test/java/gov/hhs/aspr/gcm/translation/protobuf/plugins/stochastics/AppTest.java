package gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import nucleus.PluginData;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.RandomNumberGeneratorId;
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
                .addTranslator(
                        StochasticsTranslator.getTranslatorRW(inputFilePath.resolve(fileName).toString(), outputFilePath.resolve(fileName).toString()))
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        StochasticsPluginData stochasticsPluginData = (StochasticsPluginData) pluginDatas.get(0);

        long seed = 524805676405822016L;

        assertEquals(seed, stochasticsPluginData.getSeed());

        Set<TestRandomGeneratorId> expectedRandomGeneratorIds = EnumSet.allOf(TestRandomGeneratorId.class);
        assertFalse(expectedRandomGeneratorIds.isEmpty());

        Set<RandomNumberGeneratorId> actualsGeneratorIds = stochasticsPluginData.getRandomNumberGeneratorIds();

        assertEquals(expectedRandomGeneratorIds, actualsGeneratorIds);

        translatorController.writeOutput();
    }
}