package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.StageIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.BatchIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.BatchPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.BatchStatusReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialsPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialsProducerIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialsProducerPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialsProducerPropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.MaterialsProducerResourceReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.StageIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.StageReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.TestBatchPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.TestMaterialIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.TestMaterialsProducerIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs.TestMaterialsProducerPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs.TestResourceIdTranslatorSpec;

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

                    coreBuilder.addTranslatorSpec(new MaterialsPluginDataTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new MaterialIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new MaterialsProducerIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new MaterialsProducerPropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new BatchIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new StageIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new BatchPropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestResourceIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestBatchPropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestMaterialIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestMaterialsProducerIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestMaterialsProducerPropertyIdTranslatorSpec());

                    if (withReport) {
                        coreBuilder.addTranslatorSpec(new BatchStatusReportPluginDataTranslatorSpec());
                        coreBuilder.addTranslatorSpec(new MaterialsProducerPropertyReportPluginDataTranslatorSpec());
                        coreBuilder.addTranslatorSpec(new MaterialsProducerResourceReportPluginDataTranslatorSpec());
                        coreBuilder.addTranslatorSpec(new StageReportPluginDataTranslatorSpec());
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
