package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups;

import gov.hhs.aspr.gcm.translation.protobuf.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.GroupIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.GroupPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.GroupTypeIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.GroupsPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import plugins.groups.GroupsPluginData;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupIdInput;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupsPluginDataInput;

public class GroupsTranslator {
    private GroupsTranslator() {
    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(GroupsTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new GroupsPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new GroupIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new GroupTypeIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new GroupPropertyIdTranslatorSpec());

                    translatorContext
                            .addFieldToIncludeDefaultValue(GroupIdInput.getDescriptor().findFieldByName("id"));
                });
    }

    public static Translator getTranslatorRW(String inputFileName, String outputFileName) {
        return builder()
                .addInputFile(inputFileName, GroupsPluginDataInput.getDefaultInstance())
                .addOutputFile(outputFileName, GroupsPluginData.class)
                .build();
    }

    public static Translator getTranslatorR(String inputFileName) {
        return builder()
                .addInputFile(inputFileName, GroupsPluginDataInput.getDefaultInstance())
                .build();
    }

    public static Translator getTranslatorW(String outputFileName) {
        return builder()
                .addOutputFile(outputFileName, GroupsPluginData.class)
                .build();
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
