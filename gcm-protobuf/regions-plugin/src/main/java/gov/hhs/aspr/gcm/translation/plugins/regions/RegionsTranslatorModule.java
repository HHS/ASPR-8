package gov.hhs.aspr.gcm.translation.plugins.regions;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.RegionIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.RegionPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.RegionsPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.SimpleRegionIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.translators.SimpleRegionPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.input.RegionsPluginDataInput;

public class RegionsTranslatorModule {

    private RegionsTranslatorModule() {

    }

    private static TranslatorModule.Builder getBaseModule() {
        return TranslatorModule.builder()
                .setPluginBundleId(RegionsTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .addDependency(PeopleTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .addDependency(PropertiesTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new RegionsPluginDataTranslator());
                    translatorContext.addTranslator(new RegionIdTranslator());
                    translatorContext.addTranslator(new RegionPropertyIdTranslator());
                    translatorContext.addTranslator(new SimpleRegionIdTranslator());
                    translatorContext.addTranslator(new SimpleRegionPropertyIdTranslator());
                })
                .setInputObjectType(RegionsPluginDataInput.getDefaultInstance());
    }

    public static TranslatorModule getTranslatorModule(String inputFileName, String outputFileName) {
        return getBaseModule()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static TranslatorModule getTranslatorModule() {
        return getBaseModule().build();
    }
}
