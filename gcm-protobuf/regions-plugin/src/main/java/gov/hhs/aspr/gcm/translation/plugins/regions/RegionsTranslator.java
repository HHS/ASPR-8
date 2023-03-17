package gov.hhs.aspr.gcm.translation.plugins.regions;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.regions.input.RegionsPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.regions.translatorSpecs.RegionIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.translatorSpecs.RegionPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.translatorSpecs.RegionsPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.translatorSpecs.SimpleRegionIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.translatorSpecs.SimpleRegionPropertyIdTranslatorSpec;
import plugins.regions.RegionsPluginData;

public class RegionsTranslator {

    private RegionsTranslator() {

    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(RegionsTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new RegionsPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new RegionIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new RegionPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new SimpleRegionIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new SimpleRegionPropertyIdTranslatorSpec());
                });
    }

    public static Translator getTranslatorRW(String inputFileName, String outputFileName) {
        return builder()
                .addInputFile(inputFileName, RegionsPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, RegionsPluginData.class)
                .build();
    }

    public static Translator getTranslatorR(String inputFileName) {
        return builder()
                .addInputFile(inputFileName, RegionsPluginDataInput.getDefaultInstance())
                .build();
    }

    public static Translator getTranslatorW(String outputFileName) {
        return builder()
                .addOutputFile(outputFileName, RegionsPluginData.class)
                .build();
    }

    public static Translator getTranslatorModule() {
        return builder().build();
    }
}
