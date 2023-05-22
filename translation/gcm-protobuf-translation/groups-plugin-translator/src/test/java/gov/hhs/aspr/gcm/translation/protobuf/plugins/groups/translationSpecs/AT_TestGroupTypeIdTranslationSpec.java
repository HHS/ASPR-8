package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.GroupsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.TestGroupTypeIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.groups.testsupport.TestGroupTypeId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TestGroupTypeIdTranslationSpec {
    
    @Test
    @UnitTestConstructor(target = TestGroupTypeIdTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestGroupTypeIdTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(GroupsTranslator.getTranslatorWithReport())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        TestGroupTypeIdTranslationSpec translationSpec = new TestGroupTypeIdTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        TestGroupTypeId expectedAppValue = TestGroupTypeId.GROUP_TYPE_1;

        TestGroupTypeIdInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        TestGroupTypeId actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = TestGroupTypeIdTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestGroupTypeIdTranslationSpec translationSpec = new TestGroupTypeIdTranslationSpec();

        assertEquals(TestGroupTypeId.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestGroupTypeIdTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestGroupTypeIdTranslationSpec translationSpec = new TestGroupTypeIdTranslationSpec();

        assertEquals(TestGroupTypeIdInput.class, translationSpec.getInputObjectClass());
    }
}
