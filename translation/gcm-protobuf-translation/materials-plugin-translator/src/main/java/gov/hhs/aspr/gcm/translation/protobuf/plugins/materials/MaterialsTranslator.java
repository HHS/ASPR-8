package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.StageIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.BatchIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.BatchPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.BatchStatusReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialsPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialsProducerIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialsProducerPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialsProducerPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialsProducerResourceReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.StageIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.StageReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.TestBatchPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.TestMaterialIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.TestMaterialsProducerIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.TestMaterialsProducerPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslatorId;

public class MaterialsTranslator {

    private MaterialsTranslator() {
    }

    public static Translator.Builder builder(boolean withReport) {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(MaterialsTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(ResourcesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder.addTranslatorSpec(new MaterialsPluginDataTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new MaterialIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new MaterialsProducerIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new MaterialsProducerPropertyIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new BatchIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new StageIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new BatchPropertyIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new TestBatchPropertyIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new TestMaterialIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new TestMaterialsProducerIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new TestMaterialsProducerPropertyIdTranslationSpec());

                    if (withReport) {
                        translationEngineBuilder.addTranslatorSpec(new BatchStatusReportPluginDataTranslationSpec());
                        translationEngineBuilder.addTranslatorSpec(new MaterialsProducerPropertyReportPluginDataTranslationSpec());
                        translationEngineBuilder.addTranslatorSpec(new MaterialsProducerResourceReportPluginDataTranslationSpec());
                        translationEngineBuilder.addTranslatorSpec(new StageReportPluginDataTranslationSpec());
                    }

                    translationEngineBuilder
                            .addFieldToIncludeDefaultValue(BatchIdInput.getDescriptor().findFieldByName("id"))
                            .addFieldToIncludeDefaultValue(StageIdInput.getDescriptor().findFieldByName("id"));
                });

        if (withReport) {
            builder.addDependency(ReportsTranslatorId.TRANSLATOR_ID);
        }

        return builder;
    }

    public static Translator getTranslatorWithReport() {
        return builder(true).build();
    }

    public static Translator getTranslator() {
        return builder(false).build();
    }
}
