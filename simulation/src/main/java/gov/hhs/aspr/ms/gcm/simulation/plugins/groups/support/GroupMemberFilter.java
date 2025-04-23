package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events.GroupMembershipAdditionEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events.GroupMembershipRemovalEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class GroupMemberFilter extends Filter {
	final GroupId groupId;
	private GroupsDataManager groupsDataManager;

	private void validateGroupIdNotNull(PartitionsContext partitionsContext, final GroupId groupId) {
		if (groupId == null) {
			throw new ContractException(GroupError.NULL_GROUP_ID);
		}
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public GroupMemberFilter(final GroupId groupId) {
		this.groupId = groupId;
	}

	@Override
	public void validate(PartitionsContext partitionsContext) {
		validateGroupIdNotNull(partitionsContext, groupId);
	}

	private Optional<PersonId> additionRequiresRefresh(PartitionsContext partitionsContext,
			GroupMembershipAdditionEvent event) {
		if (event.groupId().equals(groupId)) {
			return Optional.of(event.personId());
		}
		return Optional.empty();
	}

	private Optional<PersonId> removalRequiresRefresh(PartitionsContext partitionsContext,
			GroupMembershipRemovalEvent event) {
		if (event.groupId().equals(groupId)) {
			return Optional.of(event.personId());
		}
		return Optional.empty();

	}

	@Override
	public Set<FilterSensitivity<?>> getFilterSensitivities() {
		Set<FilterSensitivity<?>> result = new LinkedHashSet<>();
		result.add(new FilterSensitivity<GroupMembershipAdditionEvent>(GroupMembershipAdditionEvent.class,
				this::additionRequiresRefresh));
		result.add(new FilterSensitivity<GroupMembershipRemovalEvent>(GroupMembershipRemovalEvent.class,
				this::removalRequiresRefresh));

		return result;
	}

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (groupsDataManager == null) {
			groupsDataManager = partitionsContext.getDataManager(GroupsDataManager.class);
		}
		return groupsDataManager.isPersonInGroup(personId, groupId);
	}

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(groupId);
	}

	/**
     * Two {@link GroupMemberFilter} instances are equal if and only if
     * their inputs are equal.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GroupMemberFilter other = (GroupMemberFilter) obj;
		return Objects.equals(groupId, other.groupId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GroupMemberFilter [groupId=");
		builder.append(groupId);
		builder.append("]");
		return builder.toString();
	}

}
