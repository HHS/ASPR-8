package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups;

import gov.hhs.aspr.gcm.gcmprotobuf.core.PluginBundle;
import gov.hhs.aspr.gcm.gcmprotobuf.people.PeoplePluginBundleId;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators.GroupIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators.GroupPropertyIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators.GroupTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators.GroupTypeIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators.GroupsPluginDataTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators.TestGroupPropertyIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators.TestGroupTypeIdTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.properties.PropertiesPluginBundleId;
import plugins.groups.input.GroupIdInput;
import plugins.groups.input.GroupsPluginDataInput;

public class GroupsPluginBundle {
    private GroupsPluginBundle() {

    }

    public static PluginBundle getPluginBundle(String inputFileName, String outputFileName) {
        return PluginBundle.builder()
                .setPluginBundleId(GroupsPluginBundleId.PLUGIN_BUNDLE_ID)
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
                    translatorContext.addTranslator(new GroupTranslator());
                    translatorContext.addTranslator(new TestGroupPropertyIdTranslator());
                    translatorContext.addTranslator(new TestGroupTypeIdTranslator());

                    translatorContext
                            .addFieldToIncludeDefaultValue(GroupIdInput.getDescriptor().findFieldByName("groupId"));
                })
                .build();
    }
}
