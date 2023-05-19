package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPluginData;
import plugins.regions.testsupport.RegionsTestPluginFactory;
import plugins.util.properties.TimeTrackingPolicy;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_RegionsPluginDataTranslationSpec {

    @Test
    @UnitTestConstructor(target = RegionsPluginDataTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new RegionsPluginDataTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(RegionsTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        RegionsPluginDataTranslationSpec translationSpec = new RegionsPluginDataTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        long seed = 524805676405822016L;
        int initialPopulation = 100;
        List<PersonId> people = new ArrayList<>();

        for (int i = 0; i < initialPopulation; i++) {
            people.add(new PersonId(i));
        }

        RegionsPluginData expectedAppValue = RegionsTestPluginFactory.getStandardRegionsPluginData(people,
                TimeTrackingPolicy.TRACK_TIME, seed);

        RegionsPluginDataInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        RegionsPluginData actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);

        expectedAppValue = RegionsTestPluginFactory.getStandardRegionsPluginData(people,
                TimeTrackingPolicy.DO_NOT_TRACK_TIME, seed);

        inputValue = translationSpec.convertAppObject(expectedAppValue);

        actualAppValue = translationSpec.convertInputObject(inputValue);
        
        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = RegionsPluginDataTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        RegionsPluginDataTranslationSpec translationSpec = new RegionsPluginDataTranslationSpec();

        assertEquals(RegionsPluginData.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = RegionsPluginDataTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        RegionsPluginDataTranslationSpec translationSpec = new RegionsPluginDataTranslationSpec();

        assertEquals(RegionsPluginDataInput.class, translationSpec.getInputObjectClass());
    }
}