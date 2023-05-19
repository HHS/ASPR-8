package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_ResourceIdTranslationSpec {

    @Test
    @UnitTestConstructor(target = ResourceIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ResourceIdTranslationSpec());
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

        ResourceIdTranslationSpec translationSpec = new ResourceIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        ResourceId expectedAppValue = TestResourceId.RESOURCE_1;

        ResourceIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        ResourceId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = ResourceIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ResourceIdTranslationSpec translationSpec = new ResourceIdTranslationSpec();

        assertEquals(ResourceId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ResourceIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ResourceIdTranslationSpec translationSpec = new ResourceIdTranslationSpec();

        assertEquals(ResourceIdInput.class, translationSpec.getInputObjectClass());
    }
}
