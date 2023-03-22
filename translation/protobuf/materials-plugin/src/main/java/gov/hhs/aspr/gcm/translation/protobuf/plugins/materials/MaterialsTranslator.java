package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials;

import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.BatchIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.BatchPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.MaterialIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.MaterialsPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.MaterialsProducerIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.MaterialsProducerPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.StageIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslatorId;
import plugins.materials.MaterialsPluginData;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.BatchIdInput;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.MaterialsPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.StageIdInput;

public class MaterialsTranslator {
    private MaterialsTranslator() {

    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(MaterialsTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(ResourcesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new MaterialsPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new MaterialIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new MaterialsProducerIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new MaterialsProducerPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new BatchIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new StageIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new BatchPropertyIdTranslatorSpec());

                    translatorContext
                            .addFieldToIncludeDefaultValue(BatchIdInput.getDescriptor().findFieldByName("id"));
                    translatorContext
                            .addFieldToIncludeDefaultValue(StageIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static Translator getTranslatorRW(String inputFileName, String outputFileName) {
        return builder()
                .addInputFile(inputFileName, MaterialsPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, MaterialsPluginData.class)
                .build();
    }

    public static Translator getTranslatorR(String inputFileName) {
        return builder()
                .addInputFile(inputFileName, MaterialsPluginDataInput.getDefaultInstance())
                .build();
    }

    public static Translator getTranslatorW(String outputFileName) {
        return builder()
                .addOutputFile(outputFileName, MaterialsPluginData.class)
                .build();
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
