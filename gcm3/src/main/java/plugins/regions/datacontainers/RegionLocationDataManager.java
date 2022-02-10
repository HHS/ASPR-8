package plugins.regions.datacontainers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import nucleus.SimulationContext;
import plugins.people.support.PersonId;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionId;
import util.arraycontainers.DoubleValueContainer;
import util.arraycontainers.IntValueContainer;

/**
 * Mutable data manager that backs the {@linkplain RegionLocationDataView}.
 * This data manager is for internal use by the {@link RegionPlugin} and
 * should not be published.
 *
 * @author Shawn Hatch
 *
 */
public final class RegionLocationDataManager {

	/*
	 * Record for maintaining the number of people either globally, regionally
	 * or by compartment. Also maintains the time when the population count was
	 * last changed. PopulationRecords are maintained to eliminate iterations
	 * over other tracking structures to answer queries about population counts.
	 */
	private static class PopulationRecord {
		private int populationCount;
		private double assignmentTime;
	}

	/*
	 * Tracking record for the total number of people in each region.
	 */
	private final Map<RegionId, PopulationRecord> regionPopulationRecordMap = new LinkedHashMap<>();

	/*
	 * Supports the conversion of region ids into int values.
	 */
	private final Map<RegionId, Integer> regionToIndexMap = new LinkedHashMap<>();

	/*
	 * Supports conversion of int into RegionId values
	 */
	private final RegionId[] indexToRegionMap;

	/*
	 * Stores region identifiers as int values indexed by person id values
	 */
	private final IntValueContainer regionValues;

	/*
	 * Stores double region arrival values indexed by person id values.
	 * Maintenance depends upon tracking policy.
	 */
	private DoubleValueContainer regionArrivalTimes;

	private final TimeTrackingPolicy regionArrivalTrackingPolicy;

	/*
	 * Aids with conversion of int based person identifiers into the existing
	 * PersonIds.
	 */

	private final SimulationContext simulationContext;
	/**
	 * Creates this data manager from the given {@link SimulationContext} and
	 * {@link RegionInitialData}. Not null tolerant.
	 * 
	 * @throw {@link RuntimeException}
	 *        <li>if the context is null</li>
	 *        <li>if the region initial data is null</li>
	 * 
	 */
	public RegionLocationDataManager(final SimulationContext simulationContext, final RegionInitialData regionInitialData) {
		if (simulationContext == null) {
			throw new RuntimeException("null context supplied");
		}

		this.simulationContext = simulationContext;


		/*
		 * By setting the default value to 0, we are allowing the container to
		 * grow without having to set values in its array. HOWEVER, THIS IMPLIES
		 * THAT REGIONS MUST BE CONVERTED TO INTEGER VALUES STARTING AT ONE, NOT
		 * ZERO.
		 *
		 * The same holds true for compartments.
		 */
		regionValues = new IntValueContainer(0);

		regionArrivalTrackingPolicy = regionInitialData.getPersonRegionArrivalTrackingPolicy();
		if (regionArrivalTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
			regionArrivalTimes = new DoubleValueContainer(0);
		}

		for (final RegionId regionId : regionInitialData.getRegionIds()) {
			regionPopulationRecordMap.put(regionId, new PopulationRecord());
		}

		// Note that regions are numbered starting with one and not zero to take
		// advantage of using zero as the default value in the regionValues
		// container
		final Set<RegionId> regionIds = regionInitialData.getRegionIds();
		int index = 1;
		for (final RegionId regionId : regionIds) {

			regionToIndexMap.put(regionId, index++);
		}

		indexToRegionMap = new RegionId[regionIds.size() + 1];
		index = 1;
		for (final RegionId regionId : regionIds) {

			indexToRegionMap[index++] = regionId;
		}

	}
	
	/**
	 * Expands the capacity of data structures to hold people by the given
	 * count. Used to more efficiently prepare for bulk population additions.
	 */	
	public void expandCapacity(final int count) {
		regionValues.setCapacity(regionValues.getCapacity() + count);
		if (regionArrivalTimes != null) {
			regionArrivalTimes.setCapacity(regionArrivalTimes.getCapacity() + count);
		}
	}

	/**
	 * Returns as an array the person indexes of the people in the given
	 * region. Array values are unique. Requires a valid region id.
	 * 
	 * @throws RuntimeException
	 *             <li>if the compartment id is null</li>
	 *             <li>if the compartment id is unknown</li>
	 */
	public int[] getPeopleInRegion(final RegionId regionId) {
		
		int targetCompartmentIndex = regionToIndexMap.get(regionId).intValue();

		int populationCount = regionPopulationRecordMap.get(regionId).populationCount;

		int[] result = new int[populationCount];

		int n = regionValues.size();
		int resultIndex = 0;
		for (int personIndex = 0; personIndex < n; personIndex++) {
			final int compartmentIndex = regionValues.getValueAsInt(personIndex);
			/*
			 * a region index of zero will not match any valid region,
			 * indicating that person does not exist
			 */
			if (targetCompartmentIndex == compartmentIndex) {
				result[resultIndex++] = personIndex;
			}
		}
		
		return result;		
	}

	/**
	 * Returns the region associated with the given person id. Requires a
	 * valid, existing person.
	 *
	 * @throws RuntimeException
	 *             <li>if the person id is null</li>
	 *             <li>if the person id is unknown</li>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionId> T getPersonRegion(final PersonId personId) {
		final int r = regionValues.getValueAsInt(personId.getValue());
		return (T) indexToRegionMap[r];
	}

	/**
	 * Returns the time when then person arrived at their current region.
	 * Requires a valid, existing person.
	 * 
	 * @throws RuntimeException
	 *             <li>if the person id is null</li>
	 *             <li>if the person id is unknown</li>
	 * 
	 */
	public double getPersonRegionArrivalTime(final PersonId personId) {
		return regionArrivalTimes.getValue(personId.getValue());
	}

	/**
	 * Returns the time when then person arrived at their current region.
	 * Requires a valid, existing person.
	 * 
	 * @throws RuntimeException
	 *             <li>if the person id is null</li>
	 *             <li>if the person id is unknown</li>
	 * 
	 */
	public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy() {
		return regionArrivalTrackingPolicy;
	}
	/**
	 * Returns the number of people currently in the given region. Requires
	 * a valid region id.
	 * 
	 * @throws RuntimeException
	 *             <li>if the region id is null</li>
	 *             <li>if the region id is unknown</li>
	 */
	public int getRegionPopulationCount(final RegionId regionId) {
		return regionPopulationRecordMap.get(regionId).populationCount;
	}
	/**
	 * Returns the time when the current population of the given region was
	 * established. Requires a valid region id.
	 * 
	 * @throws RuntimeException
	 *             <li>if the region id is null</li>
	 *             <li>if the region id is unknown</li>
	 * 
	 */
	public double getRegionPopulationTime(final RegionId regionId) {
		return regionPopulationRecordMap.get(regionId).assignmentTime;
	}

	/**
	 * Removes the person from this data manager.
	 * 
	 * Precondition : the person must exist and be stored in this manager
	 * 
	 * @throws RuntimeException
	 *             <li>if the person id is null</li>
	 *             <li>if the person id does not exist</li>
	 */
	public void removePerson(final PersonId personId) {
		final int regionIndex = regionValues.getValueAsInt(personId.getValue());
		final RegionId oldRegionId = indexToRegionMap[regionIndex];
		final PopulationRecord populationRecord = regionPopulationRecordMap.get(oldRegionId);
		populationRecord.populationCount--;
		populationRecord.assignmentTime = simulationContext.getTime();
		regionValues.setIntValue(personId.getValue(), 0);
	}

	/**
	 * Updates the region associated with the given person. The person must
	 * exist and the region id must be valid.
	 * 
	 * @throws RuntimeException
	 *             <li>if the person id is null</li>
	 *             <li>if the person id is unknown</li>
	 *             <li>if the region id is null</li>
	 *             <li>if the region id is unknown</li>
	 * 
	 */
	public void setPersonRegion(final PersonId personId, final RegionId regionId) {
		/*
		 * Retrieve the int value that represents the current region of the
		 * person
		 */
		// pop
		int regionIndex = regionValues.getValueAsInt(personId.getValue());
		RegionId oldRegionId;
		if (regionIndex > 0) {
			/*
			 * Convert the int reference into a region identifier
			 */
			// pop
			oldRegionId = indexToRegionMap[regionIndex];
			final PopulationRecord populationRecord = regionPopulationRecordMap.get(oldRegionId);
			/*
			 * Update the population count associated with the old region
			 */
			populationRecord.populationCount--;
			populationRecord.assignmentTime = simulationContext.getTime();
		} else {
			/*
			 * The person was not known to this manager, but we only update the
			 * global population on the change to a compartment
			 *
			 */
			oldRegionId = null;
		}
		/*
		 * Update the population count of the new region
		 */
		final PopulationRecord populationRecord = regionPopulationRecordMap.get(regionId);
		populationRecord.populationCount++;
		populationRecord.assignmentTime = simulationContext.getTime();
		/*
		 * Convert the new region id into an int
		 */
		// pop
		regionIndex = regionToIndexMap.get(regionId).intValue();
		/*
		 * Store in the int at the person's index
		 */
		// pop
		regionValues.setIntValue(personId.getValue(), regionIndex);
		/*
		 * If region arrival times are being tracked, do so.
		 */
		if (regionArrivalTimes != null) {
			// pop
			regionArrivalTimes.setValue(personId.getValue(), simulationContext.getTime());
		}

	}

}
