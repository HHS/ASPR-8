package plugins.groups.reports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nucleus.ReportContext;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.events.GroupAdditionEvent;
import plugins.groups.events.GroupImminentRemovalEvent;
import plugins.groups.events.GroupPropertyDefinitionEvent;
import plugins.groups.events.GroupPropertyUpdateEvent;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;

/**
 * A periodic Report that displays the number of groups having particular values
 * for each group property for a given group type. Only non-zero person counts
 * are reported. The report is further limited to the
 * (GroupType,GroupPropertyId) pairs added to the builder.
 * 
 *
 *
 * Fields
 *
 * GroupType -- the group type of group
 *
 * Property -- the group property identifier
 *
 * Value -- the value of the property
 *
 * GroupCount -- the number of groups having the property value for the given
 * group type
 *
 *
 */
public final class GroupPropertyReport extends PeriodicReport {

	public GroupPropertyReport(GroupPropertyReportPluginData groupPropertyReportPluginData) {
		super(groupPropertyReportPluginData.getReportLabel(), groupPropertyReportPluginData.getReportPeriod());

		for (GroupTypeId groupTypeId : groupPropertyReportPluginData.getGroupTypeIds()) {
			includedProperties.put(groupTypeId, new LinkedHashSet<>(groupPropertyReportPluginData.getIncludedProperties(groupTypeId)));
			excludedProperties.put(groupTypeId, new LinkedHashSet<>(groupPropertyReportPluginData.getExcludedProperties(groupTypeId)));
		}
		includeNewProperties = groupPropertyReportPluginData.getDefaultInclusionPolicy();
	}

	private static class Counter {
		int count;
	}

	/*
	 * For each (GroupTypeId,GroupPropertyId,property value) triplet, count the
	 * number of groups having that triplet
	 */
	private final Map<GroupTypeId, Map<GroupPropertyId, Map<Object, Counter>>> groupTypeMap = new LinkedHashMap<>();

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder)//
																	.add("group_type")//
																	.add("property")//
																	.add("value")//
																	.add("group_count")//
																	.build();//
		}
		return reportHeader;
	}

	/*
	 * Decrement the number of groups for the given
	 * (GroupTypeId,GroupPropertyId,property value) triplet
	 */
	private void decrement(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		getCounter(groupTypeId, groupPropertyId, groupPropertyValue).count--;
	}

	@Override
	protected void flush(ReportContext reportContext) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();

		for (final GroupTypeId groupTypeId : groupTypeMap.keySet()) {
			final Map<GroupPropertyId, Map<Object, Counter>> propertyIdMap = groupTypeMap.get(groupTypeId);
			for (final GroupPropertyId groupPropertyId : propertyIdMap.keySet()) {
				final Map<Object, Counter> groupPropertyValueMap = propertyIdMap.get(groupPropertyId);
				for (final Object groupPropertyValue : groupPropertyValueMap.keySet()) {
					final Counter counter = groupPropertyValueMap.get(groupPropertyValue);
					if (counter.count > 0) {
						final int personCount = counter.count;
						reportItemBuilder.setReportHeader(getReportHeader());
						reportItemBuilder.setReportLabel(getReportLabel());

						fillTimeFields(reportItemBuilder);
						reportItemBuilder.addValue(groupTypeId.toString());
						reportItemBuilder.addValue(groupPropertyId.toString());
						reportItemBuilder.addValue(groupPropertyValue);
						reportItemBuilder.addValue(personCount);

						reportContext.releaseOutput(reportItemBuilder.build());
					}
				}
			}
		}
	}

	private Counter getCounter(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		Map<GroupPropertyId, Map<Object, Counter>> map1 = groupTypeMap.get(groupTypeId);
		if (map1 == null) {
			map1 = new LinkedHashMap<>();
			groupTypeMap.put(groupTypeId, map1);
		}
		Map<Object, Counter> map2 = map1.get(groupPropertyId);
		if (map2 == null) {
			map2 = new LinkedHashMap<>();
			map1.put(groupPropertyId, map2);
		}
		Counter counter = map2.get(groupPropertyValue);
		if (counter == null) {
			counter = new Counter();
			map2.put(groupPropertyValue, counter);
		}
		return counter;
	}

	/*
	 * Increment the number of groups for the given
	 * (GroupTypeId,GroupPropertyId,property value) triplet
	 */
	private void increment(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		getCounter(groupTypeId, groupPropertyId, groupPropertyValue).count++;
	}

	private GroupsDataManager groupsDataManager;

	private final Map<GroupTypeId, Set<GroupPropertyId>> includedProperties = new LinkedHashMap<>();
	private final Map<GroupTypeId, Set<GroupPropertyId>> excludedProperties = new LinkedHashMap<>();
	private final boolean includeNewProperties;

	@Override
	protected void prepare(final ReportContext reportContext) {

		groupsDataManager = reportContext.getDataManager(GroupsDataManager.class);

		// subscribe to events
		reportContext.subscribe(GroupAdditionEvent.class, this::handleGroupAdditionEvent);
		reportContext.subscribe(GroupImminentRemovalEvent.class, this::handleGroupImminentRemovalEvent);
		reportContext.subscribe(GroupPropertyUpdateEvent.class, this::handleGroupPropertyUpdateEvent);
		reportContext.subscribe(GroupPropertyDefinitionEvent.class, this::handleGroupPropertyDefinitionEvent);

		/*
		 * if we are supposed to add new properties, then we will add all the
		 * existing properties that are not explicitly excluded
		 */
		if (includeNewProperties) {

			for (GroupTypeId groupTypeId : groupsDataManager.getGroupTypeIds()) {
				Set<GroupPropertyId> inclusionSet = includedProperties.get(groupTypeId);
				if (inclusionSet == null) {
					inclusionSet = new LinkedHashSet<>();
					includedProperties.put(groupTypeId, inclusionSet);
				}
				inclusionSet.addAll(groupsDataManager.getGroupPropertyIds(groupTypeId));

				Set<GroupPropertyId> exclusionSet = excludedProperties.get(groupTypeId);
				if (exclusionSet != null) {
					inclusionSet.removeAll(exclusionSet);
				}

			}
		}
		/*
		 * Initialize the buckets containing what we will report, careful to
		 * only add buckets for group properties that both currently exist and
		 * are included in this report.
		 *
		 */

		for (GroupId groupId : groupsDataManager.getGroupIds()) {
			GroupTypeId groupType = groupsDataManager.getGroupType(groupId);
			if (includedProperties.containsKey(groupType)) {
				Set<GroupPropertyId> includedSet = includedProperties.get(groupType);
				for (GroupPropertyId groupPropertyId : groupsDataManager.getGroupPropertyIds(groupType)) {
					if (includedSet.contains(groupPropertyId)) {
						Object groupPropertyValue = groupsDataManager.getGroupPropertyValue(groupId, groupPropertyId);
						increment(groupType, groupPropertyId, groupPropertyValue);
					}
				}
			}
		}

	}

	private void handleGroupPropertyDefinitionEvent(ReportContext reportContext, GroupPropertyDefinitionEvent groupPropertyDefinitionEvent) {

		GroupTypeId groupTypeId = groupPropertyDefinitionEvent.groupTypeId();
		GroupPropertyId groupPropertyId = groupPropertyDefinitionEvent.groupPropertyId();

		// if the property is explicitly excluded, then we are done
		Set<GroupPropertyId> excludedGroupPropertyIds = excludedProperties.get(groupTypeId);
		if (excludedGroupPropertyIds != null) {
			if (excludedGroupPropertyIds.contains(groupPropertyId)) {
				return;
			}
		}

		
		if (includeNewProperties) {
			// add the property to the included properties if it is missing
			Set<GroupPropertyId> includedGroupPropertyIds = includedProperties.get(groupTypeId);
			if (includedGroupPropertyIds == null) {
				includedGroupPropertyIds = new LinkedHashSet<>();
				includedProperties.put(groupTypeId, includedGroupPropertyIds);
			}
			includedGroupPropertyIds.add(groupPropertyId);
		} else {
			// if we are not accepting all new properties, then the property must be
			// an explicitly included property
			Set<GroupPropertyId> set = includedProperties.get(groupTypeId);
			if (set == null) {
				return;
			}
			if (!set.contains(groupPropertyId)) {
				return;
			}
		}
		

		List<GroupId> groups = groupsDataManager.getGroupsForGroupType(groupPropertyDefinitionEvent.groupTypeId());

		for (GroupId groupId : groups) {
			Object groupPropertyValue = groupsDataManager.getGroupPropertyValue(groupId, groupPropertyId);
			increment(groupTypeId, groupPropertyId, groupPropertyValue);
		}

	}

	private void handleGroupPropertyUpdateEvent(ReportContext reportContext, GroupPropertyUpdateEvent groupPropertyUpdateEvent) {
		GroupId groupId = groupPropertyUpdateEvent.groupId();
		GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
		GroupPropertyId groupPropertyId = groupPropertyUpdateEvent.groupPropertyId();

		Set<GroupPropertyId> set = includedProperties.get(groupTypeId);
		if (set == null) {
			return;
		}
		if (!set.contains(groupPropertyId)) {
			return;
		}

		Object previousPropertyValue = groupPropertyUpdateEvent.previousPropertyValue();
		Object currentPropertyValue = groupPropertyUpdateEvent.currentPropertyValue();

		increment(groupTypeId, groupPropertyId, currentPropertyValue);
		decrement(groupTypeId, groupPropertyId, previousPropertyValue);

	}

	private void handleGroupAdditionEvent(ReportContext reportContext, GroupAdditionEvent groupAdditionEvent) {
		GroupId groupId = groupAdditionEvent.groupId();
		final GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);

		Set<GroupPropertyId> set = includedProperties.get(groupTypeId);
		if (set == null) {
			return;
		}
		for (GroupPropertyId groupPropertyId : set) {
			final Object groupPropertyValue = groupsDataManager.getGroupPropertyValue(groupId, groupPropertyId);
			increment(groupTypeId, groupPropertyId, groupPropertyValue);
		}

	}

	private void handleGroupImminentRemovalEvent(ReportContext reportContext, GroupImminentRemovalEvent groupImminentRemovalEvent) {
		GroupId groupId = groupImminentRemovalEvent.groupId();
		final GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);

		Set<GroupPropertyId> set = includedProperties.get(groupTypeId);
		if (set == null) {
			return;
		}
		for (GroupPropertyId groupPropertyId : set) {
			final Object groupPropertyValue = groupsDataManager.getGroupPropertyValue(groupId, groupPropertyId);
			decrement(groupTypeId, groupPropertyId, groupPropertyValue);
		}
	}
}