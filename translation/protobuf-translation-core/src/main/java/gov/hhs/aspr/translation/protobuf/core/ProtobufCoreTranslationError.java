package gov.hhs.aspr.translation.protobuf.core;

import util.errors.ContractError;

public enum ProtobufCoreTranslationError implements ContractError {
    INVALID_INPUT_CLASS_REF("The inputClassRef is not of the parent type: Message.class"),
    UNKNOWN_TYPE_URL("The given type url does not have a corresponding classRef. Either the typeUrl was never provided, or the typeUrl is malformed.")
    ;

    private final String description;

    private ProtobufCoreTranslationError(final String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
