package nucleus.util;

/**
 * A {@link RuntimeException} that indicates that the cause of the error is very
 * likely due to a precondition violation
 * 
 * @author Shawn Hatch
 *
 */
public final class ContractException extends RuntimeException {

	private static final long serialVersionUID = -2668978936990585390L;

	private final ContractError contractError;

	public ContractException(final ContractError contractError) {
		super(contractError.getDescription());
		this.contractError = contractError;
	}

	public ContractException(final ContractError contractError, final Object details) {
		super(contractError.getDescription()+": "+details.toString());
		this.contractError = contractError;
	}

	/**
	 * Returns the SimulationErrorType that documents the general issue that
	 * caused the exception.
	 */
	public ContractError getErrorType() {
		return contractError;
	}

}