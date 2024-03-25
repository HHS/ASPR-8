package gov.hhs.aspr.ms.epifast.pipeline.support;

import gov.hhs.aspr.ms.util.errors.ContractError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 */
public enum PipelineError implements ContractError {
    BLANK_INPUT("A blank(empty or all white space) was encountered"),
    MALFORMED_FILE("Malformed file"),

    UNKNOWN_RANDOM_NUMBER_GENERATOR_ID("Unknown random number generator id"),
    DUPLICATE_RANDOM_NUMBER_GENERATOR_ID("Duplicate random number generator id"),
    MISSING_RANDOM_NUMBER_GENERATOR_ID("Missing random number generator id"),
    STOCHASTICS_FILE_MALFORMED("The stochastics file is malformed"),;

    private final String description;

    private PipelineError(final String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
