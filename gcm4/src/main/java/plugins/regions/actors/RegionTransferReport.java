package plugins.regions.actors;

import java.util.LinkedHashMap;
import java.util.Map;

import nucleus.ReportContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.PersonAdditionEvent;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import util.wrappers.MultiKey;
import util.wrappers.MutableInteger;

/**
 * A periodic Report that displays the number of times a person transferred from
 * one region to another. Transfers from a region to itself are interpreted as
 * the addition of people at that region. Removal of people is not reflected in
 * this report.
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
 *
 */
public final class RegionTransferReport extends PeriodicReport {

	public RegionTransferReport(ReportLabel reportLabel, ReportPeriod reportPeriod) {
		super(reportLabel, reportPeriod);
	}

	/*
	 * A mapping from a (Region, Region) tuple to a count of the number of
	 * transfers.
	 */
	private final Map<MultiKey, MutableInteger> baseMap = new LinkedHashMap<>();

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
	protected void flush(ReportContext reportContext) {		

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();

		for (final MultiKey multiKey : baseMap.keySet()) {
			RegionId sourceRegionId = multiKey.getKey(0);
			RegionId destinationRegionId = multiKey.getKey(1);
			MutableInteger mutableInteger = baseMap.get(multiKey);
			reportItemBuilder.setReportHeader(getReportHeader());
			reportItemBuilder.setReportId(getReportId());
			fillTimeFields(reportItemBuilder);
			reportItemBuilder.addValue(sourceRegionId.toString());
			reportItemBuilder.addValue(destinationRegionId.toString());
			reportItemBuilder.addValue(mutableInteger.getValue());
			reportContext.releaseOutput(reportItemBuilder.build());
		}

		baseMap.clear();

	}


	private void handlePersonAdditionEvent(ReportContext ReportContext, PersonAdditionEvent personAdditionEvent) {
		PersonId personId = personAdditionEvent.personId();
		final RegionId regionId = regionsDataManager.getPersonRegion(personId);
		increment(regionId, regionId);
	}

	private void handlePersonRegionUpdateEvent(ReportContext ReportContext, PersonRegionUpdateEvent personRegionUpdateEvent) {
		RegionId previousRegionId = personRegionUpdateEvent.previousRegionId();
		RegionId currentRegionId = personRegionUpdateEvent.currentRegionId();
		increment(previousRegionId, currentRegionId);
	}

	/*
	 * Increments the number of region transfers for the give tuple
	 */
	private void increment(final RegionId sourceRegionId, final RegionId destinationRegionId) {
		MultiKey multiKey = new MultiKey(sourceRegionId, destinationRegionId);
		MutableInteger mutableInteger = baseMap.get(multiKey);
		if (mutableInteger == null) {
			mutableInteger = new MutableInteger();
			baseMap.put(multiKey, mutableInteger);
		}
		mutableInteger.increment();
	}

	private RegionsDataManager regionsDataManager;

	@Override
	public void init(final ReportContext reportContext) {
		super.init(reportContext);
		PeopleDataManager peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);
		regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);

		
		subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);


		for (PersonId personId : peopleDataManager.getPeople()) {
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			increment(regionId, regionId);
		}
	}

}