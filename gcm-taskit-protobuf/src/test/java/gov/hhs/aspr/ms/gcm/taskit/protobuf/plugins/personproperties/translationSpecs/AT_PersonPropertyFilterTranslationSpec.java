package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.PartitionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.PersonPropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import plugins.partitions.support.Equality;
import plugins.personproperties.support.PersonPropertyFilter;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_PersonPropertyFilterTranslationSpec {

    @Test
    @UnitTestConstructor(target = PersonPropertyFilterTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new PersonPropertyFilterTranslationSpec());
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
                .addTranslator(PartitionsTranslator.getTranslator())
                .build();

        ProtobufTranslationEngine protobufTranslationEngine = translationController
                .getTranslationEngine(ProtobufTranslationEngine.class);

        PersonPropertyFilterTranslationSpec translationSpec = new PersonPropertyFilterTranslationSpec();
        translationSpec.init(protobufTranslationEngine);

        Equality equality = Equality.LESS_THAN;
        PersonPropertyFilter expectedAppValue = new PersonPropertyFilter(
                TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, equality, false);

        PersonPropertyFilterInput inputValue = translationSpec.convertAppObject(expectedAppValue);

        PersonPropertyFilter actualAppValue = translationSpec.convertInputObject(inputValue);

        assertEquals(expectedAppValue, actualAppValue);
    }

    @Test
    @UnitTestMethod(target = PersonPropertyFilterTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        PersonPropertyFilterTranslationSpec translationSpec = new PersonPropertyFilterTranslationSpec();

        assertEquals(PersonPropertyFilter.class, translationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = PersonPropertyFilterTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        PersonPropertyFilterTranslationSpec translationSpec = new PersonPropertyFilterTranslationSpec();

        assertEquals(PersonPropertyFilterInput.class, translationSpec.getInputObjectClass());
    }
}
