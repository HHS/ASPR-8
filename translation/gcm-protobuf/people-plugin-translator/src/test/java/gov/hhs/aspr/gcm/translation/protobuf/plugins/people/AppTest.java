package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.translation.core.TranslatorController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import util.random.RandomGeneratorProvider;

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
    public void testPeopleTranslator() {
        String fileName = "pluginData.json";

        TranslatorController translatorController = TranslatorController.builder()
                .setTranslatorCoreBuilder(ProtobufTranslatorCore.builder())
                .addTranslator(PeopleTranslator.getTranslator())
                .addReader(inputFilePath.resolve(fileName), PeoplePluginDataInput.class)
                .addWriter(outputFilePath.resolve(fileName), PeoplePluginData.class)
                .build();

        translatorController.readInput();
        PeoplePluginData actualPluginData = translatorController.getObject(PeoplePluginData.class);

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