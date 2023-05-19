package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestMethod;

public class AT_MaterialsTranslator {

    @Test
    @UnitTestMethod(target = MaterialsTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(MaterialsTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(ResourcesTranslatorId.TRANSLATOR_ID)
                .addDependency(RegionsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new MaterialsPluginDataTranslationSpec())
                            .addTranslationSpec(new MaterialIdTranslationSpec())
                            .addTranslationSpec(new MaterialsProducerIdTranslationSpec())
                            .addTranslationSpec(new MaterialsProducerPropertyIdTranslationSpec())
                            .addTranslationSpec(new BatchIdTranslationSpec())
                            .addTranslationSpec(new StageIdTranslationSpec())
                            .addTranslationSpec(new BatchPropertyIdTranslationSpec())
                            .addTranslationSpec(new TestBatchPropertyIdTranslationSpec())
                            .addTranslationSpec(new TestMaterialIdTranslationSpec())
                            .addTranslationSpec(new TestMaterialsProducerIdTranslationSpec())
                            .addTranslationSpec(new TestMaterialsProducerPropertyIdTranslationSpec());

                    translationEngineBuilder
                            .addFieldToIncludeDefaultValue(BatchIdInput.getDescriptor().findFieldByName("id"))
                            .addFieldToIncludeDefaultValue(StageIdInput.getDescriptor().findFieldByName("id"));
                }).build();

        assertEquals(expectedTranslator, MaterialsTranslator.getTranslator());
    }

    @Test
    @UnitTestMethod(target = MaterialsTranslator.class, name = "getTranslatorWithReport", args = {})
    public void testGetTranslatorWithReport() {
        Translator expectedTranslator = Translator.builder()
                .setTranslatorId(MaterialsTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(ResourcesTranslatorId.TRANSLATOR_ID)
                .addDependency(RegionsTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new MaterialsPluginDataTranslationSpec())
                            .addTranslationSpec(new MaterialIdTranslationSpec())
                            .addTranslationSpec(new MaterialsProducerIdTranslationSpec())
                            .addTranslationSpec(new MaterialsProducerPropertyIdTranslationSpec())
                            .addTranslationSpec(new BatchIdTranslationSpec())
                            .addTranslationSpec(new StageIdTranslationSpec())
                            .addTranslationSpec(new BatchPropertyIdTranslationSpec())
                            .addTranslationSpec(new TestBatchPropertyIdTranslationSpec())
                            .addTranslationSpec(new TestMaterialIdTranslationSpec())
                            .addTranslationSpec(new TestMaterialsProducerIdTranslationSpec())
                            .addTranslationSpec(new TestMaterialsProducerPropertyIdTranslationSpec())
                            .addTranslationSpec(new BatchStatusReportPluginDataTranslationSpec())
                            .addTranslationSpec(new MaterialsProducerPropertyReportPluginDataTranslationSpec())
                            .addTranslationSpec(new MaterialsProducerResourceReportPluginDataTranslationSpec())
                            .addTranslationSpec(new StageReportPluginDataTranslationSpec());

                    translationEngineBuilder
                            .addFieldToIncludeDefaultValue(BatchIdInput.getDescriptor().findFieldByName("id"))
                            .addFieldToIncludeDefaultValue(StageIdInput.getDescriptor().findFieldByName("id"));
                }).build();

        assertEquals(expectedTranslator, MaterialsTranslator.getTranslatorWithReport());
    }
}
