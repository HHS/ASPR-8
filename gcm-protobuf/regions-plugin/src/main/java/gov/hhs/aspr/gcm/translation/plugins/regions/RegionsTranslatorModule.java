package gov.hhs.aspr.gcm.translation.plugins.regions;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorId;
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
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorModuleId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new RegionsPluginDataTranslator());
                    translatorContext.addTranslatorSpec(new RegionIdTranslator());
                    translatorContext.addTranslatorSpec(new RegionPropertyIdTranslator());
                    translatorContext.addTranslatorSpec(new SimpleRegionIdTranslator());
                    translatorContext.addTranslatorSpec(new SimpleRegionPropertyIdTranslator());
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
