package plugins.gcm.experiment;

import net.jcip.annotations.ThreadSafe;
import plugins.gcm.input.Scenario;

@ThreadSafe

/**
 * A container for a list of scenarios to be used in an experiment. All
 * scenarios are identical except for property values and resource allocations.
 * 
 * @author Shawn Hatch
 *
 */
public interface Experiment {

	/**
	 * Returns the scenario at the given index;
	 */
	public Scenario getScenario(int index);

	/**
	 * Return the scenario id from the given int index
	 */
	public ScenarioId getScenarioIdFromIndex(int index);

	/**
	 * Returns the scenario id at the given index;
	 */
	public ScenarioId getScenarioId(int index);

	/**
	 * Returns the number of scenarios contained in the experiment
	 */
	public int getScenarioCount();

	/**
	 * Returns the list of property identifiers associated with varying values
	 * between scenarios. Properties that have constant values across all
	 * scenarios are excluded. The returned list contains string conversions of
	 * each property in the following format.
	 * [Property_Type.property_identifier.sub_identifiers....]
	 */
	public String getExperimentFieldName(int fieldIndex);

	public int getExperimentFieldCount();

	/**
	 * Returns for each string(qualified property id) from getExperimentFields()
	 * the value of that property for the given scenario.
	 */
	public <T> T getExperimentFieldValue(ScenarioId scenarioId, int fieldIndex);
}