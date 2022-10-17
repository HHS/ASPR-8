package util.dimensiontree;

import util.errors.ContractError;
import util.errors.ContractException;

/**
 * An enumeration supporting {@link ContractException} that acts as a general
 * description of the exception.
 * 
 * @author Shawn Hatch
 *
 */
public enum DimensionTreeError implements ContractError {

	NON_POSITIVE_LEAF_SIZE("non-positive leaf size"),
	LOWER_BOUNDS_ARE_NULL("lower bounds are null"),
	UPPER_BOUNDS_ARE_NULL("upper bounds are null"),
	BOUNDS_MISMATCH("dimensional mismatch between bounds"),
	LOWER_BOUNDS_EXCEED_UPPER_BOUNDS("lower bounds exceed upper bounds"),
	;

	private final String description;

	private DimensionTreeError(final String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
