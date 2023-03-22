package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs.TestRegionIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.TestResourceIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.TestResourcePropertyIdTranslatorSpec;
import nucleus.PluginData;
import plugins.people.support.PersonId;
import plugins.resources.ResourcesPluginData;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import util.random.RandomGeneratorProvider;

public class AppTest {

    @Test
    public void testResourcesTranslator() {
        Path basePath = Path.of("").toAbsolutePath();

        if (!basePath.endsWith("resources-plugin")) {
            basePath = basePath.resolve("resources-plugin");
        }

        Path inputFilePath = basePath.resolve("src/main/resources/json/input.json");
        Path outputFilePath = basePath.resolve("src/main/resources/json/output/output.json");

        TranslatorController translatorController = TranslatorController.builder()
                .addTranslator(ResourcesTranslator.getTranslatorRW(inputFilePath.toString(), outputFilePath.toString()))
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslatorModule())
                .addTranslatorSpec(new TestResourceIdTranslatorSpec())
                .addTranslatorSpec(new TestResourcePropertyIdTranslatorSpec())
                .addTranslatorSpec(new TestRegionIdTranslatorSpec())
                .build();

        List<PluginData> pluginDatas = translatorController.readInput().getPluginDatas();

        ResourcesPluginData resourcesPluginData = (ResourcesPluginData) pluginDatas.get(0);

        long seed = 524805676405822016L;
        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            people.add(new PersonId(i));
        }

        Set<TestResourceId> expectedResourceIds = EnumSet.allOf(TestResourceId.class);
        assertFalse(expectedResourceIds.isEmpty());

        Set<ResourceId> actualResourceIds = resourcesPluginData.getResourceIds();
        assertEquals(expectedResourceIds, actualResourceIds);

        for (TestResourceId resourceId : TestResourceId.values()) {
            TimeTrackingPolicy expectedPolicy = resourceId.getTimeTrackingPolicy();
            TimeTrackingPolicy actualPolicy = resourcesPluginData.getPersonResourceTimeTrackingPolicy(resourceId);
            assertEquals(expectedPolicy, actualPolicy);
        }

        Set<TestResourcePropertyId> expectedResourcePropertyIds = EnumSet.allOf(TestResourcePropertyId.class);
        assertFalse(expectedResourcePropertyIds.isEmpty());

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        for (TestResourcePropertyId resourcePropertyId : TestResourcePropertyId.values()) {
            TestResourceId expectedResourceId = resourcePropertyId.getTestResourceId();
            PropertyDefinition expectedPropertyDefinition = resourcePropertyId.getPropertyDefinition();
            Object expectedPropertyValue = resourcePropertyId.getRandomPropertyValue(randomGenerator);

            assertTrue(resourcesPluginData.getResourcePropertyIds(expectedResourceId).contains(resourcePropertyId));

            PropertyDefinition actualPropertyDefinition = resourcesPluginData
                    .getResourcePropertyDefinition(expectedResourceId, resourcePropertyId);
            assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

            Object actualPropertyValue = resourcesPluginData.getResourcePropertyValue(expectedResourceId,
                    resourcePropertyId);
            assertEquals(expectedPropertyValue, actualPropertyValue);
        }

        translatorController.writeOutput();
    }

}
