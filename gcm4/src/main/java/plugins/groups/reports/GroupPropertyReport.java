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

	/*
	 * Returns the counter corresponding to the given group type, group property
	 * id and group property value. Adds the counter if it does not already
	 * exist
	 */
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
	private final Map<GroupTypeId, Set<GroupPropertyId>> currentProperties = new LinkedHashMap<>();
	private final Map<GroupTypeId, Set<GroupPropertyId>> excludedProperties = new LinkedHashMap<>();
	private final boolean includeNewProperties;

	private boolean isCurrentProperty(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
		boolean result = false;
		Set<GroupPropertyId> set = currentProperties.get(groupTypeId);
		if (set != null) {
			result = set.contains(groupPropertyId);
		}
		return result;
	}

	private boolean addToCurrentProperties(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {

		// There are eight possibilities:

		/*
		 * P -- the default inclusion policy
		 * 
		 * I -- the property is explicitly included
		 * 
		 * X -- the property is explicitly excluded
		 * 
		 * C -- the property should be on the current properties
		 * 
		 * 
		 * P I X C Table
		 * 
		 * TRUE TRUE FALSE TRUE
		 * 
		 * TRUE FALSE FALSE TRUE
		 * 
		 * FALSE TRUE FALSE TRUE
		 * 
		 * FALSE FALSE FALSE FALSE
		 * 
		 * TRUE TRUE TRUE FALSE -- not possible
		 * 
		 * TRUE FALSE TRUE FALSE
		 * 
		 * FALSE TRUE TRUE FALSE -- not possible
		 * 
		 * FALSE FALSE TRUE FALSE
		 * 
		 * 
		 * Two of the cases above are contradictory since a property cannot be
		 * both explicitly included and explicitly excluded
		 * 
		 */

		// if X is true then we don't add the property
		Set<GroupPropertyId> set = excludedProperties.get(groupTypeId);
		if (set != null) {
			if (set.contains(groupPropertyId)) {
				return false;
			}
		}

		// if both P and I are false we don't add the property
		boolean included = false;
		set = includedProperties.get(groupTypeId);
		if (set != null) {
			included = set.contains(groupPropertyId);
		}

		if (!included && !includeNewProperties) {
			return false;
		}

		// we have failed to reject the property
		set = currentProperties.get(groupTypeId);
		if (set == null) {
			set = new LinkedHashSet<>();
			currentProperties.put(groupTypeId, set);
		}
		set.add(groupPropertyId);

		return true;
	}

	@Override
	protected void prepare(final ReportContext reportContext) {

		groupsDataManager = reportContext.getDataManager(GroupsDataManager.class);

		// subscribe to events
		reportContext.subscribe(GroupAdditionEvent.class, this::handleGroupAdditionEvent);
		reportContext.subscribe(GroupImminentRemovalEvent.class, this::handleGroupImminentRemovalEvent);
		reportContext.subscribe(GroupPropertyUpdateEvent.class, this::handleGroupPropertyUpdateEvent);
		reportContext.subscribe(GroupPropertyDefinitionEvent.class, this::handleGroupPropertyDefinitionEvent);

		// update the current properties from the existing properties found in
		// the data manager
		for (GroupTypeId groupTypeId : groupsDataManager.getGroupTypeIds()) {
			for (GroupPropertyId groupPropertyId : groupsDataManager.getGroupPropertyIds(groupTypeId)) {				
				addToCurrentProperties(groupTypeId, groupPropertyId);
				System.out.println("Added "+groupTypeId+" "+groupPropertyId+" to current properties");
			}
		}

		/*
		 * Initialize the buckets containing what we will report
		 *
		 */
		for (GroupId groupId : groupsDataManager.getGroupIds()) {
			GroupTypeId groupType = groupsDataManager.getGroupType(groupId);
			if (currentProperties.containsKey(groupType)) {
				for (GroupPropertyId groupPropertyId : currentProperties.get(groupType)) {
					Object groupPropertyValue = groupsDataManager.getGroupPropertyValue(groupId, groupPropertyId);
					increment(groupType, groupPropertyId, groupPropertyValue);
				}
			}
		}
	}

	private void handleGroupPropertyDefinitionEvent(ReportContext reportContext, GroupPropertyDefinitionEvent groupPropertyDefinitionEvent) {

		final GroupTypeId groupTypeId = groupPropertyDefinitionEvent.groupTypeId();
		final GroupPropertyId groupPropertyId = groupPropertyDefinitionEvent.groupPropertyId();
		final boolean added = addToCurrentProperties(groupTypeId, groupPropertyId);

		if (added) {
			List<GroupId> groups = groupsDataManager.getGroupsForGroupType(groupPropertyDefinitionEvent.groupTypeId());
			for (GroupId groupId : groups) {
				Object groupPropertyValue = groupsDataManager.getGroupPropertyValue(groupId, groupPropertyId);
				increment(groupTypeId, groupPropertyId, groupPropertyValue);
			}
		}
	}

	private void handleGroupPropertyUpdateEvent(ReportContext reportContext, GroupPropertyUpdateEvent groupPropertyUpdateEvent) {

		final GroupId groupId = groupPropertyUpdateEvent.groupId();
		final GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);
		final GroupPropertyId groupPropertyId = groupPropertyUpdateEvent.groupPropertyId();

		if (isCurrentProperty(groupTypeId, groupPropertyId)) {
			Object previousPropertyValue = groupPropertyUpdateEvent.previousPropertyValue();
			Object currentPropertyValue = groupPropertyUpdateEvent.currentPropertyValue();
			increment(groupTypeId, groupPropertyId, currentPropertyValue);
			decrement(groupTypeId, groupPropertyId, previousPropertyValue);
		}
	}

	private void handleGroupAdditionEvent(ReportContext reportContext, GroupAdditionEvent groupAdditionEvent) {
		final GroupId groupId = groupAdditionEvent.groupId();
		final GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);

		Set<GroupPropertyId> groupPropertyIds = currentProperties.get(groupTypeId);
		if (groupPropertyIds != null) {
			for (GroupPropertyId groupPropertyId : groupPropertyIds) {
				final Object groupPropertyValue = groupsDataManager.getGroupPropertyValue(groupId, groupPropertyId);
				increment(groupTypeId, groupPropertyId, groupPropertyValue);
			}
		}
	}

	private void handleGroupImminentRemovalEvent(ReportContext reportContext, GroupImminentRemovalEvent groupImminentRemovalEvent) {
		final GroupId groupId = groupImminentRemovalEvent.groupId();
		final GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);

		Set<GroupPropertyId> groupPropertyIds = currentProperties.get(groupTypeId);
		if (groupPropertyIds != null) {
			for (GroupPropertyId groupPropertyId : groupPropertyIds) {
				final Object groupPropertyValue = groupsDataManager.getGroupPropertyValue(groupId, groupPropertyId);
				decrement(groupTypeId, groupPropertyId, groupPropertyValue);
			}
		}
	}
}