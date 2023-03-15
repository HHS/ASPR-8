package gov.hhs.aspr.gcm.translation.plugins.regions;

import gov.hhs.aspr.gcm.translation.core.PluginBundle;
import gov.hhs.aspr.gcm.translation.plugins.people.PeoplePluginBundleId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesPluginBundleId;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.RegionIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.RegionPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.RegionsPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.SimpleRegionIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.SimpleRegionPropertyIdTranslator;
import plugins.regions.input.RegionsPluginDataInput;

public class RegionsPluginBundle {

    private static PluginBundle.Builder setConstants(PluginBundle.Builder builder) {
        return builder
                .setPluginBundleId(RegionsPluginBundleId.PLUGIN_BUNDLE_ID)
                .addDependency(PeoplePluginBundleId.PLUGIN_BUNDLE_ID)
                .addDependency(PropertiesPluginBundleId.PLUGIN_BUNDLE_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new RegionsPluginDataTranslator());
                    translatorContext.addTranslator(new RegionIdTranslator());
                    translatorContext.addTranslator(new RegionPropertyIdTranslator());
                    translatorContext.addTranslator(new SimpleRegionIdTranslator());
                    translatorContext.addTranslator(new SimpleRegionPropertyIdTranslator());
                })
                .setInputObjectType(RegionsPluginDataInput.getDefaultInstance());
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
