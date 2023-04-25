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
                    ProtobufTranslationEngine.Builder coreBuilder = translatorContext
                            .getTranslatorCoreBuilder(ProtobufTranslationEngine.Builder.class);

                    coreBuilder.addTranslatorSpec(new MaterialsPluginDataTranslationSpec());
                    coreBuilder.addTranslatorSpec(new MaterialIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new MaterialsProducerIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new MaterialsProducerPropertyIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new BatchIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new StageIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new BatchPropertyIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new TestBatchPropertyIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new TestMaterialIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new TestMaterialsProducerIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new TestMaterialsProducerPropertyIdTranslationSpec());

                    if (withReport) {
                        coreBuilder.addTranslatorSpec(new BatchStatusReportPluginDataTranslationSpec());
                        coreBuilder.addTranslatorSpec(new MaterialsProducerPropertyReportPluginDataTranslationSpec());
                        coreBuilder.addTranslatorSpec(new MaterialsProducerResourceReportPluginDataTranslationSpec());
                        coreBuilder.addTranslatorSpec(new StageReportPluginDataTranslationSpec());
                    }

                    coreBuilder
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
