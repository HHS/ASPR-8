package plugins.compartments.resolvers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import nucleus.AgentContext;
import nucleus.ResolverContext;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.datacontainers.CompartmentDataManager;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.datacontainers.CompartmentLocationDataManager;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.mutation.CompartmentPropertyValueAssignmentEvent;
import plugins.compartments.events.mutation.PersonCompartmentAssignmentEvent;
import plugins.compartments.events.observation.CompartmentPropertyChangeObservationEvent;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.components.events.ComponentConstructionEvent;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PopulationGrowthProjectionEvent;
import plugins.people.events.observation.BulkPersonCreationObservationEvent;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import util.ContractException;

/**
 * 
 * Provides event resolution for the {@linkplain CompartmentPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain CompartmentDataView} and the
 * {@linkplain CompartmentLocationDataView}. Initializes both data views from
 * the {@linkplain CompartmentInitialData} instance provided to the plugin.
 * </P>
 * <P>
 * Creates all compartment agents upon initialization.
 * </P>
 * 
 * <P>
 * Initializes all event labelers defined by
 * {@linkplain CompartmentPropertyChangeObservationEvent} and
 * {@linkplain PersonCompartmentChangeObservationEvent}
 * </P>
 * 
 * 
 * <P>
 * Resolves the following events:
 * <ul>
 * 
 * <li>{@linkplain PopulationGrowthProjectionEvent} <blockquote>Increases memory
 * allocation within the {@linkplain CompartmentLocationDataManager} to allow
 * for more efficient bulk person addition.</blockquote></li>
 * 
 * <li>{@linkplain PersonCompartmentAssignmentEvent} <blockquote> Updates the
 * person's current compartment and compartment arrival time in the
 * {@linkplain CompartmentLocationDataView}. Generates a corresponding
 * {@linkplain PersonCompartmentChangeObservationEvent} <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@link PersonError#NULL_PERSON_ID} if the person id is null
 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is unknown
 * <li>{@link CompartmentError#NULL_COMPARTMENT_ID} if the compartment id is
 * null
 * <li>{@link CompartmentError#UNKNOWN_COMPARTMENT_ID} if the compartment id is
 * unknown
 * <li>{@link CompartmentError#SAME_COMPARTMENT} if the compartment id is
 * currently assigned to the person
 * </ul>
 * </blockquote></li>
 * 
 * 
 * <li>{@linkplain CompartmentPropertyValueAssignmentEvent} <blockquote>Updates
 * the compartment's property value and time in the
 * {@linkplain CompartmentDataView} and generates a corresponding
 * {@linkplain CompartmentPropertyChangeObservationEvent}
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <li>{@link CompartmentError#NULL_COMPARTMENT_ID} if the compartment id is
 * null
 * <li>{@link CompartmentError#UNKNOWN_COMPARTMENT_ID} if the compartment id is
 * unknown
 * <li>{@link CompartmentError#NULL_COMPARTMENT_PROPERTY_ID} if the property id
 * is null
 * <li>{@link CompartmentError#UNKNOWN_COMPARTMENT_PROPERTY_ID} if the property
 * id is unknown
 * <li>{@link CompartmentError#NULL_COMPARTMENT_PROPERTY_VALUE} if the value is
 * null
 * <li>{@link PropertyError#INCOMPATIBLE_VALUE} if the value is incompatible
 * with the defined type for the property
 * <li>{@link PropertyError#IMMUTABLE_VALUE} if the property has been defined as
 * immutable
 * 
 * </blockquote></li>
 * 
 * <li>{@linkplain PersonCreationObservationEvent}<blockquote> Sets the person's
 * initial compartment in the {@linkplain CompartmentLocationDataView} from the
 * compartment reference in the auxiliary data of the event.
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if no compartment data
 * was included in the event</li>
 * 
 * <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if the compartment
 * in the event is unknown</li>
 * </ul>
 * 
 * 
 * </blockquote></li>
 * 
 * <li>{@linkplain BulkPersonCreationObservationEvent}<blockquote> Sets each
 * person's initial compartment in the {@linkplain CompartmentLocationDataView}
 * from the compartment references in the auxiliary data of the event.
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain CompartmentError#NULL_COMPARTMENT_ID} if no compartment data
 * was included in the for some person in event</li>
 * 
 * <li>{@linkplain CompartmentError#UNKNOWN_COMPARTMENT_ID} if the compartment
 * is unknown for some person in the event</li>
 * </ul>
 * 
 * </blockquote></li>
 * 
 * <li>{@linkplain PersonImminentRemovalObservationEvent}<blockquote> Removes
 * the compartment assignment data for the person from the
 * {@linkplain CompartmentDataView} <BR>
 * <BR>
 * Throws {@linkplain ContractException}
 * <ul>
 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
 * unknown</li>
 * </ul>
 * 
 * </blockquote></li>
 * <ul>
 * </p>
 * 
 * 
 * 
 * @author Shawn Hatch
 *
 */
public final class CompartmentEventResolver {

	private CompartmentLocationDataManager compartmentLocationDataManager;

	private CompartmentInitialData compartmentInitialData;

	/**
	 * Creates this resolver from the the given {@link CompartmentInitialData}
	 * 
	 */
	public CompartmentEventResolver(CompartmentInitialData compartmentInitialData) {
		this.compartmentInitialData = compartmentInitialData;
	}

	private final Map<CompartmentId, Set<CompartmentPropertyId>> compartmentPropertyIds = new LinkedHashMap<>();

	private CompartmentDataManager compartmentDataManager;

	private Set<CompartmentId> compartmentIds;

	private void handlePersonImminentRemovalObservationEventExecution(final ResolverContext resolverContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		resolverContext.addPlan((context) -> compartmentLocationDataManager.removePerson(personImminentRemovalObservationEvent.getPersonId()), resolverContext.getTime());
	}

	private void handlePersonImminentRemovalObservationEventValidation(final ResolverContext resolverContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		validatePersonExists(resolverContext, personImminentRemovalObservationEvent.getPersonId());
	}

	private void handlePersonCreationObservationEventExecution(final ResolverContext resolverContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		PersonContructionData personContructionData = personCreationObservationEvent.getPersonContructionData();
		CompartmentId compartmentId = personContructionData.getValue(CompartmentId.class).orElse(null);
		compartmentLocationDataManager.setPersonCompartment(personId, compartmentId);
	}

	private void handlePersonCreationObservationEventValidation(final ResolverContext resolverContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonContructionData personContructionData = personCreationObservationEvent.getPersonContructionData();
		CompartmentId compartmentId = personContructionData.getValue(CompartmentId.class).orElse(null);
		validateCompartmentId(resolverContext, compartmentId);
	}

	private void handleBulkPersonCreationObservationEventExecution(final ResolverContext resolverContext, BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		PersonId personId = bulkPersonCreationObservationEvent.getPersonId();
		int pId = personId.getValue();
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		for (PersonContructionData personContructionData : personContructionDatas) {
			CompartmentId compartmentId = personContructionData.getValue(CompartmentId.class).orElse(null);
			PersonId boxedPersonId = personDataView.getBoxedPersonId(pId);
			compartmentLocationDataManager.setPersonCompartment(boxedPersonId, compartmentId);
			pId++;
		}
	}

	private void handleBulkPersonCreationObservationEventValidation(final ResolverContext resolverContext, BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		for (PersonContructionData personContructionData : personContructionDatas) {
			CompartmentId compartmentId = personContructionData.getValue(CompartmentId.class).orElse(null);
			validateCompartmentId(resolverContext, compartmentId);
		}
	}

	private void validatePersonExists(final ResolverContext resolverContext, final PersonId personId) {
		if (personId == null) {
			resolverContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			resolverContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void handlePopulationGrowthProjectiontEventExecution(final ResolverContext resolverContext, final PopulationGrowthProjectionEvent populationGrowthProjectionEvent) {
		compartmentLocationDataManager.expandCapacity(populationGrowthProjectionEvent.getCount());
	}

	private void handlePersonCompartmentAssignmentEventExecution(final ResolverContext resolverContext, final PersonCompartmentAssignmentEvent personCompartmentAssignmentEvent) {
		final PersonId personId = personCompartmentAssignmentEvent.getPersonId();
		final CompartmentId newCompartmentId = personCompartmentAssignmentEvent.getCompartmentId();
		final CompartmentId oldCompartmentId = compartmentLocationDataManager.getPersonCompartment(personId);
		compartmentLocationDataManager.setPersonCompartment(personId, newCompartmentId);
		resolverContext.queueEventForResolution(new PersonCompartmentChangeObservationEvent(personId, oldCompartmentId, newCompartmentId));
	}

	private void handlePersonCompartmentAssignmentEventValidation(final ResolverContext resolverContext, final PersonCompartmentAssignmentEvent personCompartmentAssignmentEvent) {
		final PersonId personId = personCompartmentAssignmentEvent.getPersonId();
		final CompartmentId newCompartmentId = personCompartmentAssignmentEvent.getCompartmentId();
		validatePersonExists(resolverContext, personId);
		validateCompartmentId(resolverContext, newCompartmentId);
		validatePersonNotInCompartment(resolverContext, personId, newCompartmentId);
	}

	/*
	 * Precondition : person must exist
	 */
	private void validatePersonNotInCompartment(final ResolverContext resolverContext, final PersonId personId, final CompartmentId compartmentId) {
		final CompartmentId currentCompartmentId = compartmentLocationDataManager.getPersonCompartment(personId);
		if (currentCompartmentId.equals(compartmentId)) {
			resolverContext.throwContractException(CompartmentError.SAME_COMPARTMENT, compartmentId);
		}
	}

	private PersonDataView personDataView;

	/**
	 * Initial behavior of this resolver.
	 * <ul>
	 * <li>Adds all event labelers defined by the following events <blockquote>
	 * <ul>
	 * <li> {@linkplain CompartmentPropertyChangeObservationEvent}</li>
	 * <li>{@linkplain PersonCompartmentChangeObservationEvent}</li>
	 * </ul>
	 * </blockquote></li>
	 * 
	 * <li>Subscribes to all handled events
	 * 
	 * <li>Establishes person compartment assignments from the
	 * {@linkplain CompartmentInitialData}</li>
	 * 
	 * <li>Sets compartment property values from the
	 * {@linkplain CompartmentInitialData}</li>
	 * 
	 * <li>Publishes the {@linkplain CompartmentDataView}</li>
	 * 
	 * <li>Publishes the {@linkplain CompartmentLocationDataView}</li>
	 * </ul>
	 */
	public void init(final ResolverContext resolverContext) {

		resolverContext.addEventLabeler(CompartmentPropertyChangeObservationEvent.getEventLabeler());

		resolverContext.subscribeToEventExecutionPhase(PopulationGrowthProjectionEvent.class, this::handlePopulationGrowthProjectiontEventExecution);

		resolverContext.subscribeToEventExecutionPhase(PersonCompartmentAssignmentEvent.class, this::handlePersonCompartmentAssignmentEventExecution);
		resolverContext.subscribeToEventValidationPhase(PersonCompartmentAssignmentEvent.class, this::handlePersonCompartmentAssignmentEventValidation);

		resolverContext.subscribeToEventExecutionPhase(CompartmentPropertyValueAssignmentEvent.class, this::handleCompartmentPropertyValueAssignmentEventExecution);
		resolverContext.subscribeToEventValidationPhase(CompartmentPropertyValueAssignmentEvent.class, this::handleCompartmentPropertyValueAssignmentEventValidation);

		resolverContext.subscribeToEventExecutionPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEventExecution);
		resolverContext.subscribeToEventValidationPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEventValidation);

		resolverContext.subscribeToEventExecutionPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventExecution);
		resolverContext.subscribeToEventValidationPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventValidation);

		resolverContext.subscribeToEventExecutionPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventExecution);
		resolverContext.subscribeToEventValidationPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventValidation);

		resolverContext.addEventLabeler(PersonCompartmentChangeObservationEvent.getEventLabelerForArrivalCompartment());
		resolverContext.addEventLabeler(PersonCompartmentChangeObservationEvent.getEventLabelerForDepartureCompartment());
		resolverContext.addEventLabeler(PersonCompartmentChangeObservationEvent.getEventLabelerForPerson());
		personDataView = resolverContext.getDataView(PersonDataView.class).get();

		compartmentDataManager = new CompartmentDataManager(resolverContext.getSafeContext());
		compartmentLocationDataManager = new CompartmentLocationDataManager(resolverContext, compartmentInitialData);

		compartmentIds = compartmentInitialData.getCompartmentIds();

		for (final CompartmentId compartmentId : compartmentInitialData.getCompartmentIds()) {
			compartmentPropertyIds.put(compartmentId, compartmentInitialData.getCompartmentPropertyIds(compartmentId));
		}

		addCompartments(resolverContext, compartmentInitialData);
		addCompartmentsPropertyDefinitions(resolverContext, compartmentInitialData);
		loadCompartmentPropertyValues(resolverContext, compartmentInitialData);

		for (CompartmentId compartmentId : compartmentIds) {
			Consumer<AgentContext> consumer = compartmentInitialData.getCompartmentInitialBehavior(compartmentId);
			resolverContext.queueEventForResolution(new ComponentConstructionEvent(compartmentId, consumer));
		}

		loadPeople(resolverContext, compartmentInitialData);

		resolverContext.publishDataView(new CompartmentDataView(resolverContext.getSafeContext(), compartmentDataManager));
		resolverContext.publishDataView(new CompartmentLocationDataView(resolverContext.getSafeContext(), compartmentLocationDataManager));
		compartmentInitialData = null;
	}

	private void loadPeople(final ResolverContext resolverContext, final CompartmentInitialData compartmentInitialData) {
		final Map<PersonId, PersonId> scenarioToSimPeopleMap = personDataView.getScenarioToSimPeopleMap();

		/*
		 * Show that every person contained in the compartment initial data
		 * exists in the person data view
		 */
		Set<PersonId> compartmentallyDefinedPeople = compartmentInitialData.getPersonIds();
		for (PersonId compartmentPersonId : compartmentallyDefinedPeople) {
			if (!scenarioToSimPeopleMap.containsKey(compartmentPersonId)) {
				resolverContext.throwContractException(PersonError.UNKNOWN_PERSON_ID, compartmentPersonId + " in compartment initial data");
			}
		}

		/*
		 * Show that every person in the person data view has a region
		 * assignment in the region initial data
		 */
		for (PersonId scenarioPersonID : scenarioToSimPeopleMap.keySet()) {
			if (!compartmentallyDefinedPeople.contains(scenarioPersonID)) {
				resolverContext.throwContractException(CompartmentError.MISSING_COMPARTMENT_ASSIGNMENT, scenarioPersonID);
			}
		}

		/*
		 * Record the assignments
		 */
		for (final PersonId scenarioPersonId : compartmentallyDefinedPeople) {
			CompartmentId compartmentId = compartmentInitialData.getPersonCompartment(scenarioPersonId);
			final PersonId personId = scenarioToSimPeopleMap.get(scenarioPersonId);
			compartmentLocationDataManager.setPersonCompartment(personId, compartmentId);
		}
	}

	private void addCompartments(final ResolverContext resolverContext, final CompartmentInitialData compartmentInitialData) {
		for (CompartmentId compartmentId : compartmentInitialData.getCompartmentIds()) {
			compartmentDataManager.addCompartmentId(compartmentId);
		}
	}

	private void addCompartmentsPropertyDefinitions(final ResolverContext resolverContext, final CompartmentInitialData compartmentInitialData) {
		for (CompartmentId compartmentId : compartmentInitialData.getCompartmentIds()) {
			Set<CompartmentPropertyId> compartmentPropertyIds = compartmentInitialData.getCompartmentPropertyIds(compartmentId);
			for (CompartmentPropertyId compartmentPropertyId : compartmentPropertyIds) {
				PropertyDefinition propertyDefinition = compartmentInitialData.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
				compartmentDataManager.addCompartmentPropertyDefinition(compartmentId, compartmentPropertyId, propertyDefinition);
			}
		}
	}

	private void loadCompartmentPropertyValues(final ResolverContext resolverContext, final CompartmentInitialData compartmentInitialData) {
		for (final CompartmentId compartmentId : compartmentInitialData.getCompartmentIds()) {
			for (final CompartmentPropertyId compartmentPropertyId : compartmentInitialData.getCompartmentPropertyIds(compartmentId)) {
				final Object compartmentPropertyValue = compartmentInitialData.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
				final PropertyDefinition propertyDefinition = compartmentDataManager.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
				validateValueCompatibility(resolverContext, compartmentPropertyId, propertyDefinition, compartmentPropertyValue);
				compartmentDataManager.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, compartmentPropertyValue);
			}
		}
	}

	private void handleCompartmentPropertyValueAssignmentEventExecution(final ResolverContext resolverContext, final CompartmentPropertyValueAssignmentEvent compartmentPropertyValueAssignmentEvent) {
		final CompartmentId compartmentId = compartmentPropertyValueAssignmentEvent.getCompartmentId();
		final CompartmentPropertyId compartmentPropertyId = compartmentPropertyValueAssignmentEvent.getCompartmentPropertyId();
		final Object compartmentPropertyValue = compartmentPropertyValueAssignmentEvent.getCompartmentPropertyValue();
		final Object oldPropertyValue = compartmentDataManager.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
		compartmentDataManager.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, compartmentPropertyValue);
		resolverContext.queueEventForResolution(new CompartmentPropertyChangeObservationEvent(compartmentId, compartmentPropertyId, oldPropertyValue, compartmentPropertyValue));
	}

	private void handleCompartmentPropertyValueAssignmentEventValidation(final ResolverContext resolverContext, final CompartmentPropertyValueAssignmentEvent compartmentPropertyValueAssignmentEvent) {
		final CompartmentId compartmentId = compartmentPropertyValueAssignmentEvent.getCompartmentId();
		final CompartmentPropertyId compartmentPropertyId = compartmentPropertyValueAssignmentEvent.getCompartmentPropertyId();
		final Object compartmentPropertyValue = compartmentPropertyValueAssignmentEvent.getCompartmentPropertyValue();
		validateCompartmentId(resolverContext, compartmentId);
		validateCompartmentProperty(resolverContext, compartmentId, compartmentPropertyId);
		final PropertyDefinition propertyDefinition = compartmentDataManager.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
		validatePropertyMutability(resolverContext, propertyDefinition);
		validateCompartmentPropertyValueNotNull(resolverContext, compartmentPropertyValue);
		validateValueCompatibility(resolverContext, compartmentPropertyId, propertyDefinition, compartmentPropertyValue);
	}

	/*
	 * Validates the compartment id
	 *
	 */
	private void validateCompartmentId(final ResolverContext resolverContext, final CompartmentId compartmentId) {
		if (compartmentId == null) {
			resolverContext.throwContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}

		if (!compartmentIds.contains(compartmentId)) {
			resolverContext.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId);
		}
	}

	private void validateCompartmentProperty(final ResolverContext resolverContext, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {

		if (compartmentPropertyId == null) {
			resolverContext.throwContractException(CompartmentError.NULL_COMPARTMENT_PROPERTY_ID);
		}
		final Set<CompartmentPropertyId> set = compartmentPropertyIds.get(compartmentId);
		if ((set == null) || !set.contains(compartmentPropertyId)) {
			resolverContext.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_PROPERTY_ID, compartmentPropertyId);
		}

	}

	private void validateCompartmentPropertyValueNotNull(final ResolverContext resolverContext, final Object propertyValue) {
		if (propertyValue == null) {
			resolverContext.throwContractException(CompartmentError.NULL_COMPARTMENT_PROPERTY_VALUE);
		}
	}

	private void validatePropertyMutability(final ResolverContext resolverContext, final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			resolverContext.throwContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void validateValueCompatibility(final ResolverContext resolverContext, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			resolverContext.throwContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

}
