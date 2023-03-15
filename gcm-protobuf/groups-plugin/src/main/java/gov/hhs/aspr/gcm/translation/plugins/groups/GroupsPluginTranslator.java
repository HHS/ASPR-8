package gov.hhs.aspr.gcm.translation.plugins.groups;

import gov.hhs.aspr.gcm.translation.core.TranslatorModule;
import gov.hhs.aspr.gcm.translation.plugins.groups.translators.GroupIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translators.GroupPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translators.GroupTypeIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translators.GroupsPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeoplePluginBundleId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesPluginBundleId;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupIdInput;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupsPluginDataInput;

public class GroupsPluginTranslator {
    private GroupsPluginTranslator() {

    }

    public static TranslatorModule getPluginBundle(String inputFileName, String outputFileName) {
        return TranslatorModule.builder()
                .setPluginBundleId(GroupsPluginTranslatorId.PLUGIN_BUNDLE_ID)
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .setInputObjectType(GroupsPluginDataInput.getDefaultInstance())
                .addDependency(PropertiesPluginBundleId.PLUGIN_BUNDLE_ID)
                .addDependency(PeoplePluginBundleId.PLUGIN_BUNDLE_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new GroupsPluginDataTranslator());
                    translatorContext.addTranslator(new GroupIdTranslator());
                    translatorContext.addTranslator(new GroupTypeIdTranslator());
                    translatorContext.addTranslator(new GroupPropertyIdTranslator());

                    translatorContext
                            .addFieldToIncludeDefaultValue(GroupIdInput.getDescriptor().findFieldByName("id"));
                })
                .build();
    }
}
