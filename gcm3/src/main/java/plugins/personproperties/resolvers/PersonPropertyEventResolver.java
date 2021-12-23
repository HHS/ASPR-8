package plugins.personproperties.resolvers;

import java.util.List;
import java.util.Map;

import nucleus.ResolverContext;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.observation.BulkPersonCreationObservationEvent;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.datacontainers.PersonPropertyDataManager;
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.events.mutation.PersonPropertyValueAssignmentEvent;
import plugins.personproperties.events.observation.PersonPropertyChangeObservationEvent;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.regions.datacontainers.RegionLocationDataView;
import util.ContractException;

/**
 * 
 * Provides event resolution for the {@linkplain PersonPropertiesPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain PersonPropertiesDataView}.
 * Initializes the data view from the {@linkplain PersonPropertiesInitialData}
 * instance provided to the plugin.
 * </P>
 * 
 * <P>
 * Initializes all event labelers defined by
 * {@linkplain PersonPropertyChangeObservationEvent}
 * </P>
 *  
 * <P>
 * Resolves the following events:
 * <ul>
 * 
 * <li>{@linkplain PersonPropertyValueAssignmentEvent} 
 * <blockquote>Updates the value of a person's property. Generates a corresponding {@linkplain PersonPropertyChangeObservationEvent}
 * <BR><BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@link PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is unknown</li>
 * <li>{@link PersonPropertyError#NULL_PERSON_PROPERTY_ID} if the person property id is null</li> 
 * <li>{@link PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID} if the person property id is unknown</li>
 * <li>{@link PersonPropertyError#NULL_PERSON_PROPERTY_VALUE} if the property value is null</li>
 * <li>{@link PropertyError#INCOMPATIBLE_VALUE} if the property value is not compatible with the corresponding property definition</li>
 * <li>{@link PropertyError#IMMUTABLE_VALUE} if the corresponding property definition marks the property as immutable</li>
 * </ul> 
 * </blockquote>
 * 
 * </li>
 * 
 * <li>{@linkplain PersonCreationObservationEvent} 
 * <blockquote> Sets person property values for newly added people based on the existence of PersonPropertyInitialization instances found in the PersonContructionData.  
 * <BR><BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@link PersonPropertyError#NULL_PERSON_PROPERTY_ID} if the event contains a PersonPropertyInitialization that has a null person property id</li> 
 * <li>{@link PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID} if the event contains a PersonPropertyInitialization that has an unknown person property id</li>
 * <li>{@link PersonPropertyError#NULL_PERSON_PROPERTY_VALUE} if the event contains a PersonPropertyInitialization that has a null person property value</li>
 * <li>{@link PropertyError#INCOMPATIBLE_VALUE} if the event contains a PersonPropertyInitialization that has a person property value that is not compatible with the corresponding property definition</li>
 * 
 * </ul>
 * </blockquote>
 * </li>
 * 
 * 
 * <li>{@linkplain BulkPersonCreationObservationEvent}
 * <blockquote>Sets person property values for newly added groups of people based on the existence of PersonPropertyInitialization instances found in the PersonContructionData.
 * <BR><BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@link PersonPropertyError#NULL_PERSON_PROPERTY_ID} if the event contains a PersonPropertyInitialization that has a null person property id</li> 
 * <li>{@link PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID} if the event contains a PersonPropertyInitialization that has an unknown person property id</li>
 * <li>{@link PersonPropertyError#NULL_PERSON_PROPERTY_VALUE }if the event contains a PersonPropertyInitialization that has a null person property value</li>
 * <li>{@link PropertyError#INCOMPATIBLE_VALUE} if the event contains a PersonPropertyInitialization that has a person property value that is not compatible with the corresponding property definition</li>
 * </ul>
 * </blockquote>
 * </li>
 * 
 * <li>{@linkplain PersonImminentRemovalObservationEvent}
 * <blockquote>Builds a plan to remove the person at the current time, allowing reports, agents and resolvers to access the person's current property values during the current event resolution.  
 * <BR><BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is unknown</li>
 * </ul>
 * </blockquote>
 * </li>
 * 
 * <ul>
 * </p>
 * 
 * 
 * 
 * @author Shawn Hatch
 *
 */
public final class PersonPropertyEventResolver {
	private PersonPropertyInitialData personPropertyInitialData;

	/**
	 * Constructs this resolver
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_INITIAL_DATA}
	 *             if the person property initial data is null</li>
	 */
	public PersonPropertyEventResolver(PersonPropertyInitialData personPropertyInitialData) {
		if (personPropertyInitialData == null) {
			throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_INITIAL_DATA);
		}
		this.personPropertyInitialData = personPropertyInitialData;
	}

	private PersonPropertyDataManager personPropertyDataManager;

	private void handlePersonCreationObservationEventValidation(final ResolverContext resolverContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonContructionData personContructionData = personCreationObservationEvent.getPersonContructionData();

		List<PersonPropertyInitialization> personPropertyAssignments = personContructionData.getValues(PersonPropertyInitialization.class);
		for (final PersonPropertyInitialization personPropertyAssignment : personPropertyAssignments) {
			PersonPropertyId personPropertyId = personPropertyAssignment.getPersonPropertyId();
			final Object personPropertyValue = personPropertyAssignment.getValue();
			validatePersonPropertyId(resolverContext, personPropertyId);
			validatePersonPropertyValueNotNull(resolverContext, personPropertyValue);
			final PropertyDefinition propertyDefinition = personPropertyDataManager.getPersonPropertyDefinition(personPropertyId);
			validateValueCompatibility(resolverContext, personPropertyId, propertyDefinition, personPropertyValue);
		}

	}

	private void handlePersonCreationObservationEventExecution(final ResolverContext resolverContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		PersonContructionData personContructionData = personCreationObservationEvent.getPersonContructionData();

		List<PersonPropertyInitialization> personPropertyAssignments = personContructionData.getValues(PersonPropertyInitialization.class);
		for (final PersonPropertyInitialization personPropertyAssignment : personPropertyAssignments) {
			personPropertyDataManager.setPersonPropertyValue(personId, personPropertyAssignment.getPersonPropertyId(), personPropertyAssignment.getValue());
		}

	}

	private void handleBulkPersonCreationObservationEventValidation(final ResolverContext resolverContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		for (PersonContructionData personContructionData : personContructionDatas) {
			List<PersonPropertyInitialization> personPropertyAssignments = personContructionData.getValues(PersonPropertyInitialization.class);
			for (final PersonPropertyInitialization personPropertyAssignment : personPropertyAssignments) {
				PersonPropertyId personPropertyId = personPropertyAssignment.getPersonPropertyId();
				final Object personPropertyValue = personPropertyAssignment.getValue();

				validatePersonPropertyId(resolverContext, personPropertyId);
				validatePersonPropertyValueNotNull(resolverContext, personPropertyValue);
				final PropertyDefinition propertyDefinition = personPropertyDataManager.getPersonPropertyDefinition(personPropertyId);
				validateValueCompatibility(resolverContext, personPropertyId, propertyDefinition, personPropertyValue);
			}
		}
	}

	private void handleBulkPersonCreationObservationEventExecution(final ResolverContext resolverContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		PersonId personId = bulkPersonCreationObservationEvent.getPersonId();
		int pId = personId.getValue();
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		for (PersonContructionData personContructionData : personContructionDatas) {
			PersonId boxedPersonId = personDataView.getBoxedPersonId(pId);
			List<PersonPropertyInitialization> personPropertyAssignments = personContructionData.getValues(PersonPropertyInitialization.class);
			for (final PersonPropertyInitialization personPropertyAssignment : personPropertyAssignments) {
				personPropertyDataManager.setPersonPropertyValue(boxedPersonId, personPropertyAssignment.getPersonPropertyId(), personPropertyAssignment.getValue());
			}
			pId++;
		}
	}

	private void handlePersonImminentRemovalObservationEventValidation(final ResolverContext resolverContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		validatePersonExists(resolverContext, personImminentRemovalObservationEvent.getPersonId());
	}

	private void handlePersonImminentRemovalObservationEventExecution(final ResolverContext resolverContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		resolverContext.addPlan((context) -> personPropertyDataManager.handlePersonRemoval(personImminentRemovalObservationEvent.getPersonId()), resolverContext.getTime());
	}

	private PersonDataView personDataView;

	public void init(final ResolverContext resolverContext) {

		CompartmentLocationDataView compartmentLocationDataView = resolverContext.getDataView(CompartmentLocationDataView.class).get();
		RegionLocationDataView regionLocationDataView = resolverContext.getDataView(RegionLocationDataView.class).get();
		
		resolverContext.addEventLabeler(PersonPropertyChangeObservationEvent.getEventLabelerForCompartmentAndProperty(compartmentLocationDataView));
		resolverContext.addEventLabeler(PersonPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty(regionLocationDataView));
		resolverContext.addEventLabeler(PersonPropertyChangeObservationEvent.getEventLabelerForPersonAndProperty());
		resolverContext.addEventLabeler(PersonPropertyChangeObservationEvent.getEventLabelerForProperty());

		personPropertyDataManager = new PersonPropertyDataManager(resolverContext.getSafeContext());
		personDataView = resolverContext.getDataView(PersonDataView.class).get();

		resolverContext.subscribeToEventValidationPhase(PersonPropertyValueAssignmentEvent.class, this::handlePersonPropertyValueAssignmentEventValidation);
		resolverContext.subscribeToEventExecutionPhase(PersonPropertyValueAssignmentEvent.class, this::handlePersonPropertyValueAssignmentEventExecution);

		resolverContext.subscribeToEventValidationPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEventValidation);
		resolverContext.subscribeToEventExecutionPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEventExecution);

		resolverContext.subscribeToEventValidationPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventValidation);
		resolverContext.subscribeToEventExecutionPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventExecution);

		resolverContext.subscribeToEventValidationPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventValidation);
		resolverContext.subscribeToEventExecutionPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventExecution);

		for (final PersonPropertyId personPropertyId : personPropertyInitialData.getPersonPropertyIds()) {
			PropertyDefinition personPropertyDefinition = personPropertyInitialData.getPersonPropertyDefinition(personPropertyId);
			personPropertyDataManager.definePersonProperty(personPropertyId, personPropertyDefinition);
		}

		final Map<PersonId, PersonId> scenarioToSimPeopleMap = personDataView.getScenarioToSimPeopleMap();
		for (final PersonId scenarioPersonId : personPropertyInitialData.getPersonIds()) {
			for (final PersonPropertyId personPropertyId : personPropertyInitialData.getPersonPropertyIds()) {
				final Object personPropertyValue = personPropertyInitialData.getPersonPropertyValue(scenarioPersonId, personPropertyId);
				if (personPropertyValue != null) {
					final PersonId simulationPersonId = scenarioToSimPeopleMap.get(scenarioPersonId);
					if (simulationPersonId == null) {
						throw new ContractException(PersonPropertyError.UNKNOWN_PERSON_ID, scenarioPersonId);
					}
					personPropertyDataManager.setPersonPropertyValue(simulationPersonId, personPropertyId, personPropertyValue);
				}
			}
		}

		resolverContext.publishDataView(new PersonPropertyDataView(resolverContext.getSafeContext(), personPropertyDataManager));
		personPropertyInitialData = null;
	}

	private void handlePersonPropertyValueAssignmentEventValidation(final ResolverContext resolverContext, final PersonPropertyValueAssignmentEvent personPropertyValueAssignmentEvent) {
		final PersonId personId = personPropertyValueAssignmentEvent.getPersonId();
		final PersonPropertyId personPropertyId = personPropertyValueAssignmentEvent.getPersonPropertyId();
		final Object personPropertyValue = personPropertyValueAssignmentEvent.getPersonPropertyValue();
		validatePersonExists(resolverContext, personId);
		validatePersonPropertyId(resolverContext, personPropertyId);
		validatePersonPropertyValueNotNull(resolverContext, personPropertyValue);
		final PropertyDefinition propertyDefinition = personPropertyDataManager.getPersonPropertyDefinition(personPropertyId);
		validateValueCompatibility(resolverContext, personPropertyId, propertyDefinition, personPropertyValue);
		validatePropertyMutability(resolverContext, propertyDefinition);
	}

	private void handlePersonPropertyValueAssignmentEventExecution(final ResolverContext resolverContext, final PersonPropertyValueAssignmentEvent personPropertyValueAssignmentEvent) {
		final PersonId personId = personPropertyValueAssignmentEvent.getPersonId();
		final PersonPropertyId personPropertyId = personPropertyValueAssignmentEvent.getPersonPropertyId();
		final Object personPropertyValue = personPropertyValueAssignmentEvent.getPersonPropertyValue();

		Object oldValue = personPropertyDataManager.getPersonPropertyValue(personId, personPropertyId);
		personPropertyDataManager.setPersonPropertyValue(personId, personPropertyId, personPropertyValue);
		resolverContext.queueEventForResolution(new PersonPropertyChangeObservationEvent(personId, personPropertyId, oldValue, personPropertyValue));
	}

	private void validatePersonExists(final ResolverContext resolverContext, final PersonId personId) {
		if (personId == null) {
			resolverContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			resolverContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private static void validatePersonPropertyValueNotNull(final ResolverContext resolverContext, final Object propertyValue) {
		if (propertyValue == null) {
			resolverContext.throwContractException(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE);
		}
	}

	private static void validatePropertyMutability(final ResolverContext resolverContext, final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			resolverContext.throwContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private static void validateValueCompatibility(final ResolverContext resolverContext, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			resolverContext.throwContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void validatePersonPropertyId(ResolverContext resolverContext, final PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			resolverContext.throwContractException(PersonPropertyError.NULL_PERSON_PROPERTY_ID);
		}
		if (!personPropertyDataManager.personPropertyIdExists(personPropertyId)) {
			resolverContext.throwContractException(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, personPropertyId);
		}
	}

}
