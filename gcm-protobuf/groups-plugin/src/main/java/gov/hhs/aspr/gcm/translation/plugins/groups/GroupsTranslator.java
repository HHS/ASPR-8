package gov.hhs.aspr.gcm.translation.plugins.groups;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupIdInput;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupsPluginDataInput;
import gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs.GroupIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs.GroupPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs.GroupTypeIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs.GroupsPluginDataTranslator;

public class GroupsTranslator {
    private GroupsTranslator() {
    }

    private static Translator.Builder getBaseTranslator() {
        return Translator.builder()
                .setTranslatorId(GroupsTranslatorId.TRANSLATOR_ID)
                .setInputObjectType(GroupsPluginDataInput.getDefaultInstance())
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new GroupsPluginDataTranslator());
                    translatorContext.addTranslatorSpec(new GroupIdTranslator());
                    translatorContext.addTranslatorSpec(new GroupTypeIdTranslator());
                    translatorContext.addTranslatorSpec(new GroupPropertyIdTranslator());

                    translatorContext
                            .addFieldToIncludeDefaultValue(GroupIdInput.getDescriptor().findFieldByName("id"));
                });
    }

    public static Translator getTranslator(String inputFileName, String outputFileName) {
        return getBaseTranslator()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static Translator getTranslator() {
        return getBaseTranslator().build();
    }
}
