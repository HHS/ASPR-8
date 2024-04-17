package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

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

public final class GroupsForPersonAndGroupTypeFilter extends Filter {
	private final GroupTypeId groupTypeId;
	private final Equality equality;
	private final int groupCount;
	private GroupsDataManager groupsDataManager;

	private void validateEquality(final PartitionsContext partitionsContext, final Equality equality) {
		if (equality == null) {
			throw new ContractException(PartitionError.NULL_EQUALITY_OPERATOR);
		}
	}

	private void validateGroupTypeId(final PartitionsContext partitionsContext, final GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}

		if (groupsDataManager == null) {
			groupsDataManager = partitionsContext.getDataManager(GroupsDataManager.class);
		}

		if (!groupsDataManager.groupTypeIdExists(groupTypeId)) {
			throw new ContractException(GroupError.UNKNOWN_GROUP_TYPE_ID, groupTypeId);
		}
	}

	public GroupTypeId getGroupTypeId() {
		return groupTypeId;
	}

	public Equality getEquality() {
		return equality;
	}

	public int getGroupCount() {
		return groupCount;
	}

	public GroupsForPersonAndGroupTypeFilter(final GroupTypeId groupTypeId, final Equality equality,
			final int groupCount) {
		this.equality = equality;
		this.groupCount = groupCount;
		this.groupTypeId = groupTypeId;
	}

	@Override
	public void validate(PartitionsContext partitionsContext) {
		validateEquality(partitionsContext, equality);
		validateGroupTypeId(partitionsContext, groupTypeId);
	}

	@Override
	public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
		if (groupsDataManager == null) {
			groupsDataManager = partitionsContext.getDataManager(GroupsDataManager.class);
		}
		final int count = groupsDataManager.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
		return evaluate(count);
	}

	private boolean evaluate(int count) {
		return equality.isCompatibleComparisonValue(Integer.compare(count, groupCount));
	}

	private Optional<PersonId> additionRequiresRefresh(PartitionsContext partitionsContext,
			GroupMembershipAdditionEvent event) {
		if (groupsDataManager == null) {
			groupsDataManager = partitionsContext.getDataManager(GroupsDataManager.class);
		}
		if (groupsDataManager.getGroupType(event.groupId()).equals(groupTypeId)) {
			return Optional.of(event.personId());
		}
		return Optional.empty();
	}

	private Optional<PersonId> removalRequiresRefresh(PartitionsContext partitionsContext,
			GroupMembershipRemovalEvent event) {
		if (groupsDataManager == null) {
			groupsDataManager = partitionsContext.getDataManager(GroupsDataManager.class);
		}
		if (groupsDataManager.getGroupType(event.groupId()).equals(groupTypeId)) {
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((equality == null) ? 0 : equality.hashCode());
		result = prime * result + groupCount;
		result = prime * result + ((groupTypeId == null) ? 0 : groupTypeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GroupsForPersonAndGroupTypeFilter)) {
			return false;
		}
		GroupsForPersonAndGroupTypeFilter other = (GroupsForPersonAndGroupTypeFilter) obj;
		if (equality != other.equality) {
			return false;
		}
		if (groupCount != other.groupCount) {
			return false;
		}
		if (groupTypeId == null) {
			if (other.groupTypeId != null) {
				return false;
			}
		} else if (!groupTypeId.equals(other.groupTypeId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GroupsForPersonAndGroupTypeFilter [groupTypeId=");
		builder.append(groupTypeId);
		builder.append(", equality=");
		builder.append(equality);
		builder.append(", groupCount=");
		builder.append(groupCount);
		builder.append("]");
		return builder.toString();
	}

}