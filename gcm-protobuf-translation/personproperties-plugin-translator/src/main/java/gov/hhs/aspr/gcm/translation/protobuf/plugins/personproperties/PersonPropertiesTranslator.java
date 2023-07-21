package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import gov.hhs.aspr.translation.core.TranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyValueInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertiesPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyDimensionTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyInteractionReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.TestPersonPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;

/**
 * Translator for the PersonProperties Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * PersonPropertiesPluginData
 */
public class PersonPropertiesTranslator {

    private PersonPropertiesTranslator() {
    }

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new PersonPropertiesPluginDataTranslationSpec());
        list.add(new PersonPropertyDimensionTranslationSpec());
        list.add(new PersonPropertyFilterTranslationSpec());
        list.add(new PersonPropertyIdTranslationSpec());
        list.add(new PersonPropertyInteractionReportPluginDataTranslationSpec());
        list.add(new PersonPropertyReportPluginDataTranslationSpec());
        list.add(new TestPersonPropertyIdTranslationSpec());

        return list;
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(PersonPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    for (TranslationSpec<?, ?> translationSpec : getTranslationSpecs()) {
                        translationEngineBuilder.addTranslationSpec(translationSpec);
                    }

                    translationEngineBuilder.addFieldToIncludeDefaultValue(
                            PersonPropertyValueInput.getDescriptor().findFieldByName("personId"));

                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
