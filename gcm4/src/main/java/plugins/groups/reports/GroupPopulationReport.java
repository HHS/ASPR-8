package plugins.groups.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import nucleus.ReportContext;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
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
 *
 */
public final class GroupPopulationReport extends PeriodicReport {

	public GroupPopulationReport(ReportLabel reportLabel,ReportPeriod reportPeriod) {
		super(reportLabel,reportPeriod);
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
																	.add("group_type")//
																	.add("person_count")//
																	.add("group_count")//
																	.build();//
		}
		return reportHeader;
	}

	@Override
	protected void flush(ReportContext reportContext) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();

		/*
		 * Count the number of groups of each size that exist for each group
		 * type
		 */
		Map<GroupTypeId, Map<Integer, Counter>> groupTypePopulationMap = new LinkedHashMap<>();
		for (GroupTypeId groupTypeId : groupsDataManager.getGroupTypeIds()) {
			Map<Integer, Counter> groupSizeMap = new LinkedHashMap<>();
			groupTypePopulationMap.put(groupTypeId, groupSizeMap);
			for (GroupId groupId : groupsDataManager.getGroupsForGroupType(groupTypeId)) {
				Integer personCountForGroup = groupsDataManager.getPersonCountForGroup(groupId);
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
				reportItemBuilder.setReportLabel(getReportLabel());
				fillTimeFields(reportItemBuilder);
				reportItemBuilder.addValue(groupTypeId.toString());
				reportItemBuilder.addValue(personCount);
				reportItemBuilder.addValue(groupCount);
				ReportItem reportItem = reportItemBuilder.build();
				reportContext.releaseOutput(reportItem);

			}
		}

	}

	private GroupsDataManager groupsDataManager;

	@Override
	public void init(ReportContext reportContext) {
		super.init(reportContext);
		groupsDataManager = reportContext.getDataManager(GroupsDataManager.class);
	}

}