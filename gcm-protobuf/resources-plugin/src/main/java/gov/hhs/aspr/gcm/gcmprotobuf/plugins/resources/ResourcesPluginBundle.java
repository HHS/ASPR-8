package gov.hhs.aspr.gcm.gcmprotobuf.plugins.resources;

import gov.hhs.aspr.gcm.gcmprotobuf.core.PluginBundle;
import gov.hhs.aspr.gcm.gcmprotobuf.people.PeoplePluginBundleId;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.regions.RegionsPluginBundleId;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.resources.translators.ResourceIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.resources.translators.ResourceInitializationTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.resources.translators.ResourcePropertyIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.resources.translators.ResourcesPluginDataTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.PropertiesPluginBundleId;
import plugins.resources.input.ResourcesPluginDataInput;

public class ResourcesPluginBundle {
    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName) {
        return PluginBundle.builder()
        .setInputFileName(inputFileName)
        .setOutputFileName(outputFileName)
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
        .setInputObjectType(ResourcesPluginDataInput.getDefaultInstance())
        .build();
    }
}
