package plugins.people.resolvers;




import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nucleus.DataManagerContext;
import plugins.compartments.support.CompartmentError;
import plugins.people.datacontainers.PersonDataManager;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.BulkPersonCreationEvent;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.events.mutation.PersonRemovalRequestEvent;
import plugins.people.events.mutation.PopulationGrowthProjectionEvent;
import plugins.people.events.observation.BulkPersonCreationObservationEvent;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import util.ContractException;
/**
 * 
 * Provides event resolution for the {@linkplain PersonPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain PersonDataView}. Initializes the data view from
 * the {@linkplain PersonInitialData} instance provided to the plugin.
 * </P>
 * 
 * 
 * <P>
 * Initializes all event labelers defined by
 * {@linkplain BulkPersonCreationObservationEvent},
 * {@linkplain PersonCreationObservationEvent} and
 * {@linkplain PersonImminentRemovalObservationEvent}
 * </P>
 * 
 * <P>
 * Resolves the following events:
 * <ul>
 * 
 * <li>{@linkplain PopulationGrowthProjectionEvent} <blockquote>Increases memory
 * allocation within the {@linkplain PersonDataManager} to allow
 * for more efficient bulk person addition.
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@link PersonError#NEGATIVE_GROWTH_PROJECTION} if the growth count is negative  
 * </ul>
 * 
 * 
 * </blockquote></li>
 * 
 * <li>{@linkplain PersonRemovalRequestEvent} <blockquote> 
 *  Plans the removal the person from the simulation.
 *  Generates a corresponding {@linkplain PersonImminentRemovalObservationEvent}.
 *  The plan for removal is executed at the current time,
 *  but after the current activation of this resolver. 
 *  This allows other resolvers, agents and reports to have reference to the person
 *  prior to the person's removal and take relevant actions. The {@linkplain PersonDataView} is
 *  updated to reflect the removal.
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@link PersonError#NULL_PERSON_ID} if the person id is null 
 * </ul>
 * </blockquote></li>
 * 
 * 
 * <li>{@linkplain PersonCreationEvent} <blockquote>Adds a new person to the simulation, updating the
 * {@linkplain PersonDataView} and generating a corresponding
 * {@linkplain PersonCreationObservationEvent}
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * 
 * <ul>
 * <li>{@linkplain PersonError#NULL_PERSON_CONTRUCTION_DATA} if the person construction data is null</li>
 * </ul> 
 * 
 * </blockquote></li>
 * 
 * <li>{@linkplain BulkPersonCreationEvent}<blockquote> Adds new people to the simulation, updating the
 * {@linkplain PersonDataView} and generating a corresponding
 * {@linkplain BulkPersonCreationObservationEvent}.
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain CompartmentError#NULL_BULK_PERSON_CONTRUCTION_DATA} if the bulk person construction data is null</li>
 * </ul>
 * </blockquote></li>
 * 
 * <ul>
 * </p>
 * 
 * 
 * 
 * @author Shawn Hatch
 *
 */
public final class PersonEventResolver {
	private PeopleInitialData peopleInitialData;

	public PersonEventResolver(PeopleInitialData peopleInitialData) {
		this.peopleInitialData = peopleInitialData;
	}

	private PersonDataManager personDataManager;

	private void handlePopulationGrowthProjectiontEventExecution(final DataManagerContext dataManagerContext, final PopulationGrowthProjectionEvent populationGrowthProjectionEvent) {
		personDataManager.expandCapacity(populationGrowthProjectionEvent.getCount());
	}

	private void validateGrowthCount(DataManagerContext dataManagerContext, int count) {
		if (count < 0) {
			dataManagerContext.throwContractException(PersonError.NEGATIVE_GROWTH_PROJECTION);
		}
	}

	private void handlePopulationGrowthProjectiontEventValidation(final DataManagerContext dataManagerContext, final PopulationGrowthProjectionEvent populationGrowthProjectionEvent) {
		validateGrowthCount(dataManagerContext, populationGrowthProjectionEvent.getCount());
	}

	private void validatePersonContructionDataNotNull(DataManagerContext dataManagerContext,PersonContructionData personContructionData) {
		if(personContructionData == null) {
			dataManagerContext.throwContractException(PersonError.NULL_PERSON_CONTRUCTION_DATA);
		}
	}
	
	private void handlePersonCreationEventExecutionValidation(final DataManagerContext dataManagerContext, final PersonCreationEvent personCreationEvent) {
		validatePersonContructionDataNotNull(dataManagerContext,personCreationEvent.getPersonContructionData());
	}
	
	private void handlePersonCreationEventExecution(final DataManagerContext dataManagerContext, final PersonCreationEvent personCreationEvent) {
		PersonContructionData personContructionData = personCreationEvent.getPersonContructionData();
		PersonId personId = personDataManager.addPersonId();
		PersonCreationObservationEvent personCreationObservationEvent = new PersonCreationObservationEvent(personId, personContructionData);
		dataManagerContext.resolveEvent(personCreationObservationEvent);
	}

	private void validateBulkPersonContructionDataNotNull(DataManagerContext dataManagerContext, BulkPersonContructionData bulkPersonContructionData) {
		if(bulkPersonContructionData == null) {
			dataManagerContext.throwContractException(PersonError.NULL_BULK_PERSON_CONTRUCTION_DATA);
		}
	}
	
	private void handleBulkPersonCreationEventValidation(final DataManagerContext dataManagerContext, final BulkPersonCreationEvent bulkPersonCreationEvent) {
		validateBulkPersonContructionDataNotNull(dataManagerContext,bulkPersonCreationEvent.getBulkPersonContructionData());
	}
	
	private void handleBulkPersonCreationEventExecution(final DataManagerContext dataManagerContext, final BulkPersonCreationEvent bulkPersonCreationEvent) {
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		PersonId personId = null;
		int count = personContructionDatas.size();
		for (int i = 0; i < count; i++) {
			if (personId == null) {
				personId = personDataManager.addPersonId();
			} else {
				personDataManager.addPersonId();
			}
		}
		BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent = new BulkPersonCreationObservationEvent(personId, bulkPersonContructionData);
		dataManagerContext.resolveEvent(bulkPersonCreationObservationEvent);
	}

	public void init(final DataManagerContext dataManagerContext) {

		dataManagerContext.addEventLabeler(BulkPersonCreationObservationEvent.getEventLabeler());

		dataManagerContext.addEventLabeler(PersonCreationObservationEvent.getEventLabeler());

		dataManagerContext.addEventLabeler(PersonImminentRemovalObservationEvent.getEventLabeler());

		dataManagerContext.subscribeToEventExecutionPhase(PopulationGrowthProjectionEvent.class, this::handlePopulationGrowthProjectiontEventExecution);
		dataManagerContext.subscribeToEventValidationPhase(PopulationGrowthProjectionEvent.class, this::handlePopulationGrowthProjectiontEventValidation);

		dataManagerContext.subscribeToEventExecutionPhase(PersonCreationEvent.class, this::handlePersonCreationEventExecution);
		dataManagerContext.subscribeToEventValidationPhase(PersonCreationEvent.class, this::handlePersonCreationEventExecutionValidation);

		dataManagerContext.subscribeToEventExecutionPhase(BulkPersonCreationEvent.class, this::handleBulkPersonCreationEventExecution);
		dataManagerContext.subscribeToEventValidationPhase(BulkPersonCreationEvent.class, this::handleBulkPersonCreationEventValidation);

		dataManagerContext.subscribeToEventValidationPhase(PersonRemovalRequestEvent.class, this::handlePersonRemovalRequestEventValidation);
		dataManagerContext.subscribeToEventExecutionPhase(PersonRemovalRequestEvent.class, this::handlePersonRemovalRequestEventExecution);

		final List<PersonId> scenarioPeopleIds = new ArrayList<>(peopleInitialData.getPersonIds());
		personDataManager = new PersonDataManager(dataManagerContext.getSafeContext(), scenarioPeopleIds.size());
		Collections.sort(scenarioPeopleIds);
		final Map<PersonId, PersonId> scenarioToSimPeopleMap = new LinkedHashMap<>();
		for (final PersonId scenarioPersonId : scenarioPeopleIds) {
			PersonId simulationPersonId = personDataManager.addPersonId();
			scenarioToSimPeopleMap.put(scenarioPersonId, simulationPersonId);
		}

		personDataManager.setScenarioToSimPeopleMap(scenarioToSimPeopleMap);
		dataManagerContext.publishDataView(new PersonDataView(personDataManager));
		peopleInitialData = null;
	}

	private void validatePersonExists(final DataManagerContext dataManagerContext, final PersonId personId) {
		if (personId == null) {
			dataManagerContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataManager.personExists(personId)) {
			dataManagerContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void handlePersonRemovalRequestEventValidation(final DataManagerContext dataManagerContext, final PersonRemovalRequestEvent personRemovalRequestEvent) {
		final PersonId personId = personRemovalRequestEvent.getPersonId();
		validatePersonExists(dataManagerContext, personId);
	}

	private void handlePersonRemovalRequestEventExecution(final DataManagerContext dataManagerContext, final PersonRemovalRequestEvent personRemovalRequestEvent) {
		final PersonId personId = personRemovalRequestEvent.getPersonId();
		dataManagerContext.resolveEvent(new PersonImminentRemovalObservationEvent(personId));
		dataManagerContext.addPlan((context) -> personDataManager.removePerson(personRemovalRequestEvent.getPersonId()), dataManagerContext.getTime());
	}

}
