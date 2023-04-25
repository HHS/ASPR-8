package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertiesPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyInteractionReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.PersonPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs.TestPersonPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;

public class PersonPropertiesTranslator {

    private PersonPropertiesTranslator() {
    }

    public static Translator.Builder builder(boolean withReport) {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(PersonPropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder.addTranslatorSpec(new PersonPropertyIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new PersonPropertiesPluginDataTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new TestPersonPropertyIdTranslationSpec());

                    if (withReport) {
                        translationEngineBuilder.addTranslatorSpec(new PersonPropertyReportPluginDataTranslationSpec());
                        translationEngineBuilder.addTranslatorSpec(new PersonPropertyInteractionReportPluginDataTranslationSpec());
                    }
                });

        if (withReport) {
            builder.addDependency(ReportsTranslatorId.TRANSLATOR_ID);
        }

        return builder;
    }

    public static Translator getTranslatorWithReport() {
        return builder(true).build();
    }

    public static Translator getTranslator() {
        return builder(false).build();
    }
}