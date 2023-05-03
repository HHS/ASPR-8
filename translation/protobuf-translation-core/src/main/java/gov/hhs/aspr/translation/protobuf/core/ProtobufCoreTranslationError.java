package gov.hhs.aspr.translation.protobuf.core;

import util.errors.ContractError;

public enum ProtobufCoreTranslationError implements ContractError {
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
