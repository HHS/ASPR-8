package nucleus;

import java.util.Optional;

/**
 * A context provides basic access to the nucleus engine and published data.
 * 
 * @author Shawn Hatch
 *
 */
public interface SimulationContext {

	/**
	 * Sends output to whatever consumer of output is registered with nucleus,
	 * if any
	 */
	public void releaseOutput(Object output);

	
	public <T extends DataManager> Optional<T> getDataManager(Class<T> dataManagerClass);

	/**
	 * Returns the current time in the simulation
	 */
	public double getTime();

	

}
