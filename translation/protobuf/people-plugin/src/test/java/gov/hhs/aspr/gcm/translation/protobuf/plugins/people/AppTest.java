package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import nucleus.PluginData;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;

public class AppTest {

    @Test
    public void testPeopleTranslator() {

        String inputFileName = "./people-plugin/src/main/resources/json/input.json";
        String outputFileName = "./people-plugin/src/main/resources/json/output/output.json";

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(PeopleTranslator.getTranslatorRW(inputFileName, outputFileName))
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();
        PeoplePluginData peoplePluginData = (PeoplePluginData) pluginDatas.get(0);

        int initialPopulation = 100;

        // add 1 to actual because of i % 2 resulting in a null for index 99, precluding
        // it from the list
        assertEquals(initialPopulation, peoplePluginData.getPersonIds().size() + 1);

        List<PersonId> expectedPersonIds = new ArrayList<>();
        for (int i = 0; i < initialPopulation; i++) {
            if (i % 2 == 0)
                expectedPersonIds.add(new PersonId(i));
            else
                expectedPersonIds.add(null);
        }

        for (int i = 0; i < peoplePluginData.getPersonIds().size(); i++) {
            PersonId actualPersonId = peoplePluginData.getPersonIds().get(i);
            PersonId expectedPersonid = expectedPersonIds.get(i);

            assertEquals(expectedPersonid, actualPersonId);

        }

        translatorController.writeOutput();
    }
}