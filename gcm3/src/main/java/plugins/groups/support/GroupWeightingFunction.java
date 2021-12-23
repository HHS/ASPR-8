package plugins.groups.support;

import nucleus.Context;
import plugins.people.support.PersonId;

/**
 * A functional interface for selecting people from a group based on assigning a
 * weighting value to a person.
 * 
 * @author Shawn Hatch
 *
 */
public interface GroupWeightingFunction {
	/**
	 * Returns a non-negative, finite and stable value for the given inputs.
	 * Repeated invocations with the same arguments should return the same value
	 * while no mutations to simulation state have taken place. The person will
	 * be a member of the group.
	 */
	public double getWeight(Context context, PersonId personId, GroupId groupId);
}
