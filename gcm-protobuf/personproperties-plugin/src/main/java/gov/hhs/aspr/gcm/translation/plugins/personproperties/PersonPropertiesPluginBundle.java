package gov.hhs.aspr.gcm.translation.plugins.personproperties;

import gov.hhs.aspr.gcm.translation.core.PluginBundle;
import gov.hhs.aspr.gcm.translation.plugins.people.PeoplePluginBundleId;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translators.PersonPropertiesPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.translators.PersonPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesPluginBundleId;
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
