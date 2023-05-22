package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcesPluginDataInput;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.ResourcesPluginData;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_ResourcesPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = ResourcesPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ResourcesPluginDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(ResourcesTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        ResourcesPluginDataTranslationSpec translationSpec = new ResourcesPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        long seed = 524805676405822016L;

        int initialPopulation = 100;
        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

        ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

        for (TestResourceId testResourceId : TestResourceId.values()) {
            builder.addResource(testResourceId, 0.0);
            builder.setResourceTimeTracking(testResourceId, testResourceId.getTimeTrackingPolicy());
            for (PersonId personId : people) {
                if (randomGenerator.nextBoolean()) {
                    builder.setPersonResourceLevel(personId, testResourceId, randomGenerator.nextInt(10));
                }
                if (randomGenerator.nextBoolean()) {
                    builder.setPersonResourceTime(personId, testResourceId, 0.0);
                }
            }

            for (RegionId regionId : TestRegionId.values()) {
                if (randomGenerator.nextBoolean()) {
                    builder.setRegionResourceLevel(regionId, testResourceId, randomGenerator.nextInt(10));
                } else {
                    builder.setRegionResourceLevel(regionId, testResourceId, 0);
                }
            }
        }

        for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
            TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
            PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
            Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
            builder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
            builder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
        }

        ResourcesPluginData expectedAppValue = builder.build();

        ResourcesPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        ResourcesPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = ResourcesPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ResourcesPluginDataTranslationSpec translationSpec = new ResourcesPluginDataTranslationSpec();

        assertEquals(ResourcesPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ResourcesPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ResourcesPluginDataTranslationSpec translationSpec = new ResourcesPluginDataTranslationSpec();

        assertEquals(ResourcesPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}
