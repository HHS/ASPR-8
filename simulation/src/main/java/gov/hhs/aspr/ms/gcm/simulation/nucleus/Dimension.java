package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.List;

/**
 * A Dimension represents a single independent dimension of an experiment.
 * Dimensions are composed of a finite number of levels. Each level in a
 * dimension updates the plugin data builders as alternate inputs for each
 * scenario.
 */
public interface Dimension {

	/**
	 * Returns the meta data for the experiment that describes the scenario-level
	 * meta data.
	 */
	public List<String> getExperimentMetaData();

	/**
	 * Returns the number of levels in this dimension
	 */
	public int levelCount();

	/**
	 * Executes mutations on the plugin data builders contained in the
	 * DimensionContext. Returns the scenario level meta data associated with the
	 * level. The length of the returned list must match the length of the list
	 * returned by the getExperimentMetaData() method. The content of the returned
	 * list must be identical for each invocation of the given level.
	 */
	public List<String> executeLevel(DimensionContext dimensionContext, int level);

}
