package nucleus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A Dimension represents a single independent dimension of an experiment.
 * Dimensions are composed of a finite number of levels. Each level in a
 * dimension is a function that 1) consumes a type map of plugin data builders,
 * 2) updates those builders to create alternate inputs for each scenario and 3)
 * returns a list of strings representing the variant values in the scenario.
 * 
 *
 */

public interface Dimension2 {

	/**
	 * Returns the meta data for the experiment that describes the
	 * scenario-level meta data.
	 */
	public List<String> getExperimentMetaData();

	/**
	 * Returns the number of levels in this dimension
	 */
	public int levelCount();

	/**
	 * Executes mutations on the plugin data builders contained in the
	 * DimensionContext. Returns the scenario level meta data associated with
	 * the level. The length of the returned list must match the length of the
	 * list returned by the getExperimentMetaData() method;
	 */

	public List<String> executeLevel(DimensionContext dimensionContext, int level);

}
