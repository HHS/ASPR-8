
package util.errors;

/**
 * Marker interface for the descriptions of runtime exceptions where the source
 * of the error is the violation of a contract (precondition) specification.
 * 
 *
 */
public interface ContractError {
	public String getDescription();
}
