package plugins.groups.support;

import nucleus.SimulationContext;
import plugins.people.support.PersonId;

/**
 * A functional interface for selecting people from a group based on assigning a
 * weighting value to a person.
 * 
 *
 */
public interface GroupWeightingFunction {
	/**
	 * Returns a non-negative, finite and stable value for the given inputs.
	 * Repeated invocations with the same arguments should return the same value
	 * while no mutations to simulation state have taken place. The person will
	 * be a member of the group.
	 */
	public double getWeight(SimulationContext simulationContext, PersonId personId, GroupId groupId);
}
