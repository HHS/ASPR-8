package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.StageIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.BatchIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.BatchPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.BatchStatusReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.MaterialIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.MaterialsPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.MaterialsProducerIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.MaterialsProducerPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.MaterialsProducerPropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.MaterialsProducerResourceReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.StageIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.StageReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.TestBatchPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.TestMaterialIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.TestMaterialsProducerIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs.TestMaterialsProducerPropertyIdTranslatorSpec;
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
                    translatorContext.addTranslatorSpec(new MaterialsPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new MaterialIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new MaterialsProducerIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new MaterialsProducerPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new BatchIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new StageIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new BatchPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestResourceIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestBatchPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestMaterialIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestMaterialsProducerIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestMaterialsProducerPropertyIdTranslatorSpec());

                    if (withReport) {
                        translatorContext.addTranslatorSpec(new BatchStatusReportPluginDataTranslatorSpec());
                        translatorContext
                                .addTranslatorSpec(new MaterialsProducerPropertyReportPluginDataTranslatorSpec());
                        translatorContext
                                .addTranslatorSpec(new MaterialsProducerResourceReportPluginDataTranslatorSpec());
                        translatorContext.addTranslatorSpec(new StageReportPluginDataTranslatorSpec());
                    }

                    ((ProtobufTranslatorCore.Builder) translatorContext.getTranslatorCoreBuilder())
                            .addFieldToIncludeDefaultValue(BatchIdInput.getDescriptor().findFieldByName("id"))
                            .addFieldToIncludeDefaultValue(StageIdInput.getDescriptor().findFieldByName("id"));
                });

        if (withReport) {
            builder.addDependency(ReportsTranslatorId.TRANSLATOR_ID);
        }
        return builder;

    }

    public static Translator.Builder builder() {
        return builder(false);
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
