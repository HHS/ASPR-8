package util.errors;

/**
 * A {@link RuntimeException} that indicates that the cause of the error as a
 * precondition(contract) violation
 * 
 *
 */
public final class ContractException extends RuntimeException {

	private static final long serialVersionUID = -2668978936990585390L;

	private final ContractError contractError;

	/**
	 * Constructs the exception with the given contract error. The resulting
	 * exception's message value will be the description value of the contract
	 * error.
	 * 
	 * @throws NullPointerException
	 *             <li>if the contract error is null</li>
	 * 
	 */
	public ContractException(final ContractError contractError) {
		super(contractError.getDescription());
		this.contractError = contractError;
	}

	/**
	 * Constructs the exception with the given contract error and details value.
	 * The resulting exception's message value will be the description value of
	 * the contract error concatenated with the details.toString().
	 * 
	 * The result will be contractError.getDescription() + ": " +
	 * details.toString())
	 * 
	 * @throws NullPointerException
	 *             <li>if the contract error is null</li>
	 *             <li>if the details value is null</li>
	 * 
	 */
	public ContractException(final ContractError contractError, final Object details) {
		super(contractError.getDescription() + ": " + details.toString());
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