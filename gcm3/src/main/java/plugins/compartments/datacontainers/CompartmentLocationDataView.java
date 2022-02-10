package plugins.compartments.datacontainers;

import java.util.ArrayList;
import java.util.List;

import nucleus.SimulationContext;
import nucleus.DataView;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.TimeTrackingPolicy;
import util.ContractException;

/**
 * Published data view that provides compartment to person relationship
 * information.
 * 
 *
 * @author Shawn Hatch
 *
 */
public final class CompartmentLocationDataView implements DataView {
	private final CompartmentLocationDataManager compartmentLocationDataManager;
	private final SimulationContext simulationContext;
	private PersonDataView personDataView;
	private CompartmentDataView compartmentDataView;

	/**
	 * Constructs this data view from the given context and data manager.
	 * 
	 * @throws RuntimeException
	 *             <li>if the context is null</li>
	 *             <li>if the compartment location data manager is null</li>
	 * 
	 */
	public CompartmentLocationDataView(SimulationContext simulationContext, CompartmentLocationDataManager compartmentLocationDataManager) {
		if (compartmentLocationDataManager == null) {
			throw new RuntimeException("null compartment location data manager");
		}

		this.simulationContext = simulationContext;
		this.compartmentLocationDataManager = compartmentLocationDataManager;
		personDataView = simulationContext.getDataView(PersonDataView.class).get();
		compartmentDataView = simulationContext.getDataView(CompartmentDataView.class).get();
	}

	/**
	 * Returns the number of people currently in the given compartment.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if
	 *             the compartment id is not known
	 */
	public int getCompartmentPopulationCount(final CompartmentId compartmentId) {
		validateCompartmentId(compartmentId);
		return compartmentLocationDataManager.getCompartmentPopulationCount(compartmentId);
	}

	/**
	 * Returns the time when the current population of the given compartment was
	 * established.
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID}
	 *                          if the compartment id is null
	 *                          <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID}
	 *                          if the compartment id is not known
	 */

	public double getCompartmentPopulationTime(final CompartmentId compartmentId) {
		validateCompartmentId(compartmentId);
		return compartmentLocationDataManager.getCompartmentPopulationTime(compartmentId);
	}

	/**
	 * Returns as a List the person identifiers of the people in the given
	 * compartment. List elements are unique.
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID}
	 *                          if the compartment id is null
	 *                          <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID}
	 *                          if the compartment id is not known
	 */
	public List<PersonId> getPeopleInCompartment(final CompartmentId compartmentId) {
		validateCompartmentId(compartmentId);
		int[] peopleInCompartment = compartmentLocationDataManager.getPeopleInCompartment(compartmentId);
		int n = peopleInCompartment.length;
		List<PersonId> result = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			result.add(personDataView.getBoxedPersonId(peopleInCompartment[i]));
		}
		return result;
	}

	/**
	 * Returns the compartment associated with the given person id.
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the compartment id is unknown
	 */
	public <T extends CompartmentId> T getPersonCompartment(final PersonId personId) {
		validatePersonExists(personId);
		return compartmentLocationDataManager.getPersonCompartment(personId);
	}

	/**
	 * Returns the time when then person arrived at their current compartment.
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null</li>
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the compartment id is unknown</li>
	 *                          <li>{@linkplain CompartmentError#COMPARTMENT_ARRIVAL_TIMES_NOT_TRACKED}
	 *                          if the compartment arrival times are not being
	 *                          tracked</li>
	 * 
	 */
	public double getPersonCompartmentArrivalTime(final PersonId personId) {
		validatePersonExists(personId);
		validatePersonCompartmentArrivalsTimesTracked();
		return compartmentLocationDataManager.getPersonCompartmentArrivalTime(personId);
	}

	/**
	 * Returns the policy for tracking the last compartment arrival time for
	 * each person
	 */
	public TimeTrackingPolicy getPersonCompartmentArrivalTrackingPolicy() {
		return compartmentLocationDataManager.getPersonCompartmentArrivalTrackingPolicy();
	}

	private void validateCompartmentId(final CompartmentId compartmentId) {
		if (compartmentId == null) {
			simulationContext.throwContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}

		if (!compartmentDataView.compartmentIdExists(compartmentId)) {
			simulationContext.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId);
		}
	}

	private void validatePersonCompartmentArrivalsTimesTracked() {
		if (compartmentLocationDataManager.getPersonCompartmentArrivalTrackingPolicy() != TimeTrackingPolicy.TRACK_TIME) {
			simulationContext.throwContractException(CompartmentError.COMPARTMENT_ARRIVAL_TIMES_NOT_TRACKED);
		}
	}

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			simulationContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			simulationContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

}
