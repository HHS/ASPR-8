package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;

/**
 * A functional interface for selecting people from a group based on assigning a
 * weighting value to a person.
 */
public interface GroupWeightingFunction {
	/**
	 * Returns a non-negative, finite and stable value for the given inputs.
	 * Repeated invocations with the same arguments should return the same value
	 * while no mutations to simulation state have taken place. The person will be a
	 * member of the group.
	 */
	public double getWeight(GroupsContext groupsContext, PersonId personId, GroupId groupId);
}
