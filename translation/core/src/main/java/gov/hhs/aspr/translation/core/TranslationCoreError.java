package gov.hhs.aspr.translation.core;

import util.errors.ContractError;

public enum TranslationCoreError implements ContractError {

    NULL_TRANSLATOR_ID("Null TranslatorId"),
    NULL_TRANSLATOR("Null Translator"),
    NULL_TRANSLATORCORE_BUILDER("Null TranslatorCore Builder"),
    NULL_TRANSLATORCORE("Null TranslatorCore"),
    INVALID_TRANSLATORCORE_BUILDER("The given TranslatorCore Builder classRef does not match the class of the actual TranslatorCore Builder"),
    DUPLICATE_TRANSLATOR("Duplicate Translator"),
    NULL_INIT_CONSUMER("Null Initilizer Consumer"),
    NULL_DEPENDENCY("Null dependency"),
    DUPLICATE_DEPENDENCY("Duplicate Dependency"),
    NULL_PATH("Null Path"),
    DUPLICATE_INPUT_PATH("Duplicate Input Path"),
    INVALID_INPUT_PATH("The given input file path does not exist"),
    NULL_CLASS_REF("Null Class Ref"),
    INVALID_OUTPUT_CLASSREF("The given class does not have a output file path associated with it."),
    DUPLICATE_OUTPUT_PATH("Duplicate Output Path"),
    INVALID_OUTPUT_PATH("The given output file path does not exist. While the file will be created on write, the directory will not."),
    DUPLICATE_CLASSREF_SCENARIO_PAIR("Duplicate ClassRef and Scenario Pair"),
    DUPLICATE_CLASSREF("Duplicate ClassRef"),
    UNKNOWN_CLASSREF("No object has been read in with the specified classRef")
    ;

    private final String description;

    private TranslationCoreError(final String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
