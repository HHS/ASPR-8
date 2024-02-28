package gov.hhs.aspr.ms.gcm.plugins.regions.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.events.PersonAdditionEvent;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.events.PersonRegionUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.PeriodicReport;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.util.wrappers.MultiKey;
import gov.hhs.aspr.ms.util.wrappers.MutableInteger;

/**
 * A periodic Report that displays the number of times a person transferred from
 * one region to another. Transfers from a region to itself are interpreted as
 * the addition of people at that region. Removal of people is not reflected in
 * this report. Fields SourceRegion -- the source region identifier
 * DestinationRegion -- the destination region property identifier Transfers --
 * the number of transfers from the source region to the destination region
 */
public final class RegionTransferReport extends PeriodicReport {

	public RegionTransferReport(RegionTransferReportPluginData regionTransferReportPluginData) {
		super(regionTransferReportPluginData.getReportLabel(), regionTransferReportPluginData.getReportPeriod());
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

		for (final MultiKey multiKey : baseMap.keySet()) {
			RegionId sourceRegionId = multiKey.getKey(0);
			RegionId destinationRegionId = multiKey.getKey(1);
			MutableInteger mutableInteger = baseMap.get(multiKey);
			ReportItem.Builder reportItemBuilder = ReportItem.builder();
			reportItemBuilder.setReportHeader(getReportHeader());
			reportItemBuilder.setReportLabel(getReportLabel());
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

	private void handlePersonRegionUpdateEvent(ReportContext ReportContext,
			PersonRegionUpdateEvent personRegionUpdateEvent) {
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
	protected void prepare(final ReportContext reportContext) {
		PeopleDataManager peopleDataManager = reportContext.getDataManager(PeopleDataManager.class);
		regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);

		reportContext.subscribe(PersonAdditionEvent.class, this::handlePersonAdditionEvent);
		reportContext.subscribe(PersonRegionUpdateEvent.class, this::handlePersonRegionUpdateEvent);
		if (reportContext.stateRecordingIsScheduled()) {
			reportContext.subscribeToSimulationClose(this::recordSimulationState);
		}

		for (PersonId personId : peopleDataManager.getPeople()) {
			final RegionId regionId = regionsDataManager.getPersonRegion(personId);
			increment(regionId, regionId);
		}
	}

	private void recordSimulationState(ReportContext reportContext) {
		RegionTransferReportPluginData.Builder builder = RegionTransferReportPluginData.builder();
		builder.setReportLabel(getReportLabel());
		builder.setReportPeriod(getReportPeriod());
		reportContext.releaseOutput(builder.build());
	}

}