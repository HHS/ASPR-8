package gov.hhs.aspr.gcm.translation.plugins.regions;

import gov.hhs.aspr.gcm.translation.core.Translator;
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

    private static Translator.Builder getBaseModule() {
        return Translator.builder()
                .setPluginBundleId(RegionsTranslatorModuleId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorModuleId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorModuleId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new RegionsPluginDataTranslator());
                    translatorContext.addTranslator(new RegionIdTranslator());
                    translatorContext.addTranslator(new RegionPropertyIdTranslator());
                    translatorContext.addTranslator(new SimpleRegionIdTranslator());
                    translatorContext.addTranslator(new SimpleRegionPropertyIdTranslator());
                })
                .setInputObjectType(RegionsPluginDataInput.getDefaultInstance());
    }

    public static Translator getTranslatorModule(String inputFileName, String outputFileName) {
        return getBaseModule()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static Translator getTranslatorModule() {
        return getBaseModule().build();
    }
}
