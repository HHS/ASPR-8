package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyValueInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertiesPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyInteractionReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.TestPersonPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestMethod;

public class AT_PersonPropertiesTranslator {

    @Test
    @UnitTestMethod(target = PersonPropertiesTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(PersonPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder.addTranslationSpec(new PersonPropertyIdTranslationSpec())
                            .addTranslationSpec(new PersonPropertiesPluginDataTranslationSpec())
                            .addTranslationSpec(new TestPersonPropertyIdTranslationSpec());

                    translationEngineBuilder.addFieldToIncludeDefaultValue(
                            PersonPropertyValueInput.getDescriptor().findFieldByName("personId"));

                }).build();

        assertEquals(expectedTranslator, PersonPropertiesTranslator.getTranslator());
    }

    @Test
    @UnitTestMethod(target = PersonPropertiesTranslator.class, name = "getTranslatorWithReport", args = {})
    public void testGetTranslatorWithReport() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(PersonPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new PersonPropertyIdTranslationSpec())
                            .addTranslationSpec(new PersonPropertiesPluginDataTranslationSpec())
                            .addTranslationSpec(new PersonPropertyReportPluginDataTranslationSpec())
                            .addTranslationSpec(new PersonPropertyInteractionReportPluginDataTranslationSpec())
                            .addTranslationSpec(new TestPersonPropertyIdTranslationSpec());

                    translationEngineBuilder.addFieldToIncludeDefaultValue(
                            PersonPropertyValueInput.getDescriptor().findFieldByName("personId"));

                }).build();

        assertEquals(expectedTranslator, PersonPropertiesTranslator.getTranslatorWithReport());
    }
}
