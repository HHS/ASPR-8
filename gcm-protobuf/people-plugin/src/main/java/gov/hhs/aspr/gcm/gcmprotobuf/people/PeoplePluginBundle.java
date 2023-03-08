package gov.hhs.aspr.gcm.gcmprotobuf.people;

import gov.hhs.aspr.gcm.gcmprotobuf.core.PluginBundle;
import gov.hhs.aspr.gcm.gcmprotobuf.people.translators.PeoplePluginDataTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.people.translators.PersonIdTranslator;
import plugins.people.input.PeoplePluginDataInput;

public class PeoplePluginBundle {
    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName) {
        return PluginBundle.builder()
                .setPluginBundleId(PeoplePluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .setInputObjectType(PeoplePluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PeoplePluginDataTranslator());
                    translatorContext.addTranslator(new PersonIdTranslator());
                })
                .build();
    }
}
