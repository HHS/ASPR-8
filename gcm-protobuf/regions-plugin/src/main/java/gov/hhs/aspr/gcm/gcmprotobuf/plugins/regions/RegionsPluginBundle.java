package gov.hhs.aspr.gcm.gcmprotobuf.plugins.regions;

import gov.hhs.aspr.gcm.gcmprotobuf.core.PluginBundle;
import gov.hhs.aspr.gcm.gcmprotobuf.people.PeoplePluginBundleId;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.regions.translators.RegionIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.regions.translators.RegionPropertyIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.regions.translators.RegionsPluginDataTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.regions.translators.SimpleRegionIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.regions.translators.SimpleRegionPropertyIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.PropertiesPluginBundleId;
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
