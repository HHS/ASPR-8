package gov.hhs.aspr.gcm.gcmprotobuf.personproperties;

import gov.hhs.aspr.gcm.gcmprotobuf.core.PluginBundle;
import gov.hhs.aspr.gcm.gcmprotobuf.people.PeoplePluginBundleId;
import gov.hhs.aspr.gcm.gcmprotobuf.personproperties.translators.PersonPropertiesPluginDataTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.personproperties.translators.PersonPropertyIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.PropertiesPluginBundleId;
import plugins.personproperties.input.PersonPropertiesPluginDataInput;

public class PersonPropertiesPluginBundle {
    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName) {
        return PluginBundle.builder()
                .setPluginBundleId(PersonPropertiesPluginBundleId.PLUGIN_BUNDLE_ID)
                .addDependency(PropertiesPluginBundleId.PLUGIN_BUNDLE_ID)
                .addDependency(PeoplePluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .setInputObjectType(PersonPropertiesPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new PersonPropertiesPluginDataTranslator());
                    translatorContext.addTranslator(new PersonPropertyIdTranslator());
                })
                .build();
    }
}
