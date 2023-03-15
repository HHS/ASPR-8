package gov.hhs.aspr.gcm.translation.plugins.materials;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translators.BatchIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translators.BatchPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translators.MaterialIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translators.MaterialsPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translators.MaterialsProducerIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translators.MaterialsProducerPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translators.StageIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.resources.ResourcesTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.BatchIdInput;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.MaterialsPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.StageIdInput;

public class MaterialsTranslatorModule {
    private MaterialsTranslatorModule() {

    }

    private static Translator.Builder getBaseModule() {
        return Translator.builder()
                .setPluginBundleId(MaterialsTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .setInputObjectType(MaterialsPluginDataInput.getDefaultInstance())
                .addDependency(PropertiesTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .addDependency(ResourcesTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new MaterialsPluginDataTranslator());
                    translatorContext.addTranslator(new MaterialIdTranslator());
                    translatorContext.addTranslator(new MaterialsProducerIdTranslator());
                    translatorContext.addTranslator(new MaterialsProducerPropertyIdTranslator());
                    translatorContext.addTranslator(new BatchIdTranslator());
                    translatorContext.addTranslator(new StageIdTranslator());
                    translatorContext.addTranslator(new BatchPropertyIdTranslator());

                    translatorContext
                            .addFieldToIncludeDefaultValue(BatchIdInput.getDescriptor().findFieldByName("id"));
                    translatorContext
                            .addFieldToIncludeDefaultValue(StageIdInput.getDescriptor().findFieldByName("id"));
                });

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
