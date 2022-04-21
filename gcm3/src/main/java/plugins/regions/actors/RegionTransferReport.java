package plugins.regions.actors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import nucleus.ActorContext;
import plugins.people.PersonDataManager;
import plugins.people.events.PersonAdditionEvent;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

/**
 * A periodic Report that displays the number of times a person transferred from
 * one region to another. Only non-zero transfers are reported.
 *
 *
 * Fields
 *
 *
 * SourceRegion -- the source region identifier
 *
 * DestinationRegion -- the destination region property identifier
 *
 * Transfers -- the number of transfers from the source region to the
 * destination region 
 *
 * @author Shawn Hatch
 *
 */
public final class RegionTransferReport extends PeriodicReport {

	public RegionTransferReport(ReportId reportId, ReportPeriod reportPeriod) {
		super(reportId, reportPeriod);
	}

	/*
	 * 
	 * A counter of the number of people transferring between regions.
	 *
	 */
	private static class Counter {
		int count;
	}

	/*
	 * A mapping from a (Region, Region) tuple to a count of the number of
	 * transfers.
	 */
	private final Map<RegionId, Map<RegionId, Counter>> baseMap = new LinkedHashMap<>();

	/*
	 * The derived header for this report
	 */
	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder)//

																	.add("source_region")//
																	.add("destination_region")//
																	.add("transfers")//
																	.build();//
		}
		return reportHeader;
	}

	@Override
	protected void flush(ActorContext ActorContext) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();

		for (final RegionId sourceRegionId : baseMap.keySet()) {
			final Map<RegionId, Counter> destinationRegionMap = baseMap.get(sourceRegionId);
			for (final RegionId destinationRegionId : destinationRegionMap.keySet()) {
				final Counter counter = destinationRegionMap.get(destinationRegionId);
				if (counter.count > 0) {
					reportItemBuilder.setReportHeader(getReportHeader());
					reportItemBuilder.setReportId(getReportId());
					fillTimeFields(reportItemBuilder);					
					reportItemBuilder.addValue(sourceRegionId.toString());
					reportItemBuilder.addValue(destinationRegionId.toString());
					reportItemBuilder.addValue(counter.count);
					ActorContext.releaseOutput(reportItemBuilder.build());
					counter.count = 0;
				}
			}

		}
	}

	private void handlePersonAdditionEvent(ActorContext ActorContext, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.getPersonId();
		final RegionId regionId = regionDataManager.getPersonRegion(personId);
		increment(regionId, regionId);
	}

	private void handlePersonRegionUpdateEvent(ActorContext ActorContext, PersonRegionUpdateEvent personRegionUpdateEvent) {
		RegionId previousRegionId = personRegionUpdateEvent.getPreviousRegionId();
		RegionId currentRegionId = personRegionUpdateEvent.getCurrentRegionId();
		increment(previousRegionId, currentRegionId);
	}

	/*
	 * Increments the number of region transfers for the give tuple
	 */
	private void increment(final RegionId sourceRegionId, final RegionId destinationRegionId) {
		final Counter counter = baseMap.get(sourceRegionId).get(destinationRegionId);
		counter.count++;
	}

	private RegionDataManager regionDataManager;

	@Override
	public void init(final ActorContext ActorContext) {
		super.init(ActorContext);

		ActorContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		ActorContext.subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);

		PersonDataManager personDataManager = ActorContext.getDataManager(PersonDataManager.class);
		regionDataManager = ActorContext.getDataManager(RegionDataManager.class);
		RegionDataManager regionDataManager = ActorContext.getDataManager(RegionDataManager.class);

		final Set<RegionId> regionIds = regionDataManager.getRegionIds();

		/*
		 * Fill the base map with empty counters
		 */

		for (final RegionId sourceRegionId : regionIds) {
			final Map<RegionId, Counter> destinationRegionMap = new LinkedHashMap<>();
			baseMap.put(sourceRegionId, destinationRegionMap);
			for (final RegionId destinationRegionId : regionIds) {
				final Counter counter = new Counter();
				destinationRegionMap.put(destinationRegionId, counter);
			}
		}

		for (PersonId personId : personDataManager.getPeople()) {
			final RegionId regionId = regionDataManager.getPersonRegion(personId);
			increment(regionId, regionId);
		}
	}
}