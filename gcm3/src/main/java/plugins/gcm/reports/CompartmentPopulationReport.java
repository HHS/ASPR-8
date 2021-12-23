package plugins.gcm.reports;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import nucleus.ReportContext;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.support.CompartmentId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.PersonId;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

/**
 * A periodic Report that displays the number of people in each
 * region/compartment pair in a periodic manner. Only non-zero person counts are
 * reported.
 *
 * Fields
 *
 * Region -- the region identifier
 *
 * Compartment -- the compartment identifier
 *
 * PersonCount -- the number of people in the region/compartment pair
 *
 * @author Shawn Hatch
 *
 */
public final class CompartmentPopulationReport extends PeriodicReport {

	public CompartmentPopulationReport(ReportPeriod reportPeriod) {
		super(reportPeriod);		
	}

	/*
	 * Static class the represents the number of people in a
	 * (region,compartment) pair
	 */
	private static class Counter {
		int count;
	}

	/*
	 * (region, compartment) pairs maped to population counters
	 */
	private final Map<RegionId, Map<CompartmentId, Counter>> regionMap = new LinkedHashMap<>();

	/*
	 * The header for the report
	 */
	private ReportHeader reportHeader;

	/*
	 * Returns the report header and builds it if it is null.
	 */
	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder)//
																	.add("Region")//
																	.add("Compartment")//
																	.add("PersonCount")//
																	.build();//
		}
		return reportHeader;

	}

	/*
	 * Decrement the number of people in the region/compartment pair by 1
	 */
	private void decrement(final RegionId regionId, final CompartmentId compartmentId) {
		final Counter counter = regionMap.get(regionId).get(compartmentId);
		counter.count--;
	}

	@Override
	protected void flush(ReportContext reportContext) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		/*
		 * Report the population count for all region/compartment pairs that are
		 * not empty
		 */
		for (final RegionId regionId : regionMap.keySet()) {
			final Map<CompartmentId, Counter> compartmentMap = regionMap.get(regionId);
			for (final CompartmentId compartmentId : compartmentMap.keySet()) {
				final Counter counter = compartmentMap.get(compartmentId);
				final int personCount = counter.count;
				if (personCount > 0) {
					reportItemBuilder.setReportHeader(getReportHeader());
					reportItemBuilder.setReportId(reportContext.getCurrentReportId());
					fillTimeFields(reportItemBuilder);
					reportItemBuilder.addValue(regionId.toString());
					reportItemBuilder.addValue(compartmentId.toString());
					reportItemBuilder.addValue(personCount);
					reportContext.releaseOutput(reportItemBuilder.build());
				}
			}
		}
	}

	private void handlePersonRegionChangeObservationEvent(ReportContext reportContext,PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		RegionId sourceRegionId = personRegionChangeObservationEvent.getPreviousRegionId();
		final RegionId destinationRegionId = personRegionChangeObservationEvent.getCurrentRegionId();
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		decrement(sourceRegionId, compartmentId);
		increment(destinationRegionId, compartmentId);
	}

	private void handlePersonCompartmentChangeObservationEvent(ReportContext reportContext,PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		CompartmentId sourceCompartmentId = personCompartmentChangeObservationEvent.getPreviousCompartmentId();
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId destinationCompartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		decrement(regionId, sourceCompartmentId);
		increment(regionId, destinationCompartmentId);
	}

	private void handlePersonCreationObservationEvent(ReportContext reportContext,PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		increment(regionId, compartmentId);
	}

	private void handlePersonImminentRemovalObservationEvent(ReportContext reportContext,PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		PersonId personId = personImminentRemovalObservationEvent.getPersonId();
		RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		decrement(regionId, compartmentId);
	}

	/*
	 * Increment the number of people in the region/compartment pair by 1
	 */
	private void increment(final RegionId regionId, final CompartmentId compartmentId) {
		final Counter counter = regionMap.get(regionId).get(compartmentId);
		counter.count++;
	}

	private CompartmentLocationDataView compartmentLocationDataView;
	private RegionLocationDataView regionLocationDataView;

	@Override
	public void init(ReportContext reportContext) {
		super.init(reportContext);

		reportContext.subscribe(PersonCreationObservationEvent.class,this::handlePersonCreationObservationEvent);
		reportContext.subscribe(PersonImminentRemovalObservationEvent.class,this::handlePersonImminentRemovalObservationEvent);
		reportContext.subscribe(PersonCompartmentChangeObservationEvent.class, this::handlePersonCompartmentChangeObservationEvent);
		reportContext.subscribe(PersonRegionChangeObservationEvent.class, this::handlePersonRegionChangeObservationEvent);

		PersonDataView personDataView = reportContext.getDataView(PersonDataView.class).get();
		compartmentLocationDataView = reportContext.getDataView(CompartmentLocationDataView.class).get();
		regionLocationDataView = reportContext.getDataView(RegionLocationDataView.class).get();

		/*
		 * Fill the region map with empty counters
		 */
		final Set<CompartmentId> compartmentIds = reportContext.getDataView(CompartmentDataView.class).get().getCompartmentIds();
		final Set<RegionId> regionIds = reportContext.getDataView(RegionDataView.class).get().getRegionIds();
		for (final RegionId regionId : regionIds) {
			final Map<CompartmentId, Counter> compartmentMap = new LinkedHashMap<>();
			regionMap.put(regionId, compartmentMap);
			for (final CompartmentId compartmentId : compartmentIds) {
				final Counter counter = new Counter();
				compartmentMap.put(compartmentId, counter);
			}
		}

		for (PersonId personId : personDataView.getPeople()) {
			increment(regionLocationDataView.getPersonRegion(personId), compartmentLocationDataView.getPersonCompartment(personId));
		}
	}

}