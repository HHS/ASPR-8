package gov.hhs.aspr.translation.core;

import util.errors.ContractError;

public enum CoreTranslationError implements ContractError {

    NULL_TRANSLATOR_ID("Null TranslatorId"),
    NULL_TRANSLATOR("Null Translator"),
    NULL_TRANSLATION_ENGINE_BUILDER("Null Translation Engine Builder"),
    NULL_TRANSLATION_ENGINE("Null Translation Engine"),
    INVALID_TRANSLATION_ENGINE_BUILDER("The given Translation Engine Builder classRef does not match the class of the actual Translation Engine Builder"),
    DUPLICATE_TRANSLATOR("Duplicate Translator"),
    MISSING_TRANSLATOR("Missing Translator: "),
    CIRCULAR_TRANSLATOR_DEPENDENCIES("Circular translator dependencies: "),
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
    UNKNOWN_CLASSREF("No object has been read in with the specified classRef"),
    NULL_TRANSLATION_SPEC("Null TranslationSpec"),
    NULL_TRANSLATION_SPEC_APP_CLASS("Null TranslationSpec App Class"),
    NULL_TRANSLATION_SPEC_INPUT_CLASS("Null TranslationSpec Input Class"),
    DUPLICATE_TRANSLATION_SPEC("Duplicate TranslationSpec"),
    UNKNOWN_TRANSLATION_SPEC("No translation spec was provided for the given class"),
    UNITIALIZED_TRANSLATION_SPEC("TranslationSpec not initialized"),
    UNKNOWN_OBJECT("Object is not Translatable by this TranslationSpec")
    ;

    private final String description;

    private CoreTranslationError(final String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
