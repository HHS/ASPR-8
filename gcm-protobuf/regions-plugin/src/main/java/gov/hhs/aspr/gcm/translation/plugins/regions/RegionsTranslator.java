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

    private static Translator.Builder getBaseTranslatorBuilder() {
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

    public static Translator getTranslatorModule(String inputFileName, String outputFileName) {
        return getBaseTranslatorBuilder()
                .addInputFile(inputFileName, RegionsPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, RegionsPluginData.class)
                .build();
    }

    public static Translator getTranslatorModule() {
        return getBaseTranslatorBuilder().build();
    }
}
