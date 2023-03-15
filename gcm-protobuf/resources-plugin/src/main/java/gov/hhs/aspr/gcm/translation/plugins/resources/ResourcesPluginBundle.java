package gov.hhs.aspr.gcm.translation.plugins.resources;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.people.PeoplePluginBundleId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesPluginBundleId;
import gov.hhs.aspr.gcm.translation.plugins.regions.RegionsPluginBundleId;
import gov.hhs.aspr.gcm.translation.plugins.resources.translators.ResourceIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translators.ResourceInitializationTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translators.ResourcePropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translators.ResourcesPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourcesPluginDataInput;

public class ResourcesPluginBundle {
    private static TranslatorModule.Builder setConstants() {
        return TranslatorModule.builder()
                .setPluginBundleId(ResourcesPluginBundleId.PLUGIN_BUNDLE_ID)
                .addDependency(PeoplePluginBundleId.PLUGIN_BUNDLE_ID)
                .addDependency(PropertiesPluginBundleId.PLUGIN_BUNDLE_ID)
                .addDependency(RegionsPluginBundleId.PLUGIN_BUNDLE_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new ResourcesPluginDataTranslator());
                    translatorContext.addTranslator(new ResourceIdTranslator());
                    translatorContext.addTranslator(new ResourcePropertyIdTranslator());
                    translatorContext.addTranslator(new ResourceInitializationTranslator());
                })
                .setInputObjectType(ResourcesPluginDataInput.getDefaultInstance());
    }

    public static TranslatorModule getPluginBundle(String inputFileName, String outputFileName) {
        return setConstants()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)

                .build();
    }

    public static TranslatorModule getPluginBundle() {
        return setConstants().build();
    }
}
