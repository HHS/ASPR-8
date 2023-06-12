package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.TestResourceHelper;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonRange;
import util.annotations.UnitTestForCoverage;
import util.random.RandomGeneratorProvider;

public class IT_PeopleTranslator {
    Path basePath = TestResourceHelper.getResourceDir(this.getClass());
    Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

    @Test
    @UnitTestForCoverage
    public void testPeopleTranslator() {
        String fileName = "pluginData.json";

        TestResourceHelper.createTestOutputFile(filePath, fileName);

        TranslationController translatorController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(PeopleTranslator.getTranslator())
                .addInputFilePath(filePath.resolve(fileName), PeoplePluginDataInput.class)
                .addOutputFilePath(filePath.resolve(fileName), PeoplePluginData.class)
                .build();

        PeoplePluginData.Builder builder = PeoplePluginData.builder();
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6573670690105604419L);

        int numRanges = randomGenerator.nextInt(15);

        for (int i = 0; i < numRanges; i++) {
            PersonRange personRange = new PersonRange(i * 15, (i * 15) + 1);
            builder.addPersonRange(personRange);
        }

        PeoplePluginData expectedPluginData = builder.build();

        translatorController.writeOutput(expectedPluginData);
        translatorController.readInput();

        PeoplePluginData actualPluginData = translatorController.getFirstObject(PeoplePluginData.class);

        assertEquals(expectedPluginData, actualPluginData);
        assertEquals(expectedPluginData.toString(), actualPluginData.toString());
    }
}