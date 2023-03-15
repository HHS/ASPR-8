package gov.hhs.aspr.gcm.translation.plugins.groups;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translators.GroupIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translators.GroupPropertyIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translators.GroupTypeIdTranslator;
import gov.hhs.aspr.gcm.translation.plugins.groups.translators.GroupsPluginDataTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslatorModuleId;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupIdInput;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupsPluginDataInput;

public class GroupsTranslatorModule {
    private GroupsTranslatorModule() {
    }

    private static Translator.Builder getBaseModule() {
        return Translator.builder()
                .setPluginBundleId(GroupsTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .setInputObjectType(GroupsPluginDataInput.getDefaultInstance())
                .addDependency(PropertiesTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .addDependency(PeopleTranslatorModuleId.TRANSLATOR_MODULE_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslator(new GroupsPluginDataTranslator());
                    translatorContext.addTranslator(new GroupIdTranslator());
                    translatorContext.addTranslator(new GroupTypeIdTranslator());
                    translatorContext.addTranslator(new GroupPropertyIdTranslator());

                    translatorContext
                            .addFieldToIncludeDefaultValue(GroupIdInput.getDescriptor().findFieldByName("id"));
                });
    }

    public static Translator getTranslatorModule(String inputFileName, String outputFileName) {
        return getBaseModule()
                .setInputFileName(inputFileName)
                .setOutputFileName(outputFileName)
                .build();
    }

    public static Translator getTranslatorModule() {
        return getBaseModule().build();
    }
}
