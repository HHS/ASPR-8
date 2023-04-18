package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertiesPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertyInteractionReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.TestPersonPropertyIdTranslatorSpec;
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
                    translatorContext.addTranslatorSpec(new PersonPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PersonPropertiesPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestPersonPropertyIdTranslatorSpec());

                    if (withReport) {
                        translatorContext.addTranslatorSpec(new PersonPropertyReportPluginDataTranslatorSpec());
                        translatorContext
                                .addTranslatorSpec(new PersonPropertyInteractionReportPluginDataTranslatorSpec());
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