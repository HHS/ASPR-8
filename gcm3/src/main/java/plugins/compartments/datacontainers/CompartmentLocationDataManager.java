package plugins.compartments.datacontainers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import nucleus.SimulationContext;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.people.support.PersonId;
import plugins.properties.support.TimeTrackingPolicy;
import util.arraycontainers.DoubleValueContainer;
import util.arraycontainers.IntValueContainer;

/**
 * Mutable data manager that backs the {@linkplain CompartmentLocationDataView}.
 * This data manager is for internal use by the {@link CompartmentPlugin} and
 * should not be published.
 *
 * @author Shawn Hatch
 *
 */
public final class CompartmentLocationDataManager {

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
	 * Tracking record for the total number of people in each compartment.
	 */
	private final Map<CompartmentId, PopulationRecord> compartmentPopulationRecordMap = new LinkedHashMap<>();

	/*
	 * Supports the conversion of compartment ids into int values
	 */
	private final Map<CompartmentId, Integer> compartmentToIndexMap = new LinkedHashMap<>();

	/*
	 * Supports the conversion of ints into CompartmentId values
	 */
	private CompartmentId[] indexToCompartmentMap;

	/*
	 * Stores compartment identifiers as int values indexed by person id values
	 */
	private IntValueContainer compartmentValues;

	/*
	 * Stores double compartment arrival values indexed by person id values.
	 * Maintenance depends upon tracking policy.
	 */
	private DoubleValueContainer compartmentArrivalTimes;

	/*
	 * Stores the compartment arrival time tracking policy that is established
	 * from the initial data.
	 */
	private TimeTrackingPolicy compartmentArrivalTrackingPolicy;

	private SimulationContext simulationContext;

	/**
	 * Returns the number of people currently in the given compartment. Requires
	 * a valid compartment id.
	 * 
	 * @throws RuntimeException
	 *             <li>if the compartment id is null</li>
	 *             <li>if the compartment id is unknown</li>
	 */
	public int getCompartmentPopulationCount(final CompartmentId compartmentId) {
		return compartmentPopulationRecordMap.get(compartmentId).populationCount;
	}

	/**
	 * Returns the time when the current population of the given compartment was
	 * established. Requires a valid compartment id.
	 * 
	 * @throws RuntimeException
	 *             <li>if the compartment id is null</li>
	 *             <li>if the compartment id is unknown</li>
	 * 
	 */
	public double getCompartmentPopulationTime(final CompartmentId compartmentId) {
		return compartmentPopulationRecordMap.get(compartmentId).assignmentTime;
	}

	/**
	 * Returns as an array the person indexes of the people in the given
	 * compartment. Array values are unique. Requires a valid compartment id.
	 * 
	 * @throws RuntimeException
	 *             <li>if the compartment id is null</li>
	 *             <li>if the compartment id is unknown</li>
	 */
	public int[] getPeopleInCompartment(final CompartmentId compartmentId) {

		int targetCompartmentIndex = compartmentToIndexMap.get(compartmentId).intValue();

		int populationCount = compartmentPopulationRecordMap.get(compartmentId).populationCount;

		int[] result = new int[populationCount];

		int n = compartmentValues.size();
		int resultIndex = 0;
		for (int personIndex = 0; personIndex < n; personIndex++) {
			final int compartmentIndex = compartmentValues.getValueAsInt(personIndex);
			/*
			 * a compartment index of zero will not match any valid compartment,
			 * indicating that person does not exist
			 */
			if (targetCompartmentIndex == compartmentIndex) {
				result[resultIndex++] = personIndex;
			}
		}
		return result;
	}

	/**
	 * Returns the compartment associated with the given person id. Requires a
	 * valid, existing person.
	 *
	 * @throws RuntimeException
	 *             <li>if the person id is null</li>
	 *             <li>if the person id is unknown</li>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends CompartmentId> T getPersonCompartment(final PersonId personId) {
		final int compartmentIndex = compartmentValues.getValueAsInt(personId.getValue());
		return (T) indexToCompartmentMap[compartmentIndex];
	}

	/**
	 * Returns the time when then person arrived at their current compartment.
	 * Requires a valid, existing person.
	 * 
	 * @throws RuntimeException
	 *             <li>if the person id is null</li>
	 *             <li>if the person id is unknown</li>
	 * 
	 */
	public double getPersonCompartmentArrivalTime(final PersonId personId) {
		return compartmentArrivalTimes.getValue(personId.getValue());
	}

	/**
	 * Returns the policy for tracking the last compartment arrival time for
	 * each person
	 */
	public TimeTrackingPolicy getPersonCompartmentArrivalTrackingPolicy() {
		return compartmentArrivalTrackingPolicy;
	}

	/**
	 * Creates this data manager from the given {@link SimulationContext} and
	 * {@link CompartmentInitialData}. Not null tolerant.
	 * 
	 * @throw {@link RuntimeException}
	 *        <li>if the context is null</li>
	 *        <li>if the compartment initial data is null</li>
	 * 
	 */
	public CompartmentLocationDataManager(final SimulationContext simulationContext, CompartmentInitialData compartmentInitialData) {

		if (simulationContext == null) {
			throw new RuntimeException("null context supplied");
		}

		this.simulationContext = simulationContext;

		/*
		 * By setting the default value to 0, we are allowing the container to
		 * grow without having to set values in its array. HOWEVER, THIS IMPLIES
		 * THAT COMPARTMENTS MUST BE CONVERTED TO INTEGER VALUES STARTING AT
		 * ONE, NOT ZERO.
		 */
		compartmentValues = new IntValueContainer(0);

		compartmentArrivalTrackingPolicy = compartmentInitialData.getPersonCompartmentArrivalTrackingPolicy();
		if (compartmentArrivalTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
			compartmentArrivalTimes = new DoubleValueContainer(0);
		}

		for (final CompartmentId compartmentId : compartmentInitialData.getCompartmentIds()) {
			compartmentPopulationRecordMap.put(compartmentId, new PopulationRecord());
		}

		/*
		 * Note that compartments are numbered starting with one and not zero to
		 * take advantage of using zero as the default value in the regionValues
		 * container.
		 */
		final Set<CompartmentId> compartmentIds = compartmentInitialData.getCompartmentIds();
		int index = 1;
		for (final CompartmentId compartmentId : compartmentIds) {
			compartmentToIndexMap.put(compartmentId, index++);
		}
		indexToCompartmentMap = new CompartmentId[compartmentIds.size() + 1];
		index = 1;
		for (final CompartmentId compartmentId : compartmentIds) {
			indexToCompartmentMap[index++] = compartmentId;
		}

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
		final int compartmentIndex = compartmentValues.getValueAsInt(personId.getValue());
		final CompartmentId oldCompartmentId = indexToCompartmentMap[compartmentIndex];
		final PopulationRecord populationRecord = compartmentPopulationRecordMap.get(oldCompartmentId);
		populationRecord.populationCount--;
		populationRecord.assignmentTime = simulationContext.getTime();
		compartmentValues.setIntValue(personId.getValue(), 0);
	}

	/**
	 * Updates the compartment associated with the given person. The person must
	 * exist and the compartment id must be valid.
	 * 
	 * @throws RuntimeException
	 *             <li>if the person id is null</li>
	 *             <li>if the person id is unknown</li>
	 *             <li>if the compartment id is null</li>
	 *             <li>if the compartment id is unknown</li>
	 * 
	 */
	public void setPersonCompartment(final PersonId personId, final CompartmentId compartmentId) {
		/*
		 * Retrieve the int value that represents the current compartment of the
		 * person
		 */
		int compartmentIndex = compartmentValues.getValueAsInt(personId.getValue());
		CompartmentId oldCompartmentId;
		if (compartmentIndex > 0) {
			/*
			 * Convert the int reference into a compartment identifier
			 */
			oldCompartmentId = indexToCompartmentMap[compartmentIndex];
			final PopulationRecord populationRecord = compartmentPopulationRecordMap.get(oldCompartmentId);
			/*
			 * Update the population count associated with the old compartment
			 */
			populationRecord.populationCount--;
			populationRecord.assignmentTime = simulationContext.getTime();

		} else {
			/*
			 * The person was not known to this manager, so the old compartment
			 * is null and the global population must be incremented
			 */
			oldCompartmentId = null;
		}

		/*
		 * Update the population count of the new compartment
		 */
		final PopulationRecord populationRecord = compartmentPopulationRecordMap.get(compartmentId);
		populationRecord.populationCount++;
		populationRecord.assignmentTime = simulationContext.getTime();

		/*
		 * Convert the new compartment id into an int
		 */
		compartmentIndex = compartmentToIndexMap.get(compartmentId).intValue();
		/*
		 * Store in the int at the person's index
		 */
		compartmentValues.setIntValue(personId.getValue(), compartmentIndex);

		/*
		 * If compartment arrival times are being tracked, do so.
		 */
		if (compartmentArrivalTimes != null) {
			compartmentArrivalTimes.setValue(personId.getValue(), simulationContext.getTime());
		}

	}

	/**
	 * Expands the capacity of data structures to hold people by the given
	 * count. Used to more efficiently prepare for bulk population additions.
	 */	
	public void expandCapacity(int count) {
		compartmentValues.setCapacity(compartmentValues.getCapacity() + count);
		if (compartmentArrivalTimes != null) {
			compartmentArrivalTimes.setCapacity(compartmentArrivalTimes.getCapacity() + count);
		}
	}

}
