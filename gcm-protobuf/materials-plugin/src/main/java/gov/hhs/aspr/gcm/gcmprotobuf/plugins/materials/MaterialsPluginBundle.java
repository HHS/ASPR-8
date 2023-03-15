package gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials;

import gov.hhs.aspr.gcm.gcmprotobuf.core.PluginBundle;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators.BatchIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators.BatchPropertyIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators.MaterialIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators.MaterialsPluginDataTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators.MaterialsProducerIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators.MaterialsProducerPropertyIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators.StageIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.resources.ResourcesPluginBundleId;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.PropertiesPluginBundleId;
import plugins.materials.input.BatchIdInput;
import plugins.materials.input.MaterialsPluginDataInput;
import plugins.materials.input.StageIdInput;

public class MaterialsPluginBundle {
    private MaterialsPluginBundle() {

    }

    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName) {
        return PluginBundle.builder()
                .setPluginBundleId(MaterialsPluginBundleId.PLUGIN_BUNDLE_ID)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .setInputObjectType(MaterialsPluginDataInput.getDefaultInstance())
                .addDependency(PropertiesPluginBundleId.PLUGIN_BUNDLE_ID)
                .addDependency(ResourcesPluginBundleId.PLUGIN_BUNDLE_ID)
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
                })
                .build();
    }
}