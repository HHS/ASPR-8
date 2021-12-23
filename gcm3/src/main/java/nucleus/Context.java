package nucleus;

import java.util.Optional;

import util.ContractError;
import util.ContractException;

/**
 * A context provides basic access to the nucleus engine and published data.
 * 
 * @author Shawn Hatch
 *
 */
public interface Context {

	/**
	 * Sends output to whatever consumer of output is registered with nucleus,
	 * if any
	 */
	public void releaseOutput(Object output);

	/**
	 * Returns a data whose class is the data view class.
	 */
	public <T extends DataView> Optional<T> getDataView(Class<T> dataViewClass);

	/**
	 * Returns the current time in the simulation
	 */
	public double getTime();

	/**
	 * Provides a convenience mechanism for throwing
	 * {@linkplain ContractException} that will include contextual details
	 * supplied by nucleus to aid with debugging
	 */
	public void throwContractException(ContractError recoverableError);

	/**
	 * Provides a convenience mechanism for throwing
	 * {@linkplain ContractException} that will include contextual details
	 * supplied by nucleus to aid with debugging
	 */
	public void throwContractException(ContractError recoverableError, Object details);

}
