package plugins.regions.resolvers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import nucleus.AgentContext;
import nucleus.DataManagerContext;
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
import plugins.regions.RegionPlugin;
import plugins.regions.datacontainers.RegionDataManager;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataManager;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.mutation.PersonRegionAssignmentEvent;
import plugins.regions.events.mutation.RegionPropertyValueAssignmentEvent;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import plugins.regions.events.observation.RegionPropertyChangeObservationEvent;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import util.ContractException;

/**
 * 
 * Provides event resolution for the {@linkplain RegionPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain RegionDataView} and the
 * {@linkplain RegionLocationDataView}. Initializes both data views from
 * the {@linkplain RegionInitialData} instance provided to the plugin.
 * </P>
 * <P>
 * Creates all region agents upon initialization.
 * </P>
 * 
 * <P>
 * Initializes all event labelers defined by
 * {@linkplain RegionPropertyChangeObservationEvent} and
 * {@linkplain PersonRegionChangeObservationEvent}
 * </P>
 * 
 * 
 * <P>
 * Resolves the following events:
 * <ul>
 * 
 * <li>{@linkplain PopulationGrowthProjectionEvent} <blockquote>Increases memory
 * allocation within the {@linkplain RegionLocationDataManager} to allow
 * for more efficient bulk person addition.</blockquote></li>
 * 
 * <li>{@linkplain PersonRegionAssignmentEvent} <blockquote> Updates the
 * person's current region and regino arrival time in the
 * {@linkplain RegionLocationDataView}. Generates a corresponding
 * {@linkplain PersonRegionChangeObservationEvent} <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@link PersonError#NULL_PERSON_ID} if the person id is null
 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is unknown
 * <li>{@link RegionError#NULL_REGION_ID} if the region id is
 * null
 * <li>{@link RegionError#UNKNOWN_REGION_ID} if the region id is
 * unknown
 * <li>{@link RegionError#SAME_REGION} if the region id is
 * currently assigned to the person
 * </ul>
 * </blockquote></li>
 * 
 * 
 * <li>{@linkplain RegionPropertyValueAssignmentEvent} <blockquote>Updates
 * the region's property value and time in the
 * {@linkplain RegionDataView} and generates a corresponding
 * {@linkplain RegionPropertyChangeObservationEvent}
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <li>{@link RegionError#NULL_REGION_ID} if the region id is
 * null
 * <li>{@link RegionError#UNKNOWN_REGION_ID} if the region id is
 * unknown
 * <li>{@link RegionError#NULL_REGION_PROPERTY_ID} if the property id
 * is null
 * <li>{@link RegionError#UNKNOWN_REGION_PROPERTY_ID} if the property
 * id is unknown
 * <li>{@link RegionError#NULL_REGION_PROPERTY_VALUE} if the value is
 * null
 * <li>{@link PropertyError#INCOMPATIBLE_VALUE} if the value is incompatible
 * with the defined type for the property
 * <li>{@link PropertyError#IMMUTABLE_VALUE} if the property has been defined as
 * immutable
 * 
 * </blockquote></li>
 * 
 * <li>{@linkplain PersonCreationObservationEvent}<blockquote> Sets the person's
 * initial region in the {@linkplain RegionLocationDataView} from the
 * region reference in the auxiliary data of the event.
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain RegionError#NULL_REGION_ID} if no region data
 * was included in the event</li>
 * 
 * <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
 * in the event is unknown</li>
 * </ul>
 * 
 * 
 * </blockquote></li>
 * 
 * <li>{@linkplain BulkPersonCreationObservationEvent}<blockquote> Sets each
 * person's initial region in the {@linkplain RegionLocationDataView}
 * from the region references in the auxiliary data of the event.
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain RegionError#NULL_REGION_ID} if no region data
 * was included in the for some person in event</li>
 * 
 * <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
 * is unknown for some person in the event</li>
 * </ul>
 * 
 * </blockquote></li>
 * 
 * <li>{@linkplain PersonImminentRemovalObservationEvent}<blockquote> Removes
 * the region assignment data for the person from the
 * {@linkplain RegionDataView} <BR>
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
public final class RegionEventResolver {
	
	private RegionDataManager regionDataManager;
	
	private RegionLocationDataManager regionLocationDataManager;
	
	private RegionInitialData regionInitialData;

	public RegionEventResolver(RegionInitialData regionInitialData) {
		this.regionInitialData = regionInitialData;
	}

	private Set<RegionId> regionIds;

	private Set<RegionPropertyId> regionPropertyIds;

	private void handlePersonImminentRemovalObservationEventValidation(final DataManagerContext dataManagerContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		validatePersonExists(dataManagerContext, personImminentRemovalObservationEvent.getPersonId());
	}

	private void handlePersonImminentRemovalObservationEventExecution(final DataManagerContext dataManagerContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		dataManagerContext.addPlan((context) -> regionLocationDataManager.removePerson(personImminentRemovalObservationEvent.getPersonId()), dataManagerContext.getTime());
	}

	private PersonDataView personDataView;
	/**
	 * Initial behavior of this resolver.
	 * <li>Adds all event labelers defined by the following events <blockquote>
	 * <li>{@linkplain RegionPropertyChangeObservationEvent}</li>
	 * <li>{@linkplain PersonRegionChangeObservationEvent}</li>
	 * </blockquote></li>
	 * 
	 *
	 * <li>Subscribes to all handled events
	 * 
	 * <li>Establishes person region assignments from the
	 * {@linkplain RegionInitialData}</li>
	 * 
	 * <li>Sets region property values from the
	 * {@linkplain RegionInitialData}</li>
	 * 
	 * <li>Publishes the {@linkplain RegionDataView}</li>
	 * 
	 * <li>Publishes the {@linkplain RegionLocationDataView}</li>
	 */
	public void init(final DataManagerContext dataManagerContext) {
		dataManagerContext.subscribeToEventExecutionPhase(PopulationGrowthProjectionEvent.class, this::handlePopulationGrowthProjectiontEventExecution);

		dataManagerContext.subscribeToEventValidationPhase(PersonRegionAssignmentEvent.class, this::handlePersonRegionAssignmentEventValidation);
		dataManagerContext.subscribeToEventExecutionPhase(PersonRegionAssignmentEvent.class, this::handlePersonRegionAssignmentEventExecution);

		dataManagerContext.subscribeToEventValidationPhase(RegionPropertyValueAssignmentEvent.class, this::handleRegionPropertyValueAssignmentEventValidation);
		dataManagerContext.subscribeToEventExecutionPhase(RegionPropertyValueAssignmentEvent.class, this::handleRegionPropertyValueAssignmentEventExecution);

		dataManagerContext.subscribeToEventValidationPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEventValidation);
		dataManagerContext.subscribeToEventExecutionPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEventExecution);

		dataManagerContext.subscribeToEventValidationPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventValidation);
		dataManagerContext.subscribeToEventExecutionPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventExecution);

		dataManagerContext.subscribeToEventValidationPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventValidation);
		dataManagerContext.subscribeToEventExecutionPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventExecution);

		dataManagerContext.addEventLabeler(RegionPropertyChangeObservationEvent.getEventLabelerForProperty());
		dataManagerContext.addEventLabeler(RegionPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty());
		dataManagerContext.addEventLabeler(PersonRegionChangeObservationEvent.getEventLabelerForArrivalRegion());
		dataManagerContext.addEventLabeler(PersonRegionChangeObservationEvent.getEventLabelerForDepartureRegion());
		dataManagerContext.addEventLabeler(PersonRegionChangeObservationEvent.getEventLabelerForPerson());

		personDataView = dataManagerContext.getDataView(PersonDataView.class).get();

		regionDataManager = new RegionDataManager(dataManagerContext.getSafeContext());
		regionIds = regionInitialData.getRegionIds();
		regionPropertyIds = regionInitialData.getRegionPropertyIds();
		for(RegionId regionId : regionIds) {
			regionDataManager.addRegionId(regionId);
		}
		
		for(RegionPropertyId regionPropertyId : regionPropertyIds) {
			PropertyDefinition regionPropertyDefinition = regionInitialData.getRegionPropertyDefinition(regionPropertyId);
			regionDataManager.addRegionPropertyDefinition(regionPropertyId, regionPropertyDefinition);
		}
		
		for (final RegionId regionId : regionIds) {
			for(RegionPropertyId regionPropertyId : regionPropertyIds) {
				final Object regionPropertyValue = regionInitialData.getRegionPropertyValue(regionId, regionPropertyId);
				regionDataManager.setRegionPropertyValue(regionId, regionPropertyId,regionPropertyValue);
			}
		}

		regionLocationDataManager = new RegionLocationDataManager(dataManagerContext.getSafeContext(), regionInitialData);

		
		loadRegionPropertyValues(dataManagerContext);

		for (RegionId regionId : regionIds) {
			Consumer<AgentContext> consumer = regionInitialData.getRegionComponentInitialBehavior(regionId);
			dataManagerContext.resolveEvent(new ComponentConstructionEvent(regionId, consumer));
		}

		loadPeople();

		dataManagerContext.publishDataView(new RegionDataView(dataManagerContext.getSafeContext(), regionDataManager));
		dataManagerContext.publishDataView(new RegionLocationDataView(dataManagerContext, regionLocationDataManager));
		regionInitialData = null;
	}

	private void loadPeople() {
		final Map<PersonId, PersonId> scenarioToSimPeopleMap = personDataView.getScenarioToSimPeopleMap();

		/*
		 * Show that every person contained in the region initial data exists in
		 * the person data view
		 */
		Set<PersonId> regionallyDefinedPeople = regionInitialData.getPersonIds();
		for (PersonId regionPersonId : regionallyDefinedPeople) {
			if (!scenarioToSimPeopleMap.containsKey(regionPersonId)) {
				throw new ContractException(PersonError.UNKNOWN_PERSON_ID, regionPersonId + " in regional initial data");
			}
		}

		/*
		 * Show that every person in the person data view has a region
		 * assignment in the region initial data
		 */
		for (PersonId scenarioPersonID : scenarioToSimPeopleMap.keySet()) {
			if (!regionallyDefinedPeople.contains(scenarioPersonID)) {
				throw new ContractException(RegionError.MISSING_REGION_ASSIGNMENT, scenarioPersonID);
			}
		}

		/*
		 * Record the assignments
		 */
		for (final PersonId scenarioPersonId : regionallyDefinedPeople) {
			final RegionId regionId = regionInitialData.getPersonRegion(scenarioPersonId);
			final PersonId personId = scenarioToSimPeopleMap.get(scenarioPersonId);
			regionLocationDataManager.setPersonRegion(personId, regionId);
		}
	}

	private void handlePersonCreationObservationEventValidation(final DataManagerContext dataManagerContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonContructionData personContructionData = personCreationObservationEvent.getPersonContructionData();
		RegionId regionId = personContructionData.getValue(RegionId.class).orElse(null);
		validateRegionId(dataManagerContext, regionId);
	}

	private void handlePersonCreationObservationEventExecution(final DataManagerContext dataManagerContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		PersonContructionData personContructionData = personCreationObservationEvent.getPersonContructionData();
		RegionId regionId = personContructionData.getValue(RegionId.class).orElse(null);
		regionLocationDataManager.setPersonRegion(personId, regionId);
	}

	private void handleBulkPersonCreationObservationEventValidation(final DataManagerContext dataManagerContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		for (PersonContructionData personContructionData : personContructionDatas) {
			RegionId regionId = personContructionData.getValue(RegionId.class).orElse(null);
			validateRegionId(dataManagerContext, regionId);
		}
	}

	private void handleBulkPersonCreationObservationEventExecution(final DataManagerContext dataManagerContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		PersonId personId = bulkPersonCreationObservationEvent.getPersonId();
		int pId = personId.getValue();
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		for (PersonContructionData personContructionData : personContructionDatas) {
			RegionId regionId = personContructionData.getValue(RegionId.class).get();
			PersonId boxedPersonId = personDataView.getBoxedPersonId(pId);
			regionLocationDataManager.setPersonRegion(boxedPersonId, regionId);
			pId++;
		}
	}

	private void validatePersonExists(final DataManagerContext dataManagerContext, final PersonId personId) {
		if (personId == null) {
			dataManagerContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			dataManagerContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	/*
	 * Precondition : person and region exist
	 */
	private void validatePersonNotInRegion(final DataManagerContext dataManagerContext, final PersonId personId, final RegionId regionId) {
		final RegionId currentRegionId = regionLocationDataManager.getPersonRegion(personId);
		if (currentRegionId.equals(regionId)) {
			dataManagerContext.throwContractException(RegionError.SAME_REGION, regionId);
		}
	}

	private void handlePopulationGrowthProjectiontEventExecution(final DataManagerContext dataManagerContext, final PopulationGrowthProjectionEvent populationGrowthProjectionEvent) {
		regionLocationDataManager.expandCapacity(populationGrowthProjectionEvent.getCount());
	}

	private void handlePersonRegionAssignmentEventValidation(final DataManagerContext dataManagerContext, final PersonRegionAssignmentEvent personRegionAssignmentEvent) {
		final PersonId personId = personRegionAssignmentEvent.getPersonId();
		final RegionId regionId = personRegionAssignmentEvent.getRegionId();

		validatePersonExists(dataManagerContext, personId);
		validateRegionId(dataManagerContext, regionId);
		validatePersonNotInRegion(dataManagerContext, personId, regionId);
	}

	private void handlePersonRegionAssignmentEventExecution(final DataManagerContext dataManagerContext, final PersonRegionAssignmentEvent personRegionAssignmentEvent) {
		final PersonId personId = personRegionAssignmentEvent.getPersonId();
		final RegionId regionId = personRegionAssignmentEvent.getRegionId();

		final RegionId oldRegionId = regionLocationDataManager.getPersonRegion(personId);
		regionLocationDataManager.setPersonRegion(personId, regionId);
		dataManagerContext.resolveEvent(new PersonRegionChangeObservationEvent(personId, oldRegionId, regionId));
	}

	private void loadRegionPropertyValues(final DataManagerContext dataManagerContext) {
		for (final RegionId regionId : regionInitialData.getRegionIds()) {
			for (final RegionPropertyId regionPropertyId : regionInitialData.getRegionPropertyIds()) {
				final Object regionPropertyValue = regionInitialData.getRegionPropertyValue(regionId, regionPropertyId);
				if (regionPropertyValue != null) {
					final PropertyDefinition propertyDefinition = regionDataManager.getRegionPropertyDefinition(regionPropertyId);
					validateValueCompatibility(dataManagerContext, regionPropertyId, propertyDefinition, regionPropertyValue);
					regionDataManager.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
				}
			}
		}
	}

	private void handleRegionPropertyValueAssignmentEventValidation(final DataManagerContext dataManagerContext, final RegionPropertyValueAssignmentEvent regionPropertyValueAssignmentEvent) {
		final RegionId regionId = regionPropertyValueAssignmentEvent.getRegionId();
		final RegionPropertyId regionPropertyId = regionPropertyValueAssignmentEvent.getRegionPropertyId();
		final Object regionPropertyValue = regionPropertyValueAssignmentEvent.getRegionPropertyValue();

		validateRegionId(dataManagerContext, regionId);
		validateRegionPropertyId(dataManagerContext, regionPropertyId);
		validateRegionPropertyValueNotNull(dataManagerContext, regionPropertyValue);
		final PropertyDefinition propertyDefinition = regionDataManager.getRegionPropertyDefinition(regionPropertyId);
		validateValueCompatibility(dataManagerContext, regionPropertyId, propertyDefinition, regionPropertyValue);
		validatePropertyMutability(dataManagerContext, propertyDefinition);
	}

	private void handleRegionPropertyValueAssignmentEventExecution(final DataManagerContext dataManagerContext, final RegionPropertyValueAssignmentEvent regionPropertyValueAssignmentEvent) {
		final RegionId regionId = regionPropertyValueAssignmentEvent.getRegionId();
		final RegionPropertyId regionPropertyId = regionPropertyValueAssignmentEvent.getRegionPropertyId();
		final Object regionPropertyValue = regionPropertyValueAssignmentEvent.getRegionPropertyValue();

		final Object previousPropertyValue = regionDataManager.getRegionPropertyValue(regionId, regionPropertyId);
		regionDataManager.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
		dataManagerContext.resolveEvent(new RegionPropertyChangeObservationEvent(regionId, regionPropertyId, previousPropertyValue, regionPropertyValue));
	}

	private void validatePropertyMutability(final DataManagerContext dataManagerContext, final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			dataManagerContext.throwContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void validateRegionId(final DataManagerContext dataManagerContext, final RegionId regionId) {

		if (regionId == null) {
			dataManagerContext.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionIds.contains(regionId)) {
			dataManagerContext.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private void validateRegionPropertyId(final DataManagerContext dataManagerContext, final RegionPropertyId regionPropertyId) {
		if (regionPropertyId == null) {
			dataManagerContext.throwContractException(RegionError.NULL_REGION_PROPERTY_ID);
		}
		if (!regionPropertyIds.contains(regionPropertyId)) {
			dataManagerContext.throwContractException(RegionError.UNKNOWN_REGION_PROPERTY_ID, regionPropertyId);
		}
	}

	private void validateRegionPropertyValueNotNull(final DataManagerContext dataManagerContext, final Object propertyValue) {
		if (propertyValue == null) {
			dataManagerContext.throwContractException(RegionError.NULL_REGION_PROPERTY_VALUE);
		}
	}

	private void validateValueCompatibility(final DataManagerContext dataManagerContext, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			dataManagerContext.throwContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

}
