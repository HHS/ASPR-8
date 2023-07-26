package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.SimpleRegionPropertyIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import plugins.regions.support.SimpleRegionPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_SimpleRegionPropertyIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = SimpleRegionPropertyIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new SimpleRegionPropertyIdTranslationSpec());
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

        SimpleRegionPropertyIdTranslationSpec translationSpec = new SimpleRegionPropertyIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        SimpleRegionPropertyId expectedAppValue = new SimpleRegionPropertyId("test");

        SimpleRegionPropertyIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        SimpleRegionPropertyId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = SimpleRegionPropertyIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        SimpleRegionPropertyIdTranslationSpec translationSpec = new SimpleRegionPropertyIdTranslationSpec();

        assertEquals(SimpleRegionPropertyId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = SimpleRegionPropertyIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        SimpleRegionPropertyIdTranslationSpec translationSpec = new SimpleRegionPropertyIdTranslationSpec();

        assertEquals(SimpleRegionPropertyIdInput.class, translationSpec.getInputObjectClass());
    }
}
