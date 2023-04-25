package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
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
                    ProtobufTranslatorCore.Builder coreBuilder = translatorContext
                            .getTranslatorCoreBuilder(ProtobufTranslatorCore.Builder.class);

                    coreBuilder.addTranslatorSpec(new PersonPropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new PersonPropertiesPluginDataTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestPersonPropertyIdTranslatorSpec());

                    if (withReport) {
                        coreBuilder.addTranslatorSpec(new PersonPropertyReportPluginDataTranslatorSpec());
                        coreBuilder.addTranslatorSpec(new PersonPropertyInteractionReportPluginDataTranslatorSpec());
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
