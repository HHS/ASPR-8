package plugins.regions.datacontainers;

import java.util.ArrayList;
import java.util.List;

import nucleus.Context;
import nucleus.DataView;
import plugins.compartments.support.CompartmentError;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import util.ContractException;

/**
 * Published data view that provides region to person relationship information.
 * 
 *
 * @author Shawn Hatch
 *
 */
public final class RegionLocationDataView implements DataView {
	private RegionLocationDataManager regionLocationDataManager;
	private final Context context;
	private PersonDataView personDataView;
	private RegionDataView regionDataView;

	/**
	 * Constructs this data view from the given context and data manager.
	 * 
	 * @throws RuntimeException
	 *             <li>if the context is null</li>
	 *             <li>if the region location data manager is null</li>
	 * 
	 */
	public RegionLocationDataView(Context context, RegionLocationDataManager regionLocationDataManager) {
		if (regionLocationDataManager == null) {
			throw new RuntimeException("null region location data manager");
		}
		this.context = context;
		this.regionLocationDataManager = regionLocationDataManager;
		personDataView = context.getDataView(PersonDataView.class).get();
		regionDataView = context.getDataView(RegionDataView.class).get();
	}

	/**
	 * Returns the region associated with the given person id.
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the compartment id is unknown
	 */
	public <T extends RegionId> T getPersonRegion(final PersonId personId) {
		validatePersonExists(personId);
		return regionLocationDataManager.getPersonRegion(personId);
	}

	/**
	 * Returns the time when then person arrived at their current region.
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the compartment id is unknown
	 *                          <li>{@linkplain RegionError#REGION_ARRIVAL_TIMES_NOT_TRACKED}
	 *                          if the region arrival times are not being
	 *                          tracked</li>
	 * 
	 */
	public double getPersonRegionArrivalTime(final PersonId personId) {
		validatePersonExists(personId);
		validatePersonRegionArrivalsTimesTracked();
		return regionLocationDataManager.getPersonRegionArrivalTime(personId);
	}

	/**
	 * Returns the number of people currently in the given region.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *             region id is null
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *             the region id is not known
	 */
	public int getRegionPopulationCount(final RegionId regionId) {
		validateRegionId(regionId);
		return regionLocationDataManager.getRegionPopulationCount(regionId);
	}

	/**
	 * Returns the time when the current population of the given region was
	 * established.
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain RegionError#NULL_REGION_ID}
	 *                          if the region id is null
	 *                          <li>{@linkplain RegionError#UNKNOWN_REGION_ID}
	 *                          if the region id is not known
	 */

	public double getRegionPopulationTime(final RegionId regionId) {
		validateRegionId(regionId);
		return regionLocationDataManager.getRegionPopulationTime(regionId);
	}

	/**
	 * Returns the policy for tracking the last region arrival time for
	 * each person
	 */
	public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy() {
		return regionLocationDataManager.getPersonRegionArrivalTrackingPolicy();
	}

	/**
	 * Returns as a List the person identifiers of the people in the given
	 * region. List elements are unique.
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain RegionError#NULL_REGION_ID} if
	 *                          the c id is null
	 *                          <li>{@linkplain CompartmentError#UNKNOWN_REGION_ID}
	 *                          if the region id is not known
	 */
	public List<PersonId> getPeopleInRegion(final RegionId regionId) {
		// validateRegionId(regionId);
		// return regionLocationDataManager.getPeopleInRegion(regionId);
		//
		validateRegionId(regionId);
		int[] peopleInRegion = regionLocationDataManager.getPeopleInRegion(regionId);
		int n = peopleInRegion.length;
		List<PersonId> result = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			result.add(personDataView.getBoxedPersonId(peopleInRegion[i]));
		}
		return result;
	}

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			context.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			context.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void validatePersonRegionArrivalsTimesTracked() {
		if (regionLocationDataManager.getPersonRegionArrivalTrackingPolicy() != TimeTrackingPolicy.TRACK_TIME) {
			context.throwContractException(RegionError.REGION_ARRIVAL_TIMES_NOT_TRACKED);
		}
	}

	private void validateRegionId(final RegionId regionId) {

		if (regionId == null) {
			context.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionDataView.regionIdExists(regionId)) {
			context.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}
}
