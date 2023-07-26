package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.PersonPropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyDimensionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.personproperties.support.PersonPropertyDimension;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_PersonPropertyDimensionTranslationSpec {

    @Test
    @UnitTestConstructor(target = PersonPropertyDimensionTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PersonPropertyDimensionTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertObject() {
        TranslationController translationController = TranslationController.builder()
                .setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
                .addTranslator(PersonPropertiesTranslator.getTranslator())
                .addTranslator(PropertiesTranslator.getTranslator())
                .addTranslator(PeopleTranslator.getTranslator())
                .addTranslator(ReportsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        PersonPropertyDimensionTranslationSpec translationSpec = new PersonPropertyDimensionTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        PersonPropertyDimension expectedAppValue = PersonPropertyDimension
                .builder()
                .setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK)
                .setTrackTimes(true)
                .addValue(10.0)
                .addValue(1250.2)
                .addValue(15000.5)
                .build();

        PersonPropertyDimensionInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        PersonPropertyDimension actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PersonPropertyDimensionTranslationSpec translationSpec = new PersonPropertyDimensionTranslationSpec();

        assertEquals(PersonPropertyDimension.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyDimensionTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PersonPropertyDimensionTranslationSpec translationSpec = new PersonPropertyDimensionTranslationSpec();

        assertEquals(PersonPropertyDimensionInput.class, translationSpec.getInputObjectClass());
    }
}
