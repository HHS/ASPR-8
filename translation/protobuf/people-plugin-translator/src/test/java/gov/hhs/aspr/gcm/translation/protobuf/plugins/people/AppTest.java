package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import nucleus.PluginData;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import util.random.RandomGeneratorProvider;

public class AppTest {

    @Test
    public void testPeopleTranslator() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("people-plugin-translator")) {
            basePath = basePath.resolve("people-plugin-translator");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output");

        outputFilePath.toFile().mkdir();

        String fileName = "pluginData.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(PeopleTranslator.getTranslatorRW(inputFilePath.resolve(fileName).toString(),
                        outputFilePath.resolve(fileName).toString()))
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();
        PeoplePluginData actualPluginData = (PeoplePluginData) pluginDatas.get(0);

        PeoplePluginData.Builder builder = PeoplePluginData.builder();

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6573670690105604419L);

        int numRanges = randomGenerator.nextInt(15);

        for (int i = 0; i < numRanges; i++) {
            PersonRange personRange = new PersonRange(i * 15, (i * 15) + 1);
            builder.addPersonRange(personRange);
        }

        PeoplePluginData expectedPluginData = builder.build();

        assertEquals(expectedPluginData.getPersonCount(), actualPluginData.getPersonCount());

        List<PersonRange> expectedRanges = expectedPluginData.getPersonRanges();
        List<PersonRange> actualRanges = actualPluginData.getPersonRanges();

        for (int i = 0; i < expectedRanges.size(); i++) {
            assertEquals(expectedRanges.get(i), actualRanges.get(i));
        }

        List<PersonId> expectedPersonIds = expectedPluginData.getPersonIds();
        List<PersonId> actualPersonIds = actualPluginData.getPersonIds();

        for (int i = 0; i < expectedPersonIds.size(); i++) {
            assertEquals(expectedPersonIds.get(i), actualPersonIds.get(i));
        }

        translatorController.writeOutput();
    }
}