package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events.GroupMembershipAdditionEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events.GroupMembershipRemovalEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class GroupsForPersonFilter extends Filter {

	private final Equality equality;
	private final int groupCount;
	private GroupsDataManager groupsDataManager;

	private void validateEquality(final PartitionsContext partitionsContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	public Equality getEquality() {
		return equality;
	}

	public int getGroupCount() {
		return groupCount;
	}

	public GroupsForPersonFilter(final Equality equality, final int groupCount) {
		this.equality = equality;
		this.groupCount = groupCount;
	}

	@Override
	public void validate(PartitionsContext partitionsContext) {
		validateEquality(partitionsContext, equality);
	}

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
		if (partitionsContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		if (groupsDataManager == null) {
			groupsDataManager = partitionsContext.getDataManager(GroupsDataManager.class);
		}
		final int count = groupsDataManager.getGroupCountForPerson(personId);
		return equality.isCompatibleComparisonValue(Integer.compare(count, groupCount));
	}

	private Optional<PersonId> additionRequiresRefresh(PartitionsContext partitionsContext,
			GroupMembershipAdditionEvent event) {
		return Optional.of(event.personId());
	}

	private Optional<PersonId> removalRequiresRefresh(PartitionsContext partitionsContext,
			GroupMembershipRemovalEvent event) {
		return Optional.of(event.personId());
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

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(equality, groupCount);
	}

	/**
     * Two {@link GroupsForPersonFilter} instances are equal if and only if
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
		GroupsForPersonFilter other = (GroupsForPersonFilter) obj;
		return equality == other.equality && groupCount == other.groupCount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GroupsForPersonFilter [equality=");
		builder.append(equality);
		builder.append(", groupCount=");
		builder.append(groupCount);
		builder.append("]");
		return builder.toString();
	}

}
