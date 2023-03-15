package gov.hhs.aspr.gcm.translation.plugins.resources;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.regions.RegionsTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.resources.translators.ResourceIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translators.ResourceInitializationTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translators.ResourcePropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.translators.ResourcesPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.resources.input.ResourcesPluginDataInput;

public class ResourcesTranslatorModule {
    private ResourcesTranslatorModule() {

    }

    private static Translator.Builder getBaseModule() {
        return Translator.builder()
                .setPluginBundleId(ResourcesTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .addDependency(PeopleTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .addDependency(PropertiesTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .addDependency(RegionsTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new ResourcesPluginDataTranslator());
                    translatorContext.addTranslator(new ResourceIdTranslator());
                    translatorContext.addTranslator(new ResourcePropertyIdTranslator());
                    translatorContext.addTranslator(new ResourceInitializationTranslator());
                })
                .setInputObjectType(ResourcesPluginDataInput.getDefaultInstance());
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
