package gov.hhs.aspr.gcm.translation.plugins.globalproperties;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.globalproperties.translators.GlobalPropertiesPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.globalproperties.translators.GlobalPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;

public class GlobalPropertiesTranslatorModule {

    private GlobalPropertiesTranslatorModule() {
    }

    private static TranslatorModule.Builder getBaseModule() {
        return TranslatorModule.builder()
                .setPluginBundleId(GlobalPropertiesTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .addDependency(PropertiesTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .setInputObjectType(GlobalPropertiesPluginDataInput.getDefaultInstance())
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new GlobalPropertiesPluginDataTranslator());
                    translatorContext.addTranslator(new GlobalPropertyIdTranslator());
                });
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
