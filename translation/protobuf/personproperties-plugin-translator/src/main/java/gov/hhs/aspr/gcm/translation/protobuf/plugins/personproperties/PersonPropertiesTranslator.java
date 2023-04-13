package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertiesPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertyInteractionReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.PersonPropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs.TestPersonPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;

public class PersonPropertiesTranslator {
    private PersonPropertiesTranslator() {
    }

    public static Translator.Builder builder(boolean withReport) {
        return Translator.builder()
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

    }

    public static Translator.Builder builder() {
        return builder(false);
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
