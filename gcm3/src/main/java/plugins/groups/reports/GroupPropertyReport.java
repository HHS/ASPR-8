package plugins.groups.reports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import nucleus.ActorContext;
import nucleus.EventLabel;
import plugins.groups.GroupCreationObservationEvent;
import plugins.groups.GroupDataManager;
import plugins.groups.events.GroupImminentRemovalObservationEvent;
import plugins.groups.events.GroupPropertyChangeObservationEvent;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

/**
 * A periodic Report that displays the number of groups having particular values
 * for each group property for a given group type. Only non-zero person counts
 * are reported. The report is further limited to the
 * (GroupType,GroupPropertyId) pairs contained in the
 * GroupPropertyReportSettings instance used to initialize this report.
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
 * @author Shawn Hatch
 *
 */
public final class GroupPropertyReport extends PeriodicReport {

	private static class Scaffold {
		private ReportPeriod reportPeriod = ReportPeriod.DAILY;
		private final Map<GroupTypeId, Set<GroupPropertyId>> clientPropertyMap = new LinkedHashMap<>();
		private final Set<GroupTypeId> allProperties = new LinkedHashSet<>();
		private ReportId reportId;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Scaffold scaffold = new Scaffold();

		private Builder() {
		}

		/**
		 * Returns the GroupPropertyReport instance
		 */
		public GroupPropertyReport build() {
			try {

				return new GroupPropertyReport(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the report period for this report
		 * 
		 * @throws RuntimeException
		 *             <li>if the report period is null
		 *             <li>if the report period END_OF_SIMULATION
		 */
		public Builder setReportPeriod(ReportPeriod reportPeriod) {
			if (reportPeriod == null) {
				throw new RuntimeException("null report period");
			}

			if (reportPeriod == ReportPeriod.END_OF_SIMULATION) {
				throw new RuntimeException("cannot be " + ReportPeriod.END_OF_SIMULATION);
			}
			scaffold.reportPeriod = reportPeriod;
			return this;
		}
		
		/**
		 * Sets the report period for this report
		 * 
		 * @throws RuntimeException
		 *             <li>if the report period is null
		 *             <li>if the report period END_OF_SIMULATION
		 */
		public Builder setReportId(ReportId reportId) {
			if (reportId == null) {
				throw new RuntimeException("null report id");
			}
			
			scaffold.reportId = reportId;
			return this;
		}

		/**
		 * Adds all properties for the given group type id
		 * 
		 * @throws RuntimeException
		 *             <li>if the group type id is null
		 */
		public Builder addAllProperties(GroupTypeId groupTypeId) {
			if (groupTypeId == null) {
				throw new RuntimeException("null group type id");
			}
			scaffold.allProperties.add(groupTypeId);
			return this;
		}

		/**
		 * Adds all properties for the given group type id
		 * 
		 * @throws RuntimeException
		 *             <li>if the group type id is null
		 *             <li>if the group property id is null
		 */
		public Builder addProperty(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
			if (groupTypeId == null) {
				throw new RuntimeException("null group type id");
			}
			if (groupPropertyId == null) {
				throw new RuntimeException("null group property id");
			}
			Set<GroupPropertyId> set = scaffold.clientPropertyMap.get(groupTypeId);
			if (set == null) {
				set = new LinkedHashSet<>();
				scaffold.clientPropertyMap.put(groupTypeId, set);
			}
			set.add(groupPropertyId);
			return this;
		}

	}

	private final Scaffold scaffold;

	private GroupPropertyReport(Scaffold scaffold) {
		super(scaffold.reportId,scaffold.reportPeriod);
		this.scaffold = scaffold;
	}

	private static class Counter {
		int count;
	}

	/*
	 * The set of (GroupTypeId,GroupPropertyId) pairs collected from the
	 * GroupPropertyReportSettings supplied during initialization
	 */
	private final Map<GroupTypeId, Set<GroupPropertyId>> clientPropertyMap = new LinkedHashMap<>();

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
	protected void flush(ActorContext actorContext) {

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
						reportItemBuilder.setReportId(getReportId());

						fillTimeFields(reportItemBuilder);
						reportItemBuilder.addValue(groupTypeId.toString());
						reportItemBuilder.addValue(groupPropertyId.toString());
						reportItemBuilder.addValue(groupPropertyValue);
						reportItemBuilder.addValue(personCount);

						actorContext.releaseOutput(reportItemBuilder.build());
					}
				}
			}
		}
	}

	private Counter getCounter(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		final Map<Object, Counter> propertyValueMap = groupTypeMap.get(groupTypeId).get(groupPropertyId);
		Counter counter = propertyValueMap.get(groupPropertyValue);
		if (counter == null) {
			counter = new Counter();
			propertyValueMap.put(groupPropertyValue, counter);
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

	private GroupDataManager groupDataManager;

	@Override
	public void init(final ActorContext actorContext) {
		super.init(actorContext);

		groupDataManager = actorContext.getDataManager(GroupDataManager.class).get();

		// transfer all VALID property selections from the scaffold
		Set<GroupTypeId> groupTypeIds = groupDataManager.getGroupTypeIds();
		for (GroupTypeId groupTypeId : groupTypeIds) {
			Set<GroupPropertyId> groupPropertyIds = new LinkedHashSet<>();
			if (scaffold.allProperties.contains(groupTypeId)) {
				groupPropertyIds.addAll(groupDataManager.getGroupPropertyIds(groupTypeId));
			} else {
				Set<GroupPropertyId> selectedPropertyIds = scaffold.clientPropertyMap.get(groupTypeId);
				if (selectedPropertyIds != null) {
					Set<GroupPropertyId> allPropertyIds = groupDataManager.getGroupPropertyIds(groupTypeId);
					for (GroupPropertyId groupPropertyId : allPropertyIds) {
						if (selectedPropertyIds.contains(groupPropertyId)) {
							groupPropertyIds.add(groupPropertyId);
						}
					}
				}
			}
			clientPropertyMap.put(groupTypeId, groupPropertyIds);
		}

		// determine the subscriptions for group creation
		if (clientPropertyMap.keySet().equals(groupDataManager.getGroupTypeIds())) {
			
			actorContext.subscribe(GroupCreationObservationEvent.class, getFlushingConsumer(this::handleGroupCreationObservationEvent));
		} else {
			for (GroupTypeId groupTypeId : clientPropertyMap.keySet()) {
				EventLabel<GroupCreationObservationEvent> eventLabelByGroupType = GroupCreationObservationEvent.getEventLabelByGroupType(actorContext, groupTypeId);
				actorContext.subscribe(eventLabelByGroupType, getFlushingConsumer(this::handleGroupCreationObservationEvent));
			}
		}

		//determine the subscriptions for group removal observations
		if (clientPropertyMap.keySet().equals(groupDataManager.getGroupTypeIds())) {
			actorContext.subscribe(GroupImminentRemovalObservationEvent.class, getFlushingConsumer(this::handleGroupDestructionObservationEvent));
		} else {
			for (GroupTypeId groupTypeId : clientPropertyMap.keySet()) {
				EventLabel<GroupImminentRemovalObservationEvent> eventLabelByGroupType = GroupImminentRemovalObservationEvent.getEventLabelByGroupType(actorContext, groupTypeId);
				actorContext.subscribe(eventLabelByGroupType, getFlushingConsumer(this::handleGroupDestructionObservationEvent));
			}
		}
		
		//determine the subscriptions for group property changes		
		boolean allPropertiesRequired = false;
		if (clientPropertyMap.keySet().equals(groupDataManager.getGroupTypeIds())) {
			allPropertiesRequired = true;
			for (GroupTypeId groupTypeId : clientPropertyMap.keySet()) {
				if(!clientPropertyMap.get(groupTypeId).equals(groupDataManager.getGroupPropertyIds(groupTypeId))) {
					allPropertiesRequired = false;
					break;
				}
			}
		}
		
		if(allPropertiesRequired) {
			actorContext.subscribe(GroupPropertyChangeObservationEvent.class, getFlushingConsumer(this::handleGroupPropertyChangeObservationEvent));	
		}else {
			for (GroupTypeId groupTypeId : clientPropertyMap.keySet()) {
				if(clientPropertyMap.get(groupTypeId).equals(groupDataManager.getGroupPropertyIds(groupTypeId))) {
					EventLabel<GroupPropertyChangeObservationEvent> eventLabelByGroupType = GroupPropertyChangeObservationEvent.getEventLabelByGroupType(actorContext, groupTypeId);
					actorContext.subscribe(eventLabelByGroupType, getFlushingConsumer(this::handleGroupPropertyChangeObservationEvent));
				}else {
					for(GroupPropertyId groupPropertyId : clientPropertyMap.get(groupTypeId)) {
						EventLabel<GroupPropertyChangeObservationEvent> eventLabelByGroupTypeAndProperty = GroupPropertyChangeObservationEvent.getEventLabelByGroupTypeAndProperty(actorContext, groupTypeId, groupPropertyId);
						actorContext.subscribe(eventLabelByGroupTypeAndProperty, getFlushingConsumer(this::handleGroupPropertyChangeObservationEvent));
					}
				}
			}
		}

		/*
		 * Fill the top layers of the groupTypeMap. We do not yet know the set
		 * of property values, so we leave that layer empty.
		 *
		 */

		for (GroupTypeId groupTypeId : clientPropertyMap.keySet()) {
			final Map<GroupPropertyId, Map<Object, Counter>> propertyIdMap = new LinkedHashMap<>();
			groupTypeMap.put(groupTypeId, propertyIdMap);
			Set<GroupPropertyId> groupPropertyIds = clientPropertyMap.get(groupTypeId);
			for (final GroupPropertyId groupPropertyId : groupPropertyIds) {
				final Map<Object, Counter> propertyValueMap = new LinkedHashMap<>();
				propertyIdMap.put(groupPropertyId, propertyValueMap);
			}
		}

		// group addition
		for (GroupId groupId : groupDataManager.getGroupIds()) {
			final GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
			if (clientPropertyMap.containsKey(groupTypeId)) {
				for (final GroupPropertyId groupPropertyId : clientPropertyMap.get(groupTypeId)) {
					final Object groupPropertyValue = groupDataManager.getGroupPropertyValue(groupId, groupPropertyId);
					increment(groupTypeId, groupPropertyId, groupPropertyValue);
				}
			}
		}
	}

	private void handleGroupPropertyChangeObservationEvent(ActorContext actorContext, GroupPropertyChangeObservationEvent groupPropertyChangeObservationEvent) {
		GroupId groupId = groupPropertyChangeObservationEvent.getGroupId();

		final GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
		if (clientPropertyMap.containsKey(groupTypeId)) {

			GroupPropertyId groupPropertyId = groupPropertyChangeObservationEvent.getGroupPropertyId();
			Object previousPropertyValue = groupPropertyChangeObservationEvent.getPreviousPropertyValue();
			Object currentPropertyValue = groupPropertyChangeObservationEvent.getCurrentPropertyValue();

			if (clientPropertyMap.get(groupTypeId).contains(groupPropertyId)) {

				increment(groupTypeId, groupPropertyId, currentPropertyValue);
				decrement(groupTypeId, groupPropertyId, previousPropertyValue);
			}
		}

	}

	private void handleGroupCreationObservationEvent(ActorContext actorContext, GroupCreationObservationEvent groupCreationObservationEvent) {
		GroupId groupId = groupCreationObservationEvent.getGroupId();
		final GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
		if (clientPropertyMap.containsKey(groupTypeId)) {
			for (final GroupPropertyId groupPropertyId : clientPropertyMap.get(groupTypeId)) {
				final Object groupPropertyValue = groupDataManager.getGroupPropertyValue(groupId, groupPropertyId);
				increment(groupTypeId, groupPropertyId, groupPropertyValue);
			}
		}
	}

	private void handleGroupDestructionObservationEvent(ActorContext actorContext, GroupImminentRemovalObservationEvent groupImminentRemovalObservationEvent) {
		GroupId groupId = groupImminentRemovalObservationEvent.getGroupId();
		final GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);
		if (clientPropertyMap.containsKey(groupTypeId)) {
			Set<GroupPropertyId> groupPropertyIds = groupDataManager.getGroupPropertyIds(groupTypeId);
			for (final GroupPropertyId groupPropertyId : groupPropertyIds) {
				if (clientPropertyMap.get(groupTypeId).contains(groupPropertyId)) {
					final Object groupPropertyValue = groupDataManager.getGroupPropertyValue(groupId, groupPropertyId);
					decrement(groupTypeId, groupPropertyId, groupPropertyValue);
				}
			}
		}
	}
}