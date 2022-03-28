package plugins.groups.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import nucleus.ActorContext;
import plugins.groups.GroupDataManager;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

/**
 * A periodic Report that displays the number of groups having a particular
 * number of people for a given group type.
 *
 * Fields
 *
 * GroupType -- the group type of group
 *
 * PersonCount -- the number of people in each group
 * 
 * GroupCount -- the number of groups having the person count
 *
 * @author Shawn Hatch
 *
 */
public final class GroupPopulationReport extends PeriodicReport {

	public GroupPopulationReport(ReportId reportId,ReportPeriod reportPeriod) {
		super(reportId,reportPeriod);
	}

	/*
	 * 
	 * Count of the number of groups having a particular person count for a
	 * particular group type
	 *
	 */
	private static class Counter {
		int count;
	}

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder)//
																	.add("GroupType")//
																	.add("PersonCount")//
																	.add("GroupCount")//
																	.build();//
		}
		return reportHeader;
	}

	@Override
	protected void flush(ActorContext actorContext) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();

		/*
		 * Count the number of groups of each size that exist for each group
		 * type
		 */
		Map<GroupTypeId, Map<Integer, Counter>> groupTypePopulationMap = new LinkedHashMap<>();
		for (GroupTypeId groupTypeId : groupDataManager.getGroupTypeIds()) {
			Map<Integer, Counter> groupSizeMap = new LinkedHashMap<>();
			groupTypePopulationMap.put(groupTypeId, groupSizeMap);
			for (GroupId groupId : groupDataManager.getGroupsForGroupType(groupTypeId)) {
				Integer personCountForGroup = groupDataManager.getPersonCountForGroup(groupId);
				Counter counter = groupSizeMap.get(personCountForGroup);
				if (counter == null) {
					counter = new Counter();
					groupSizeMap.put(personCountForGroup, counter);
				}
				counter.count++;
			}
		}

		/*
		 * Report the collected group counters
		 */
		for (final GroupTypeId groupTypeId : groupTypePopulationMap.keySet()) {
			Map<Integer, Counter> groupSizeMap = groupTypePopulationMap.get(groupTypeId);
			for (final Integer personCount : groupSizeMap.keySet()) {
				Counter counter = groupSizeMap.get(personCount);

				final int groupCount = counter.count;
				reportItemBuilder.setReportHeader(getReportHeader());
				reportItemBuilder.setReportId(getReportId());
				fillTimeFields(reportItemBuilder);
				reportItemBuilder.addValue(groupTypeId.toString());
				reportItemBuilder.addValue(personCount);
				reportItemBuilder.addValue(groupCount);
				ReportItem reportItem = reportItemBuilder.build();
				actorContext.releaseOutput(reportItem);

			}
		}

	}

	private GroupDataManager groupDataManager;

	@Override
	public void init(ActorContext actorContext) {
		super.init(actorContext);
		groupDataManager = actorContext.getDataManager(GroupDataManager.class).get();
	}

}