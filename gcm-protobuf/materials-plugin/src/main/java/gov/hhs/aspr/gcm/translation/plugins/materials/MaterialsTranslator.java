package gov.hhs.aspr.gcm.translation.plugins.materials;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.resources.ResourcesTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.BatchIdInput;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.MaterialsPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.StageIdInput;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.BatchIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.BatchPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.MaterialIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.MaterialsPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.MaterialsProducerIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.MaterialsProducerPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs.StageIdTranslator;

public class MaterialsTranslator {
    private MaterialsTranslator() {

    }

    private static Translator.Builder getBaseTranslator() {
        return Translator.builder()
                .setPluginBundleId(MaterialsTranslatorId.TRANSLATOR_ID)
                .setInputObjectType(MaterialsPluginDataInput.getDefaultInstance())
                .addDependency(PropertiesTranslatorModuleId.TRANSLATOR_ID)
                .addDependency(ResourcesTranslatorModuleId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new MaterialsPluginDataTranslator());
                    translatorContext.addTranslatorSpec(new MaterialIdTranslator());
                    translatorContext.addTranslatorSpec(new MaterialsProducerIdTranslator());
                    translatorContext.addTranslatorSpec(new MaterialsProducerPropertyIdTranslator());
                    translatorContext.addTranslatorSpec(new BatchIdTranslator());
                    translatorContext.addTranslatorSpec(new StageIdTranslator());
                    translatorContext.addTranslatorSpec(new BatchPropertyIdTranslator());

                    translatorContext
                            .addFieldToIncludeDefaultValue(BatchIdInput.getDescriptor().findFieldByName("id"));
                    translatorContext
                            .addFieldToIncludeDefaultValue(StageIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static Translator getTranslator(String inputFileName, String outputFileName) {
        return getBaseTranslator()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static Translator getTranslator() {
        return getBaseTranslator().build();
    }
}
