package plugins.regions.resolvers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import nucleus.AgentContext;
import nucleus.ResolverContext;
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

	private void handlePersonImminentRemovalObservationEventValidation(final ResolverContext resolverContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		validatePersonExists(resolverContext, personImminentRemovalObservationEvent.getPersonId());
	}

	private void handlePersonImminentRemovalObservationEventExecution(final ResolverContext resolverContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		resolverContext.addPlan((context) -> regionLocationDataManager.removePerson(personImminentRemovalObservationEvent.getPersonId()), resolverContext.getTime());
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
	public void init(final ResolverContext resolverContext) {
		resolverContext.subscribeToEventExecutionPhase(PopulationGrowthProjectionEvent.class, this::handlePopulationGrowthProjectiontEventExecution);

		resolverContext.subscribeToEventValidationPhase(PersonRegionAssignmentEvent.class, this::handlePersonRegionAssignmentEventValidation);
		resolverContext.subscribeToEventExecutionPhase(PersonRegionAssignmentEvent.class, this::handlePersonRegionAssignmentEventExecution);

		resolverContext.subscribeToEventValidationPhase(RegionPropertyValueAssignmentEvent.class, this::handleRegionPropertyValueAssignmentEventValidation);
		resolverContext.subscribeToEventExecutionPhase(RegionPropertyValueAssignmentEvent.class, this::handleRegionPropertyValueAssignmentEventExecution);

		resolverContext.subscribeToEventValidationPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEventValidation);
		resolverContext.subscribeToEventExecutionPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEventExecution);

		resolverContext.subscribeToEventValidationPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventValidation);
		resolverContext.subscribeToEventExecutionPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventExecution);

		resolverContext.subscribeToEventValidationPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventValidation);
		resolverContext.subscribeToEventExecutionPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventExecution);

		resolverContext.addEventLabeler(RegionPropertyChangeObservationEvent.getEventLabelerForProperty());
		resolverContext.addEventLabeler(RegionPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty());
		resolverContext.addEventLabeler(PersonRegionChangeObservationEvent.getEventLabelerForArrivalRegion());
		resolverContext.addEventLabeler(PersonRegionChangeObservationEvent.getEventLabelerForDepartureRegion());
		resolverContext.addEventLabeler(PersonRegionChangeObservationEvent.getEventLabelerForPerson());

		personDataView = resolverContext.getDataView(PersonDataView.class).get();

		regionDataManager = new RegionDataManager(resolverContext.getSafeContext());
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

		regionLocationDataManager = new RegionLocationDataManager(resolverContext.getSafeContext(), regionInitialData);

		
		loadRegionPropertyValues(resolverContext);

		for (RegionId regionId : regionIds) {
			Consumer<AgentContext> consumer = regionInitialData.getRegionComponentInitialBehavior(regionId);
			resolverContext.queueEventForResolution(new ComponentConstructionEvent(regionId, consumer));
		}

		loadPeople();

		resolverContext.publishDataView(new RegionDataView(resolverContext.getSafeContext(), regionDataManager));
		resolverContext.publishDataView(new RegionLocationDataView(resolverContext, regionLocationDataManager));
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

	private void handlePersonCreationObservationEventValidation(final ResolverContext resolverContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonContructionData personContructionData = personCreationObservationEvent.getPersonContructionData();
		RegionId regionId = personContructionData.getValue(RegionId.class).orElse(null);
		validateRegionId(resolverContext, regionId);
	}

	private void handlePersonCreationObservationEventExecution(final ResolverContext resolverContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		PersonContructionData personContructionData = personCreationObservationEvent.getPersonContructionData();
		RegionId regionId = personContructionData.getValue(RegionId.class).orElse(null);
		regionLocationDataManager.setPersonRegion(personId, regionId);
	}

	private void handleBulkPersonCreationObservationEventValidation(final ResolverContext resolverContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		for (PersonContructionData personContructionData : personContructionDatas) {
			RegionId regionId = personContructionData.getValue(RegionId.class).orElse(null);
			validateRegionId(resolverContext, regionId);
		}
	}

	private void handleBulkPersonCreationObservationEventExecution(final ResolverContext resolverContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
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

	private void validatePersonExists(final ResolverContext resolverContext, final PersonId personId) {
		if (personId == null) {
			resolverContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			resolverContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	/*
	 * Precondition : person and region exist
	 */
	private void validatePersonNotInRegion(final ResolverContext resolverContext, final PersonId personId, final RegionId regionId) {
		final RegionId currentRegionId = regionLocationDataManager.getPersonRegion(personId);
		if (currentRegionId.equals(regionId)) {
			resolverContext.throwContractException(RegionError.SAME_REGION, regionId);
		}
	}

	private void handlePopulationGrowthProjectiontEventExecution(final ResolverContext resolverContext, final PopulationGrowthProjectionEvent populationGrowthProjectionEvent) {
		regionLocationDataManager.expandCapacity(populationGrowthProjectionEvent.getCount());
	}

	private void handlePersonRegionAssignmentEventValidation(final ResolverContext resolverContext, final PersonRegionAssignmentEvent personRegionAssignmentEvent) {
		final PersonId personId = personRegionAssignmentEvent.getPersonId();
		final RegionId regionId = personRegionAssignmentEvent.getRegionId();

		validatePersonExists(resolverContext, personId);
		validateRegionId(resolverContext, regionId);
		validatePersonNotInRegion(resolverContext, personId, regionId);
	}

	private void handlePersonRegionAssignmentEventExecution(final ResolverContext resolverContext, final PersonRegionAssignmentEvent personRegionAssignmentEvent) {
		final PersonId personId = personRegionAssignmentEvent.getPersonId();
		final RegionId regionId = personRegionAssignmentEvent.getRegionId();

		final RegionId oldRegionId = regionLocationDataManager.getPersonRegion(personId);
		regionLocationDataManager.setPersonRegion(personId, regionId);
		resolverContext.queueEventForResolution(new PersonRegionChangeObservationEvent(personId, oldRegionId, regionId));
	}

	private void loadRegionPropertyValues(final ResolverContext resolverContext) {
		for (final RegionId regionId : regionInitialData.getRegionIds()) {
			for (final RegionPropertyId regionPropertyId : regionInitialData.getRegionPropertyIds()) {
				final Object regionPropertyValue = regionInitialData.getRegionPropertyValue(regionId, regionPropertyId);
				if (regionPropertyValue != null) {
					final PropertyDefinition propertyDefinition = regionDataManager.getRegionPropertyDefinition(regionPropertyId);
					validateValueCompatibility(resolverContext, regionPropertyId, propertyDefinition, regionPropertyValue);
					regionDataManager.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
				}
			}
		}
	}

	private void handleRegionPropertyValueAssignmentEventValidation(final ResolverContext resolverContext, final RegionPropertyValueAssignmentEvent regionPropertyValueAssignmentEvent) {
		final RegionId regionId = regionPropertyValueAssignmentEvent.getRegionId();
		final RegionPropertyId regionPropertyId = regionPropertyValueAssignmentEvent.getRegionPropertyId();
		final Object regionPropertyValue = regionPropertyValueAssignmentEvent.getRegionPropertyValue();

		validateRegionId(resolverContext, regionId);
		validateRegionPropertyId(resolverContext, regionPropertyId);
		validateRegionPropertyValueNotNull(resolverContext, regionPropertyValue);
		final PropertyDefinition propertyDefinition = regionDataManager.getRegionPropertyDefinition(regionPropertyId);
		validateValueCompatibility(resolverContext, regionPropertyId, propertyDefinition, regionPropertyValue);
		validatePropertyMutability(resolverContext, propertyDefinition);
	}

	private void handleRegionPropertyValueAssignmentEventExecution(final ResolverContext resolverContext, final RegionPropertyValueAssignmentEvent regionPropertyValueAssignmentEvent) {
		final RegionId regionId = regionPropertyValueAssignmentEvent.getRegionId();
		final RegionPropertyId regionPropertyId = regionPropertyValueAssignmentEvent.getRegionPropertyId();
		final Object regionPropertyValue = regionPropertyValueAssignmentEvent.getRegionPropertyValue();

		final Object previousPropertyValue = regionDataManager.getRegionPropertyValue(regionId, regionPropertyId);
		regionDataManager.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
		resolverContext.queueEventForResolution(new RegionPropertyChangeObservationEvent(regionId, regionPropertyId, previousPropertyValue, regionPropertyValue));
	}

	private void validatePropertyMutability(final ResolverContext resolverContext, final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			resolverContext.throwContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void validateRegionId(final ResolverContext resolverContext, final RegionId regionId) {

		if (regionId == null) {
			resolverContext.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionIds.contains(regionId)) {
			resolverContext.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private void validateRegionPropertyId(final ResolverContext resolverContext, final RegionPropertyId regionPropertyId) {
		if (regionPropertyId == null) {
			resolverContext.throwContractException(RegionError.NULL_REGION_PROPERTY_ID);
		}
		if (!regionPropertyIds.contains(regionPropertyId)) {
			resolverContext.throwContractException(RegionError.UNKNOWN_REGION_PROPERTY_ID, regionPropertyId);
		}
	}

	private void validateRegionPropertyValueNotNull(final ResolverContext resolverContext, final Object propertyValue) {
		if (propertyValue == null) {
			resolverContext.throwContractException(RegionError.NULL_REGION_PROPERTY_VALUE);
		}
	}

	private void validateValueCompatibility(final ResolverContext resolverContext, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			resolverContext.throwContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

}
