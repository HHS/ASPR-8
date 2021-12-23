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
import plugins.people.support.PersonId;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.support.RegionId;
import plugins.reports.support.PeriodicReport;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;

/**
 * A periodic Report that displays the number of times a person transferred from
 * one compartment to another within a region. Only non-zero transfer counts are
 * reported.
 *
 *
 * Fields
 *
 * Region -- the region identifier
 *
 * SourceCompartment -- the source compartment identifier
 *
 * DestinationCompartment -- the destination compartment property identifier
 *
 * Transfers -- the number of transfers from the source compartment to the
 * destination compartment for people in the region
 *
 * @author Shawn Hatch
 *
 */
public final class CompartmentTransferReport extends PeriodicReport {

	public CompartmentTransferReport(ReportPeriod reportPeriod) {
		super(reportPeriod);
	}

	/*
	 * Static class that counts the number of transfers between two compartment
	 * within a region
	 */
	private static class Counter {
		int count;
	}

	/*
	 * Map of counters
	 */
	private final Map<RegionId, Map<CompartmentId, Map<CompartmentId, Counter>>> regionMap = new LinkedHashMap<>();

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
			reportHeader = addTimeFieldHeaders(reportHeaderBuilder)//
																	.add("Region")//
																	.add("SourceCompartment")//
																	.add("DestinationCompartment")//
																	.add("Transfers")//
																	.build();//
		}
		return reportHeader;
	}

	@Override
	protected void flush(ReportContext reportContext) {

		final ReportItem.Builder reportItemBuilder = ReportItem.builder();

		for (final RegionId regionId : regionMap.keySet()) {
			final Map<CompartmentId, Map<CompartmentId, Counter>> sourceCompartmentMap = regionMap.get(regionId);
			for (final CompartmentId sourceCompartmentId : sourceCompartmentMap.keySet()) {
				final Map<CompartmentId, Counter> destinationCompartmentMap = sourceCompartmentMap.get(sourceCompartmentId);
				for (final CompartmentId destinationCompartmentId : destinationCompartmentMap.keySet()) {
					final Counter counter = destinationCompartmentMap.get(destinationCompartmentId);
					if (counter.count > 0) {
						reportItemBuilder.setReportHeader(getReportHeader());
						reportItemBuilder.setReportId(reportContext.getCurrentReportId());

						fillTimeFields(reportItemBuilder);
						reportItemBuilder.addValue(regionId.toString());
						reportItemBuilder.addValue(sourceCompartmentId.toString());
						reportItemBuilder.addValue(destinationCompartmentId.toString());
						reportItemBuilder.addValue(counter.count);

						reportContext.releaseOutput(reportItemBuilder.build());
						counter.count = 0;
					}
				}
			}
		}
	}

	private void handlePersonCompartmentChangeObservationEvent(ReportContext reportContext,PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		CompartmentId sourceCompartmentId = personCompartmentChangeObservationEvent.getPreviousCompartmentId();
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId destinationCompartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		increment(regionId, sourceCompartmentId, destinationCompartmentId);
	}

	private void handlePersonCreationObservationEvent(ReportContext reportContext,PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		increment(regionId, compartmentId, compartmentId);
	}

	private void increment(final RegionId regionId, final CompartmentId sourceCompartmentId, final CompartmentId destinationCompartmentId) {
		final Counter counter = regionMap.get(regionId).get(sourceCompartmentId).get(destinationCompartmentId);
		counter.count++;
	}

	private CompartmentLocationDataView compartmentLocationDataView;
	private RegionLocationDataView regionLocationDataView;

	@Override
	public void init(final ReportContext context) {
		super.init(context);

		context.subscribe(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEvent);
		context.subscribe(PersonCompartmentChangeObservationEvent.class, this::handlePersonCompartmentChangeObservationEvent);

		PersonDataView personDataView = context.getDataView(PersonDataView.class).get();
		compartmentLocationDataView = context.getDataView(CompartmentLocationDataView.class).get();
		regionLocationDataView = context.getDataView(RegionLocationDataView.class).get();

		final Set<CompartmentId> compartmentIds = context.getDataView(CompartmentDataView.class).get().getCompartmentIds();
		final Set<RegionId> regionIds = context.getDataView(RegionDataView.class).get().getRegionIds();

		/*
		 * Fill the region map with empty counters
		 */
		for (final RegionId regionId : regionIds) {
			final Map<CompartmentId, Map<CompartmentId, Counter>> sourceCompartmentMap = new LinkedHashMap<>();
			regionMap.put(regionId, sourceCompartmentMap);
			for (final CompartmentId sourceCompartmentId : compartmentIds) {
				final Map<CompartmentId, Counter> destinationCompartmentMap = new LinkedHashMap<>();
				sourceCompartmentMap.put(sourceCompartmentId, destinationCompartmentMap);
				for (final CompartmentId destinationCompartmentId : compartmentIds) {
					final Counter counter = new Counter();
					destinationCompartmentMap.put(destinationCompartmentId, counter);
				}
			}
		}
		for (PersonId personId : personDataView.getPeople()) {
			final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
			final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
			increment(regionId, compartmentId, compartmentId);
		}
	}

}