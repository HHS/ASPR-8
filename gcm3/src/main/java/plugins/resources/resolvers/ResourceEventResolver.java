package plugins.resources.resolvers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nucleus.ResolverContext;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
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
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.ResourcesPlugin;
import plugins.resources.datacontainers.ResourceDataManager;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.mutation.InterRegionalResourceTransferEvent;
import plugins.resources.events.mutation.PersonResourceRemovalEvent;
import plugins.resources.events.mutation.RegionResourceAdditionEvent;
import plugins.resources.events.mutation.RegionResourceRemovalEvent;
import plugins.resources.events.mutation.ResourcePropertyValueAssignmentEvent;
import plugins.resources.events.mutation.ResourceTransferFromPersonEvent;
import plugins.resources.events.mutation.ResourceTransferToPersonEvent;
import plugins.resources.events.observation.PersonResourceChangeObservationEvent;
import plugins.resources.events.observation.RegionResourceChangeObservationEvent;
import plugins.resources.events.observation.ResourcePropertyChangeObservationEvent;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.resources.support.ResourcePropertyId;
import util.ContractException;

/**
 * 
 * Provides event resolution for the {@linkplain ResourcesPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain ResourceDataView}.
 * Initializes the data view from the {@linkplain ResourceInitialData} instance
 * provided to the plugin.
 * </P>
 * 
 * 
 * <P>
 * Initializes all event labelers defined by
 * {@linkplain PersonResourceChangeObservationEvent},
 * {@linkplain RegionResourceChangeObservationEvent} and
 * {@linkplain ResourcePropertyChangeObservationEvent}
 * </P>
 * 
 * 
 * <P>
 * Resolves the following events:
 * 
 * <ul>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain PopulationGrowthProjectionEvent} <blockquote>Increases memory
 * allocation within the {@linkplain ResourceDataManager} to allow for more
 * efficient bulk person addition.</blockquote></li>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain PersonCreationObservationEvent}<blockquote> Sets the person's
 * initial resource levels in the {@linkplain ResourceDataView} from the
 * ResourceInitialization references in the auxiliary data of the event.
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the auxiliary data
 * contains a ResourceInitialization that has a null resource id</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the auxiliary data
 * contains a ResourceInitialization that has an unknown resource id</li>
 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the auxiliary data
 * contains a ResourceInitialization that has a negative resource level</li>
 * </ul>
 * 
 * 
 * </blockquote></li>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain BulkPersonCreationObservationEvent}<blockquote> Sets each
 * person's initial resource levels in the {@linkplain ResourceDataView} from
 * the ResourceInitialization references in the auxiliary data of the event.
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the auxiliary data
 * contains a ResourceInitialization that has a null resource id</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the auxiliary data
 * contains a ResourceInitialization that has an unknown resource id</li>
 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the auxiliary data
 * contains a ResourceInitialization that has a negative resource level</li>
 * </ul>
 * 
 * </blockquote></li>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain PersonImminentRemovalObservationEvent}<blockquote> Removes
 * the resource assignment data for the person from the
 * {@linkplain ResourceDataView} by scheduling the removal for the current time.
 * This allows the person and their resource levels to remain long enough for
 * resolvers, agents and reports to have final reference to the person while
 * still associated with any relevant resources. <BR>
 * <BR>
 * Throws {@linkplain ContractException}
 * <ul>
 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
 * unknown</li>
 * </ul>
 * 
 * </blockquote></li>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain InterRegionalResourceTransferEvent}<blockquote> Transfers
 * resources in the {@linkplain ResourceDataView} from one region to another.
 * Generates the corresponding {@linkplain RegionResourceChangeObservationEvent}
 * events for each region. <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain RegionError#NULL_REGION_ID} if the source region is null</li>
 * <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the source region is
 * unknown</li>
 * <li>{@linkplain RegionError#NULL_REGION_ID} if the destination region is
 * null</li>
 * <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the destination region is
 * unknown</li>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is
 * null</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the resource id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the resource
 * amount is negative</li>
 * <li>{@linkplain ResourceError#REFLEXIVE_RESOURCE_TRANSFER} if the source and
 * destination region are equal</li>
 * <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE} if the source
 * region does not have sufficient resources to support the transfer</li>
 * <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION} if the transfer
 * will cause a numeric overflow in the destination region</li>
 * 
 * </ul>
 * </blockquote></li>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain PersonResourceRemovalEvent}<blockquote> Expends an amount of
 * resource from a person and updates the {@linkplain ResourceDataView}
 * Generates the corresponding {@linkplain PersonResourceChangeObservationEvent}
 * event<BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person does not
 * exist</li>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is
 * null</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the resource id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the amount is
 * negative</li>
 * <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE} if the person
 * does not have the required amount of the resource</li>
 * </ul>
 * 
 * 
 * </blockquote></li>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain RegionResourceAdditionEvent}<blockquote> Adds an amount of
 * resource to a region and updates the {@linkplain ResourceDataView} Generates
 * the corresponding {@linkplain RegionResourceChangeObservationEvent} event<BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain RegionError#NULL_REGION_ID} if the region id is null</li>
 * <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is
 * null</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the resource id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the amount is
 * negative</li>
 * <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION} if the addition
 * results in an overflow</li>
 * 
 * </ul>
 * </blockquote></li>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain ResourceTransferToPersonEvent}<blockquote> Transfers an
 * amount of resource to a person from the person's current region and updates
 * the {@linkplain ResourceDataView} Generates the corresponding
 * {@linkplain RegionResourceChangeObservationEvent} and
 * {@linkplain PersonResourceChangeObservationEvent} events <BR>
 * 
 * 
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person does not
 * exist</li>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is
 * null</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the resource id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the amount is
 * negative</li>
 * <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE} if the region
 * does not have the required amount of the resource</li>
 * <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION} if the transfer
 * results in an overflow of the person's resource level</li>
 * </ul>
 * </blockquote></li>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain RegionResourceRemovalEvent}<blockquote> Removes an amount of
 * resource from a region and updates the {@linkplain ResourceDataView}
 * Generates the corresponding {@linkplain RegionResourceChangeObservationEvent}
 * event<BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain RegionError#NULL_REGION_ID} if the region id is null</li>
 * <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is
 * null</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the resource id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the amount is
 * negative</li>
 * <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE} if the region
 * does not have the required amount of the resource</li>
 * </ul>
 * </blockquote></li>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain ResourcePropertyValueAssignmentEvent}<blockquote> Assigns a
 * value to a resource property and updates the {@linkplain ResourceDataView}
 * Generates the corresponding
 * {@linkplain ResourcePropertyChangeObservationEvent} event <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is
 * null</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the resource id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_ID} if the resource
 * property id is null</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_PROPERTY_ID} if the resource
 * property id is unknown</li>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_PROPERTY_VALUE} if the resource
 * property value is null</li>
 * <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the resource property
 * value is incompatible with the corresponding property definition</li>
 * <li>{@linkplain PropertyError#IMMUTABLE_VALUE} if the property has been
 * defined as immutable</li>
 * </ul>
 * </blockquote></li>
 * -------------------------------------------------------------------------------
 * <li>{@linkplain ResourceTransferFromPersonEvent}<blockquote> Transfers an
 * amount of resource from a person to the person's current region and updates
 * the {@linkplain ResourceDataView} Generates the corresponding
 * {@linkplain RegionResourceChangeObservationEvent} and
 * {@linkplain PersonResourceChangeObservationEvent} events <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id is null</li>
 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person does not
 * exist</li>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is
 * null</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the resource id is
 * unknown</li>
 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the amount is
 * negative</li>
 * <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE} if the person
 * does not have the required amount of the resource</li>
 * <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION} if the transfer
 * results in an overflow of the region's resource level</li>
 * </ul>
 * </blockquote></li>
 * -------------------------------------------------------------------------------
 * 
 * <ul>
 * </p>
 * 
 * 
 * @author Shawn Hatch
 *
 */

public final class ResourceEventResolver {

	private ResourceInitialData resourceInitialData;

	/**
	 * Constructs this resolver
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_INITIAL_DATA} if
	 *             the resource initial data is null</li>
	 */
	public ResourceEventResolver(ResourceInitialData resourceInitialData) {
		if (resourceInitialData == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_INITIAL_DATA);
		}
		this.resourceInitialData = resourceInitialData;
	}

	private ResourceDataManager resourceDataManager;
	private RegionLocationDataView regionLocationDataView;
	private PersonDataView personDataView;
	private Map<ResourceId, Set<ResourcePropertyId>> resourcePropertyIdsMap;

	private Set<RegionId> regionIds;

	private void handleRegionResourceAdditionEventValidation(final ResolverContext resolverContext, final RegionResourceAdditionEvent regionResourceAdditionEvent) {

		final long amount = regionResourceAdditionEvent.getAmount();
		final RegionId regionId = regionResourceAdditionEvent.getRegionId();
		final ResourceId resourceId = regionResourceAdditionEvent.getResourceId();
		validateRegionId(resolverContext, regionId);
		validateResourceId(resolverContext, resourceId);
		validateNonnegativeResourceAmount(resolverContext, amount);
		final long previousResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
		validateResourceAdditionValue(resolverContext, previousResourceLevel, amount);

	}

	private void handleRegionResourceAdditionEventExecution(final ResolverContext resolverContext, final RegionResourceAdditionEvent regionResourceAdditionEvent) {

		final long amount = regionResourceAdditionEvent.getAmount();
		final RegionId regionId = regionResourceAdditionEvent.getRegionId();
		final ResourceId resourceId = regionResourceAdditionEvent.getResourceId();
		final long previousResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
		long currentResourceLevel;

		resourceDataManager.incrementRegionResourceLevel(regionId, resourceId, amount);
		currentResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
		resolverContext.queueEventForResolution(new RegionResourceChangeObservationEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel));

	}

	/**
	 * Initial behavior of this resolver.
	 *
	 * <ul>
	 * <li>Adds all event labelers defined by the following events <blockquote>
	 * <ul>
	 * <li>{@linkplain PersonResourceChangeObservationEvent}</li>
	 * <li>{@linkplain RegionResourceChangeObservationEvent}</li>
	 * <li>{@linkplain ResourcePropertyChangeObservationEvent}</li>
	 * </ul>
	 * </blockquote></li>
	 * 
	 *
	 * <li>Subscribes to all handled events
	 * 
	 * 
	 * <li>Sets resource property values from the
	 * {@linkplain ResourceInitialData}</li>
	 * 
	 * <li>Sets region resource levels from the
	 * {@linkplain ResourceInitialData}</li>
	 * 
	 * <li>Sets person resource levels from the
	 * {@linkplain ResourceInitialData}</li>
	 * 
	 * <li>Publishes the {@linkplain ResourceDataView}</li>
	 * 
	 * </ul>
	 */
	public void init(final ResolverContext resolverContext) {

		resolverContext.subscribeToEventExecutionPhase(PopulationGrowthProjectionEvent.class, this::handlePopulationGrowthProjectiontEventExecution);

		resolverContext.subscribeToEventValidationPhase(InterRegionalResourceTransferEvent.class, this::handleInterRegionalResourceTransferEventValidation);
		resolverContext.subscribeToEventExecutionPhase(InterRegionalResourceTransferEvent.class, this::handleInterRegionalResourceTransferEventExecution);

		resolverContext.subscribeToEventValidationPhase(PersonResourceRemovalEvent.class, this::handlePersonResourceRemovalEventValidation);
		resolverContext.subscribeToEventExecutionPhase(PersonResourceRemovalEvent.class, this::handlePersonResourceRemovalEventExecution);

		resolverContext.subscribeToEventValidationPhase(RegionResourceAdditionEvent.class, this::handleRegionResourceAdditionEventValidation);
		resolverContext.subscribeToEventExecutionPhase(RegionResourceAdditionEvent.class, this::handleRegionResourceAdditionEventExecution);

		resolverContext.subscribeToEventValidationPhase(RegionResourceRemovalEvent.class, this::handleRegionResourceRemovalEventValidation);
		resolverContext.subscribeToEventExecutionPhase(RegionResourceRemovalEvent.class, this::handleRegionResourceRemovalEventExecution);

		resolverContext.subscribeToEventValidationPhase(ResourcePropertyValueAssignmentEvent.class, this::handleResourcePropertyValueAssignmentEventValidation);
		resolverContext.subscribeToEventExecutionPhase(ResourcePropertyValueAssignmentEvent.class, this::handleResourcePropertyValueAssignmentEventExecution);

		resolverContext.subscribeToEventValidationPhase(ResourceTransferFromPersonEvent.class, this::handleResourceTransferFromPersonEventValidation);
		resolverContext.subscribeToEventExecutionPhase(ResourceTransferFromPersonEvent.class, this::handleResourceTransferFromPersonEventExecution);

		resolverContext.subscribeToEventValidationPhase(ResourceTransferToPersonEvent.class, this::handleResourceTransferToPersonEventValidation);
		resolverContext.subscribeToEventExecutionPhase(ResourceTransferToPersonEvent.class, this::handleResourceTransferToPersonEventExecution);

		resolverContext.subscribeToEventValidationPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEventValidation);
		resolverContext.subscribeToEventExecutionPhase(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEventExecution);

		resolverContext.subscribeToEventValidationPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventValidation);
		resolverContext.subscribeToEventExecutionPhase(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEventExecution);

		resolverContext.subscribeToEventValidationPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventValidation);
		resolverContext.subscribeToEventExecutionPhase(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEventExecution);

		/*
		 * Establish all the convenience references
		 */
		CompartmentLocationDataView compartmentLocationDataView = resolverContext.getDataView(CompartmentLocationDataView.class).get();
		regionLocationDataView = resolverContext.getDataView(RegionLocationDataView.class).get();

		resolverContext.addEventLabeler(PersonResourceChangeObservationEvent.getEventLabelerForCompartmentAndResource(compartmentLocationDataView));
		resolverContext.addEventLabeler(PersonResourceChangeObservationEvent.getEventLabelerForRegionAndResource(regionLocationDataView));
		resolverContext.addEventLabeler(PersonResourceChangeObservationEvent.getEventLabelerForPersonAndResource());
		resolverContext.addEventLabeler(PersonResourceChangeObservationEvent.getEventLabelerForResource());

		resolverContext.addEventLabeler(ResourcePropertyChangeObservationEvent.getEventLabeler());

		resolverContext.addEventLabeler(RegionResourceChangeObservationEvent.getEventLabelerForRegionAndResource());

		RegionDataView regionDataView = resolverContext.getDataView(RegionDataView.class).get();

		personDataView = resolverContext.getDataView(PersonDataView.class).get();

		regionIds = regionDataView.getRegionIds();

		resourcePropertyIdsMap = new LinkedHashMap<>();
		for (final ResourceId resourceId : resourceInitialData.getResourceIds()) {
			final Set<ResourcePropertyId> resourcePropertyIds = resourceInitialData.getResourcePropertyIds(resourceId);
			resourcePropertyIdsMap.put(resourceId, resourcePropertyIds);
		}

		/*
		 * Load the remaining data from the scenario that generally corresponds
		 * to mutations available to components so that reporting will properly
		 * reflect these data. The adding of resources directly to people and
		 * material producers is covered here but do not correspond to mutations
		 * allowed to components.
		 */
		resourceDataManager = new ResourceDataManager(resolverContext.getSafeContext());

		for (ResourceId resourceId : resourceInitialData.getResourceIds()) {
			TimeTrackingPolicy personResourceTimeTrackingPolicy = resourceInitialData.getPersonResourceTimeTrackingPolicy(resourceId);
			resourceDataManager.addResource(resourceId, personResourceTimeTrackingPolicy);
			Set<ResourcePropertyId> resourcePropertyIds = resourceInitialData.getResourcePropertyIds(resourceId);
			for (ResourcePropertyId resourcePropertyId : resourcePropertyIds) {
				PropertyDefinition propertyDefinition = resourceInitialData.getResourcePropertyDefinition(resourceId, resourcePropertyId);
				Object resourcePropertyValue = resourceInitialData.getResourcePropertyValue(resourceId, resourcePropertyId);
				resourceDataManager.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition, resourcePropertyValue);
			}
		}
		for (RegionId regionId : regionIds) {
			resourceDataManager.addRegion(regionId);
		}

		for (final RegionId regionId : resourceInitialData.getRegionIds()) {
			if (!regionIds.contains(regionId)) {
				throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId + " is an unknown region with initial resources");
			}
			for (final ResourceId resourceId : resourceInitialData.getResourceIds()) {
				final Long amount = resourceInitialData.getRegionResourceLevel(regionId, resourceId);
				if (amount != null) {
					resourceDataManager.incrementRegionResourceLevel(regionId, resourceId, amount);
				}
			}
		}

		final Map<PersonId, PersonId> scenarioToSimPeopleMap = personDataView.getScenarioToSimPeopleMap();
		Set<PersonId> initialDataPersonIds = resourceInitialData.getPersonIds();
		for (PersonId personId : initialDataPersonIds) {
			if (!scenarioToSimPeopleMap.containsKey(personId)) {
				throw new ContractException(PersonError.UNKNOWN_PERSON_ID, personId);
			}
		}
		Set<ResourceId> resourceIds = resourceInitialData.getResourceIds();

		for (final PersonId scenarioPersonId : initialDataPersonIds) {
			for (final ResourceId resourceId : resourceIds) {
				final Long amount = resourceInitialData.getPersonResourceLevel(scenarioPersonId, resourceId);
				if (amount > 0) {
					final PersonId simulationPersonId = scenarioToSimPeopleMap.get(scenarioPersonId);
					resourceDataManager.incrementPersonResourceLevel(resourceId, simulationPersonId, amount);
				}
			}
		}

		resolverContext.publishDataView(new ResourceDataView(resolverContext.getSafeContext(), resourceDataManager));
		resourceInitialData = null;
	}

	private void handlePersonResourceRemovalEventValidation(final ResolverContext resolverContext, final PersonResourceRemovalEvent personResourceRemovalEvent) {
		final long amount = personResourceRemovalEvent.getAmount();
		final PersonId personId = personResourceRemovalEvent.getPersonId();
		final ResourceId resourceId = personResourceRemovalEvent.getResourceId();
		validatePersonExists(resolverContext, personId);
		validateResourceId(resolverContext, resourceId);
		validateNonnegativeResourceAmount(resolverContext, amount);
		validatePersonHasSufficientResources(resolverContext, resourceId, personId, amount);
	}

	private void handlePersonResourceRemovalEventExecution(final ResolverContext resolverContext, final PersonResourceRemovalEvent personResourceRemovalEvent) {
		final long amount = personResourceRemovalEvent.getAmount();
		final PersonId personId = personResourceRemovalEvent.getPersonId();
		final ResourceId resourceId = personResourceRemovalEvent.getResourceId();
		final long oldLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
		resourceDataManager.decrementPersonResourceLevel(resourceId, personId, amount);
		final long newLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
		resolverContext.queueEventForResolution(new PersonResourceChangeObservationEvent(personId, resourceId, oldLevel, newLevel));
	}

	private void handleRegionResourceRemovalEventValidation(final ResolverContext resolverContext, final RegionResourceRemovalEvent regionResourceRemovalEvent) {
		final RegionId regionId = regionResourceRemovalEvent.getRegionId();
		final ResourceId resourceId = regionResourceRemovalEvent.getResourceId();
		final long amount = regionResourceRemovalEvent.getAmount();

		validateRegionId(resolverContext, regionId);
		validateResourceId(resolverContext, resourceId);
		validateNonnegativeResourceAmount(resolverContext, amount);
		validateRegionHasSufficientResources(resolverContext, resourceId, regionId, amount);
	}

	private void handleRegionResourceRemovalEventExecution(final ResolverContext resolverContext, final RegionResourceRemovalEvent regionResourceRemovalEvent) {
		final RegionId regionId = regionResourceRemovalEvent.getRegionId();
		final ResourceId resourceId = regionResourceRemovalEvent.getResourceId();
		final long amount = regionResourceRemovalEvent.getAmount();

		final long previousResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
		long currentResourceLevel;
		resourceDataManager.decrementRegionResourceLevel(regionId, resourceId, amount);
		currentResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
		resolverContext.queueEventForResolution(new RegionResourceChangeObservationEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel));
	}

	private void handleResourcePropertyValueAssignmentEventValidation(final ResolverContext resolverContext, final ResourcePropertyValueAssignmentEvent resourcePropertyValueAssignmentEvent) {
		final ResourceId resourceId = resourcePropertyValueAssignmentEvent.getResourceId();
		final ResourcePropertyId resourcePropertyId = resourcePropertyValueAssignmentEvent.getResourcePropertyId();
		final Object resourcePropertyValue = resourcePropertyValueAssignmentEvent.getResourcePropertyValue();

		validateResourceId(resolverContext, resourceId);
		validateResourcePropertyId(resolverContext, resourceId, resourcePropertyId);
		validateResourcePropertyValueNotNull(resolverContext, resourcePropertyValue);
		final PropertyDefinition propertyDefinition = resourceDataManager.getResourcePropertyDefinition(resourceId, resourcePropertyId);
		validateValueCompatibility(resolverContext, resourcePropertyId, propertyDefinition, resourcePropertyValue);
		validatePropertyMutability(resolverContext, propertyDefinition);
	}

	private void handleResourcePropertyValueAssignmentEventExecution(final ResolverContext resolverContext, final ResourcePropertyValueAssignmentEvent resourcePropertyValueAssignmentEvent) {
		final ResourceId resourceId = resourcePropertyValueAssignmentEvent.getResourceId();
		final ResourcePropertyId resourcePropertyId = resourcePropertyValueAssignmentEvent.getResourcePropertyId();
		final Object resourcePropertyValue = resourcePropertyValueAssignmentEvent.getResourcePropertyValue();

		final Object oldPropertyValue = resourceDataManager.getResourcePropertyValue(resourceId, resourcePropertyId);
		resourceDataManager.setResourcePropertyValue(resourceId, resourcePropertyId, resourcePropertyValue);
		resolverContext.queueEventForResolution(new ResourcePropertyChangeObservationEvent(resourceId, resourcePropertyId, oldPropertyValue, resourcePropertyValue));
	}

	private void handlePopulationGrowthProjectiontEventExecution(final ResolverContext resolverContext, final PopulationGrowthProjectionEvent populationGrowthProjectionEvent) {
		resourceDataManager.expandCapacity(populationGrowthProjectionEvent.getCount());
	}

	private void handleInterRegionalResourceTransferEventValidation(final ResolverContext resolverContext, final InterRegionalResourceTransferEvent interRegionalResourceTransferEvent) {
		final long amount = interRegionalResourceTransferEvent.getAmount();
		final RegionId destinationRegionId = interRegionalResourceTransferEvent.getDestinationRegionId();
		final ResourceId resourceId = interRegionalResourceTransferEvent.getResourceId();
		final RegionId sourceRegionId = interRegionalResourceTransferEvent.getSourceRegionId();

		validateRegionId(resolverContext, sourceRegionId);
		validateRegionId(resolverContext, destinationRegionId);
		validateResourceId(resolverContext, resourceId);
		validateNonnegativeResourceAmount(resolverContext, amount);
		validateDifferentRegionsForResourceTransfer(resolverContext, sourceRegionId, destinationRegionId);
		validateRegionHasSufficientResources(resolverContext, resourceId, sourceRegionId, amount);
		final long regionResourceLevel = resourceDataManager.getRegionResourceLevel(destinationRegionId, resourceId);
		validateResourceAdditionValue(resolverContext, regionResourceLevel, amount);
	}

	private void handleInterRegionalResourceTransferEventExecution(final ResolverContext resolverContext, final InterRegionalResourceTransferEvent interRegionalResourceTransferEvent) {
		final long amount = interRegionalResourceTransferEvent.getAmount();
		final RegionId destinationRegionId = interRegionalResourceTransferEvent.getDestinationRegionId();
		final ResourceId resourceId = interRegionalResourceTransferEvent.getResourceId();
		final RegionId sourceRegionId = interRegionalResourceTransferEvent.getSourceRegionId();

		final long previousSourceRegionResourceLevel = resourceDataManager.getRegionResourceLevel(sourceRegionId, resourceId);
		final long previousDestinationRegionResourceLevel = resourceDataManager.getRegionResourceLevel(destinationRegionId, resourceId);
		long currentSourceRegionResourceLevel;
		long currentDestinationRegionResourceLevel;
		resourceDataManager.decrementRegionResourceLevel(sourceRegionId, resourceId, amount);
		resourceDataManager.incrementRegionResourceLevel(destinationRegionId, resourceId, amount);

		currentSourceRegionResourceLevel = resourceDataManager.getRegionResourceLevel(sourceRegionId, resourceId);
		currentDestinationRegionResourceLevel = resourceDataManager.getRegionResourceLevel(destinationRegionId, resourceId);

		resolverContext.queueEventForResolution(new RegionResourceChangeObservationEvent(sourceRegionId, resourceId, previousSourceRegionResourceLevel, currentSourceRegionResourceLevel));
		resolverContext.queueEventForResolution(
				new RegionResourceChangeObservationEvent(destinationRegionId, resourceId, previousDestinationRegionResourceLevel, currentDestinationRegionResourceLevel));

	}

	private void handleResourceTransferFromPersonEventValidation(final ResolverContext resolverContext, final ResourceTransferFromPersonEvent resourceTransferFromPersonEvent) {
		final long amount = resourceTransferFromPersonEvent.getAmount();
		final PersonId personId = resourceTransferFromPersonEvent.getPersonId();
		final ResourceId resourceId = resourceTransferFromPersonEvent.getResourceId();

		validatePersonExists(resolverContext, personId);
		validateResourceId(resolverContext, resourceId);
		validateNonnegativeResourceAmount(resolverContext, amount);
		validatePersonHasSufficientResources(resolverContext, resourceId, personId, amount);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final long regionResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
		validateResourceAdditionValue(resolverContext, regionResourceLevel, amount);
	}

	private void handleResourceTransferFromPersonEventExecution(final ResolverContext resolverContext, final ResourceTransferFromPersonEvent resourceTransferFromPersonEvent) {
		final long amount = resourceTransferFromPersonEvent.getAmount();
		final PersonId personId = resourceTransferFromPersonEvent.getPersonId();
		final ResourceId resourceId = resourceTransferFromPersonEvent.getResourceId();
		validateResourceId(resolverContext, resourceId);
		validatePersonExists(resolverContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final long previousRegionResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
		long currentRegionResourceLevel;
		final long oldLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
		resourceDataManager.decrementPersonResourceLevel(resourceId, personId, amount);
		final long newLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
		resourceDataManager.incrementRegionResourceLevel(regionId, resourceId, amount);
		currentRegionResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
		resolverContext.queueEventForResolution(new PersonResourceChangeObservationEvent(personId, resourceId, oldLevel, newLevel));
		resolverContext.queueEventForResolution(new RegionResourceChangeObservationEvent(regionId, resourceId, previousRegionResourceLevel, currentRegionResourceLevel));
	}

	private void handlePersonCreationObservationEventValidation(final ResolverContext resolverContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonContructionData personContructionData = personCreationObservationEvent.getPersonContructionData();
		validatePersonExists(resolverContext, personCreationObservationEvent.getPersonId());
		List<ResourceInitialization> resourceAssignments = personContructionData.getValues(ResourceInitialization.class);
		for (final ResourceInitialization resourceAssignment : resourceAssignments) {
			ResourceId resourceId = resourceAssignment.getResourceId();
			Long amount = resourceAssignment.getAmount();
			validateResourceId(resolverContext, resourceId);
			validateNonnegativeResourceAmount(resolverContext, amount);
		}

	}

	private void handlePersonCreationObservationEventExecution(final ResolverContext resolverContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		PersonContructionData personContructionData = personCreationObservationEvent.getPersonContructionData();
		List<ResourceInitialization> resourceAssignments = personContructionData.getValues(ResourceInitialization.class);
		for (final ResourceInitialization resourceAssignment : resourceAssignments) {
			resourceDataManager.incrementPersonResourceLevel(resourceAssignment.getResourceId(), personId, resourceAssignment.getAmount());
		}
	}

	private void handleBulkPersonCreationObservationEventValidation(final ResolverContext resolverContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		for (PersonContructionData personContructionData : personContructionDatas) {
			List<ResourceInitialization> resourceAssignments = personContructionData.getValues(ResourceInitialization.class);
			for (final ResourceInitialization resourceAssignment : resourceAssignments) {
				ResourceId resourceId = resourceAssignment.getResourceId();
				Long amount = resourceAssignment.getAmount();
				validateResourceId(resolverContext, resourceId);
				validateNonnegativeResourceAmount(resolverContext, amount);
			}
		}

	}

	private void handleBulkPersonCreationObservationEventExecution(final ResolverContext resolverContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		PersonId personId = bulkPersonCreationObservationEvent.getPersonId();
		int pId = personId.getValue();
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		for (PersonContructionData personContructionData : personContructionDatas) {
			List<ResourceInitialization> resourceAssignments = personContructionData.getValues(ResourceInitialization.class);
			PersonId boxedPersonId = personDataView.getBoxedPersonId(pId);
			for (final ResourceInitialization resourceAssignment : resourceAssignments) {
				resourceDataManager.incrementPersonResourceLevel(resourceAssignment.getResourceId(), boxedPersonId, resourceAssignment.getAmount());
			}
			pId++;
		}

	}

	private void handleResourceTransferToPersonEventValidation(final ResolverContext resolverContext, final ResourceTransferToPersonEvent resourceTransferToPersonEvent) {
		final PersonId personId = resourceTransferToPersonEvent.getPersonId();
		final ResourceId resourceId = resourceTransferToPersonEvent.getResourceId();
		final long amount = resourceTransferToPersonEvent.getAmount();

		validatePersonExists(resolverContext, personId);
		validateResourceId(resolverContext, resourceId);
		validateNonnegativeResourceAmount(resolverContext, amount);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		validateRegionHasSufficientResources(resolverContext, resourceId, regionId, amount);
		final long personResourceLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
		validateResourceAdditionValue(resolverContext, personResourceLevel, amount);
	}

	private void handleResourceTransferToPersonEventExecution(final ResolverContext resolverContext, final ResourceTransferToPersonEvent resourceTransferToPersonEvent) {
		final PersonId personId = resourceTransferToPersonEvent.getPersonId();
		final ResourceId resourceId = resourceTransferToPersonEvent.getResourceId();
		final long amount = resourceTransferToPersonEvent.getAmount();
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final long personResourceLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);

		final long previousRegionResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
		resourceDataManager.decrementRegionResourceLevel(regionId, resourceId, amount);
		resourceDataManager.incrementPersonResourceLevel(resourceId, personId, amount);
		final long newLevel = resourceDataManager.getPersonResourceLevel(resourceId, personId);
		long currentRegionResourceLevel = resourceDataManager.getRegionResourceLevel(regionId, resourceId);

		resolverContext.queueEventForResolution(new RegionResourceChangeObservationEvent(regionId, resourceId, previousRegionResourceLevel, currentRegionResourceLevel));

		resolverContext.queueEventForResolution(new PersonResourceChangeObservationEvent(personId, resourceId, personResourceLevel, newLevel));

	}

	private void validateDifferentRegionsForResourceTransfer(final ResolverContext resolverContext, final RegionId sourceRegionId, final RegionId destinationRegionId) {
		if (sourceRegionId.equals(destinationRegionId)) {
			resolverContext.throwContractException(ResourceError.REFLEXIVE_RESOURCE_TRANSFER);
		}
	}

	private void validateNonnegativeResourceAmount(final ResolverContext resolverContext, final long amount) {
		if (amount < 0) {
			resolverContext.throwContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
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
	 * Preconditions : the resource and person must exist
	 */
	private void validatePersonHasSufficientResources(final ResolverContext resolverContext, final ResourceId resourceId, final PersonId personId, final long amount) {
		final long oldValue = resourceDataManager.getPersonResourceLevel(resourceId, personId);
		if (oldValue < amount) {
			resolverContext.throwContractException(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	private void validatePropertyMutability(final ResolverContext resolverContext, final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			resolverContext.throwContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	/*
	 * Preconditions : the region and resource must exist
	 */
	private void validateRegionHasSufficientResources(final ResolverContext resolverContext, final ResourceId resourceId, final RegionId regionId, final long amount) {
		final long currentAmount = resourceDataManager.getRegionResourceLevel(regionId, resourceId);
		if (currentAmount < amount) {
			resolverContext.throwContractException(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	/*
	 * Validates the region id
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_REGION_ID} if the region id is null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if the region id does
	 * not correspond to a known region
	 */
	private void validateRegionId(final ResolverContext resolverContext, final RegionId regionId) {

		if (regionId == null) {
			resolverContext.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionIds.contains(regionId)) {
			resolverContext.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private void validateResourceAdditionValue(final ResolverContext resolverContext, final long currentResourceLevel, final long amount) {
		try {
			Math.addExact(currentResourceLevel, amount);
		} catch (final ArithmeticException e) {
			resolverContext.throwContractException(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION);
		}
	}

	/*
	 * Validates the resource id.
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if the resource id is
	 * null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if the resource id
	 * does not correspond to a known resource
	 */
	private void validateResourceId(final ResolverContext resolverContext, final ResourceId resourceId) {
		if (resourceId == null) {
			resolverContext.throwContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (!resourceDataManager.resourceIdExists(resourceId)) {
			resolverContext.throwContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	/*
	 * Assumes a valid resource id
	 */
	private void validateResourcePropertyId(final ResolverContext resolverContext, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		if (resourcePropertyId == null) {
			resolverContext.throwContractException(ResourceError.NULL_RESOURCE_PROPERTY_ID);
		}

		final Set<ResourcePropertyId> set = resourcePropertyIdsMap.get(resourceId);
		if (set == null) {
			resolverContext.throwContractException(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, resourcePropertyId);
		}
		if (!set.contains(resourcePropertyId)) {
			resolverContext.throwContractException(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, resourcePropertyId);
		}

	}

	private void validateResourcePropertyValueNotNull(final ResolverContext resolverContext, final Object propertyValue) {
		if (propertyValue == null) {
			resolverContext.throwContractException(ResourceError.NULL_RESOURCE_PROPERTY_VALUE);
		}
	}

	private void validateValueCompatibility(final ResolverContext resolverContext, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			resolverContext.throwContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void handlePersonImminentRemovalObservationEventValidation(final ResolverContext resolverContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		validatePersonExists(resolverContext, personImminentRemovalObservationEvent.getPersonId());
	}

	private void handlePersonImminentRemovalObservationEventExecution(final ResolverContext resolverContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		resolverContext.addPlan((context) -> resourceDataManager.dropPerson(personImminentRemovalObservationEvent.getPersonId()), resolverContext.getTime());
	}

}
