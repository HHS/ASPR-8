package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.core.TranslationSpec;
import gov.hhs.aspr.translation.core.Translator;

import java.util.ArrayList;
import java.util.List;

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
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslatorId;

/**
 * Translator for the Materials Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * MaterialsPluginData
 */
public class MaterialsTranslator {

    private MaterialsTranslator() {
    }

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new MaterialsPluginDataTranslationSpec());
        list.add(new MaterialIdTranslationSpec());
        list.add(new MaterialsProducerIdTranslationSpec());
        list.add(new MaterialsProducerPropertyIdTranslationSpec());
        list.add(new BatchIdTranslationSpec());
        list.add(new StageIdTranslationSpec());
        list.add(new BatchPropertyIdTranslationSpec());
        list.add(new TestBatchPropertyIdTranslationSpec());
        list.add(new TestMaterialIdTranslationSpec());
        list.add(new TestMaterialsProducerIdTranslationSpec());
        list.add(new TestMaterialsProducerPropertyIdTranslationSpec());
        list.add(new BatchStatusReportPluginDataTranslationSpec());
        list.add(new MaterialsProducerPropertyReportPluginDataTranslationSpec());
        list.add(new MaterialsProducerResourceReportPluginDataTranslationSpec());
        list.add(new StageReportPluginDataTranslationSpec());

        return list;
    }

    private static Translator.Builder builder(boolean withReport) {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(MaterialsTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(ResourcesTranslatorId.TRANSLATOR_ID)
                .addDependency(RegionsTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    for (TranslationSpec<?, ?> translationSpec : getTranslationSpecs()) {
                        translationEngineBuilder.addTranslationSpec(translationSpec);
                    }

                    translationEngineBuilder
                            .addFieldToIncludeDefaultValue(BatchIdInput.getDescriptor().findFieldByName("id"))
                            .addFieldToIncludeDefaultValue(StageIdInput.getDescriptor().findFieldByName("id"));
                });

        return builder;
    }

    public static Translator getTranslatorWithReport() {
        return builder(true).build();
    }

    public static Translator getTranslator() {
        return builder(false).build();
    }
}
