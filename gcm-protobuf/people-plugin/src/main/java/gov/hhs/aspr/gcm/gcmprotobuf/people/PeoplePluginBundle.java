package gov.hhs.aspr.gcm.gcmprotobuf.people;

import gov.hhs.aspr.gcm.gcmprotobuf.core.PluginBundle;
import gov.hhs.aspr.gcm.gcmprotobuf.people.translators.PeoplePluginDataTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.people.translators.PersonIdTranslator;
import plugins.people.input.PeoplePluginDataInput;
import plugins.people.input.PersonIdInput;

public class PeoplePluginBundle {

    private static PluginBundle.Builder setConstants(PluginBundle.Builder builder) {
        builder.setPluginBundleId(PeoplePluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputObjectType(PeoplePluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PeoplePluginDataTranslator());
                    translatorContext.addTranslator(new PersonIdTranslator());

                    translatorContext.addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("personId"));
                });

        return builder;
    }

    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName) {
        return setConstants(PluginBundle.builder())
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static PluginBundle getPluginBundle() {
        return setConstants(PluginBundle.builder()).build();
    }
}
