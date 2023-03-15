package gov.hhs.aspr.gcm.translation.plugins.people;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.people.translators.PeoplePluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.translators.PersonIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.people.input.PersonIdInput;

public class PeoplePluginBundle {

    private static TranslatorModule.Builder setConstants(TranslatorModule.Builder builder) {
        builder.setPluginBundleId(PeoplePluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputObjectType(PeoplePluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PeoplePluginDataTranslator());
                    translatorContext.addTranslator(new PersonIdTranslator());

                    translatorContext.addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("id"));
                });

        return builder;
    }

    public static TranslatorModule getPluginBundle(String inputFileName, String outputFileName) {
        return setConstants(TranslatorModule.builder())
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static TranslatorModule getPluginBundle() {
        return setConstants(TranslatorModule.builder()).build();
    }
}
