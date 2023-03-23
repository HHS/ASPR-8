package plugins.regions.datamanagers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.Event;
import nucleus.EventFilter;
import nucleus.IdentifiableFunctionMap;
import nucleus.SimulationStateContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.PersonImminentAdditionEvent;
import plugins.people.events.PersonRemovalEvent;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.events.RegionAdditionEvent;
import plugins.regions.events.RegionPropertyDefinitionEvent;
import plugins.regions.events.RegionPropertyUpdateEvent;
import plugins.regions.support.RegionConstructionData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyDefinitionInitialization;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.PropertyValueRecord;
import plugins.util.properties.TimeTrackingPolicy;
import plugins.util.properties.arraycontainers.DoubleValueContainer;
import plugins.util.properties.arraycontainers.IntValueContainer;
import util.errors.ContractException;

/**
 * Mutable data manager that backs the {@linkplain RegionDataView}. This data
 * manager is for internal use by the {@link RegionsPlugin} and should not be
 * published.
 *
 * All regions and region properties are established during construction and
 * cannot be changed. Region property values are mutable. Limited validation of
 * inputs are performed and mutation methods have invocation ordering
 * requirements.
 *
 *
 */
public final class RegionsDataManager extends DataManager {
	/*
	 * Record for maintaining the number of people either globally or
	 * regionally. Also maintains the time when the population count was last
	 * changed. PopulationRecords are maintained to eliminate iterations over
	 * other tracking structures to answer queries about population counts.
	 */
	private static class PopulationRecord {
		private int populationCount;
		private double assignmentTime;
	}

	private DataManagerContext dataManagerContext;

	private final RegionsPluginData regionsPluginData;

	private PeopleDataManager peopleDataManager;

	private final Map<RegionPropertyId, Integer> nonDefaultBearingPropertyIds = new LinkedHashMap<>();

	private boolean[] nonDefaultChecks = new boolean[0];

	private final Map<RegionId, Map<RegionPropertyId, PropertyValueRecord>> regionPropertyMap = new LinkedHashMap<>();

	private final Set<RegionPropertyId> regionPropertyIds = new LinkedHashSet<>();
	private final Map<RegionPropertyId, PropertyDefinition> regionPropertyDefinitions = new LinkedHashMap<>();
	private final Map<RegionPropertyId, Double> regionPropertyDefinitionTimes = new LinkedHashMap<>();
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
	private final List<RegionId> indexToRegionMap = new ArrayList<>();

	/*
	 * Stores region identifiers as int values indexed by person id values
	 */
	private IntValueContainer regionValues;

	/*
	 * Stores double region arrival values indexed by person id values.
	 * Maintenance depends upon tracking policy.
	 */
	private DoubleValueContainer regionArrivalTimes;

	private TimeTrackingPolicy regionArrivalTrackingPolicy;

	/**
	 * Creates a Region Data Manager from the given resolver context.
	 * Preconditions: The context must be a valid and non-null.
	 */
	public RegionsDataManager(final RegionsPluginData regionsPluginData) {
		if (regionsPluginData == null) {
			throw new ContractException(RegionError.NULL_REGION_PLUGIN_DATA);
		}
		this.regionsPluginData = regionsPluginData;
	}

	private void addNonDefaultProperty(final RegionPropertyId regionPropertyId) {
		nonDefaultBearingPropertyIds.put(regionPropertyId, nonDefaultBearingPropertyIds.size());
		nonDefaultChecks = new boolean[nonDefaultBearingPropertyIds.size()];
	}

	private void clearNonDefaultChecks() {
		for (int i = 0; i < nonDefaultChecks.length; i++) {
			nonDefaultChecks[i] = false;
		}
	}

	private void markAssigned(RegionPropertyId regionPropertyId) {
		Integer nonDefaultPropertyIndex = nonDefaultBearingPropertyIds.get(regionPropertyId);
		if (nonDefaultPropertyIndex != null) {
			nonDefaultChecks[nonDefaultPropertyIndex] = true;
		}
	}

	private void verifyNonDefaultChecks() {

		boolean missingPropertyAssignments = false;

		for (int i = 0; i < nonDefaultChecks.length; i++) {
			if (!nonDefaultChecks[i]) {
				missingPropertyAssignments = true;
				break;
			}
		}

		if (missingPropertyAssignments) {
			StringBuilder sb = new StringBuilder();
			int index = -1;
			boolean firstMember = true;
			for (RegionPropertyId regionPropertyId : nonDefaultBearingPropertyIds.keySet()) {
				index++;
				if (!nonDefaultChecks[index]) {
					if (firstMember) {
						firstMember = false;
					} else {
						sb.append(", ");
					}
					sb.append(regionPropertyId);
				}
			}
			throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, sb.toString());
		}
	}

	private void validateRegionConstructionDataNotNull(RegionConstructionData regionConstructionData) {
		if (regionConstructionData == null) {
			throw new ContractException(RegionError.NULL_REGION_CONSTRUCTION_DATA);
		}
	}

	private static record RegionAdditionMutationEvent(RegionConstructionData regionConstructionData) implements Event {
	}

	/**
	 * Adds a new region id
	 *
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain RegionError#NULL_REGION_CONSTRUCTION_DATA} if
	 *             the region construction data is null</li>
	 * 
	 *             <li>{@linkplain RegionError#DUPLICATE_REGION_ID} if the
	 *             region is already present</li>
	 * 
	 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *             if for any property that lacks default value there is a
	 *             region that has not had a value assigned</li>
	 *
	 */
	public void addRegion(final RegionConstructionData regionConstructionData) {		
		dataManagerContext.releaseMutationEvent(new RegionAdditionMutationEvent(regionConstructionData));
	}

	private void handleRegionAdditionMutationEvent(DataManagerContext dataManagerContext, RegionAdditionMutationEvent regionAdditionMutationEvent) {
		RegionConstructionData regionConstructionData = regionAdditionMutationEvent.regionConstructionData();
		validateRegionConstructionDataNotNull(regionConstructionData);
		RegionId regionId = regionConstructionData.getRegionId();
		validateNewRegionId(regionId);
		Map<RegionPropertyId, Object> regionPropertyValues = regionConstructionData.getRegionPropertyValues();
		for (RegionPropertyId regionPropertyId : regionPropertyValues.keySet()) {
			validateRegionPropertyId(regionPropertyId);				
			Object regionPropertyValue = regionPropertyValues.get(regionPropertyId);
			final PropertyDefinition propertyDefinition = regionPropertyDefinitions.get(regionPropertyId);				
			validateValueCompatibility(regionPropertyId, propertyDefinition, regionPropertyValue);
		}	
		
		if (!nonDefaultBearingPropertyIds.isEmpty()) {
			clearNonDefaultChecks();
			for (RegionPropertyId regionPropertyId : regionPropertyValues.keySet()) {
				markAssigned(regionPropertyId);
			}
			verifyNonDefaultChecks();
		}	
		
		regionPopulationRecordMap.put(regionId, new PopulationRecord());
		regionToIndexMap.put(regionId, regionToIndexMap.size() + 1);
		indexToRegionMap.add(regionId);

		final Map<RegionPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
		regionPropertyMap.put(regionId, map);		

		for (RegionPropertyId regionPropertyId : regionPropertyValues.keySet()) {
			Object regionPropertyValue = regionPropertyValues.get(regionPropertyId);							
			PropertyValueRecord propertyValueRecord = map.get(regionPropertyId);
			if (propertyValueRecord == null) {
				propertyValueRecord = new PropertyValueRecord(dataManagerContext);
				map.put(regionPropertyId, propertyValueRecord);
			}
			propertyValueRecord.setPropertyValue(regionPropertyValue);
		}

		if (dataManagerContext.subscribersExist(RegionAdditionEvent.class)) {
			RegionAdditionEvent.Builder regionAdditionEventBuilder = RegionAdditionEvent.builder().setRegionId(regionId);
			for (Object value : regionConstructionData.getValues(Object.class)) {
				regionAdditionEventBuilder.addValue(value);
			}
			RegionAdditionEvent regionAdditionEvent = regionAdditionEventBuilder.build();
			dataManagerContext.releaseObservationEvent(regionAdditionEvent);
		}

	}

	private static record RegionPropertyDefinitionMutationEvent(RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization) implements Event {
	}

	/**
	 * Adds a new region property
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_DEFINITION_INITIALIZATION}
	 *             if the region property definition initialization is null</li>
	 * 
	 *             <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
	 *             if the region property is already defined</li>
	 * 
	 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *             if the region property definition has no default and a
	 *             property value for some region is missing from the
	 *             RegionPropertyDefinitionInitialization</li>
	 */
	public void defineRegionProperty(final RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization) {
		dataManagerContext.releaseMutationEvent(new RegionPropertyDefinitionMutationEvent(regionPropertyDefinitionInitialization));
	}

	private void handleRegionPropertyDefinitionMutationEvent(DataManagerContext dataManagerContext, RegionPropertyDefinitionMutationEvent regionPropertyDefinitionMutationEvent) {

		RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = regionPropertyDefinitionMutationEvent.regionPropertyDefinitionInitialization();
		validateregionPropertyDefinitionInitializationNotNull(regionPropertyDefinitionInitialization);
		final RegionPropertyId regionPropertyId = regionPropertyDefinitionInitialization.getRegionPropertyId();
		final PropertyDefinition propertyDefinition = regionPropertyDefinitionInitialization.getPropertyDefinition();
		validateNewRegionPropertyId(regionPropertyId);
		validateNewPropertyDefinition(propertyDefinition);
		
		final boolean checkAllRegionsHaveValues = propertyDefinition.getDefaultValue().isEmpty();

		if (checkAllRegionsHaveValues) {			
			final Map<RegionId, Boolean> coverageSet = new LinkedHashMap<>();
			for (final RegionId regionId : regionPropertyMap.keySet()) {
				coverageSet.put(regionId, false);
			}
			for (final Pair<RegionId, Object> pair : regionPropertyDefinitionInitialization.getPropertyValues()) {
				final RegionId regionId = pair.getFirst();
				coverageSet.put(regionId, true);
			}
			for (final RegionId regionId : coverageSet.keySet()) {
				if (!coverageSet.get(regionId)) {
					throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);
				}
			}
		}
			
		if (checkAllRegionsHaveValues) {
			addNonDefaultProperty(regionPropertyId);
		}
		regionPropertyDefinitionTimes.put(regionPropertyId, dataManagerContext.getTime());
		regionPropertyIds.add(regionPropertyId);
		regionPropertyDefinitions.put(regionPropertyId, propertyDefinition);
		
		for (final Pair<RegionId, Object> pair : regionPropertyDefinitionInitialization.getPropertyValues()) {
			final RegionId regionId = pair.getFirst();

			/*
			 * we do not have to validate the value since it is guaranteed
			 * to be consistent with the property definition by contract.
			 */
			final Object value = pair.getSecond();
			Map<RegionPropertyId, PropertyValueRecord> map = regionPropertyMap.get(regionId);
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
			map.put(regionPropertyId, propertyValueRecord);
			propertyValueRecord.setPropertyValue(value);

		}

		if (dataManagerContext.subscribersExist(RegionPropertyDefinitionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new RegionPropertyDefinitionEvent(regionPropertyId));
		}

	}

	/**
	 * Expands the capacity of data structures to hold people by the given
	 * count. Used to more efficiently prepare for multiple population
	 * additions.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NEGATIVE_GROWTH_PROJECTION} if
	 *             the count is negative</li>
	 */
	public void expandCapacity(final int count) {
		if (count < 0) {
			throw new ContractException(PersonError.NEGATIVE_GROWTH_PROJECTION);
		}
		if (count > 0) {
			regionValues.setCapacity(regionValues.getCapacity() + count);
			if (regionArrivalTimes != null) {
				regionArrivalTimes.setCapacity(regionArrivalTimes.getCapacity() + count);
			}
		}
	}

	/**
	 * Returns as a List the person identifiers of the people in the given
	 * region. List elements are unique.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the c id is
	 *             null
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known
	 */
	public List<PersonId> getPeopleInRegion(final RegionId regionId) {
		validateRegionId(regionId);

		final int targetRegionIndex = regionToIndexMap.get(regionId).intValue();

		final List<PersonId> result = new ArrayList<>();

		final int n = regionValues.size();
		for (int personIndex = 0; personIndex < n; personIndex++) {
			final int regionIndex = regionValues.getValueAsInt(personIndex);
			/*
			 * a region index of zero will not match any valid region,
			 * indicating that person does not exist
			 */
			if (targetRegionIndex == regionIndex) {
				final PersonId personId = peopleDataManager.getBoxedPersonId(personIndex).get();
				result.add(personId);
			}
		}

		return result;
	}

	/**
	 * Returns the region associated with the given person id.
	 *
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the person id is unknown
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionId> T getPersonRegion(final PersonId personId) {
		validatePersonExists(personId);
		final int r = regionValues.getValueAsInt(personId.getValue());
		return (T) indexToRegionMap.get(r);
	}

	/**
	 * Returns the time when then person arrived at their current region.
	 *
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the person id is unknown
	 *                          <li>{@linkplain RegionError#REGION_ARRIVAL_TIMES_NOT_TRACKED}
	 *                          if the region arrival times are not being
	 *                          tracked</li>
	 *
	 */
	public double getPersonRegionArrivalTime(final PersonId personId) {
		validatePersonExists(personId);
		validatePersonRegionArrivalsTimesTracked();
		return regionArrivalTimes.getValue(personId.getValue());
	}

	/**
	 * Returns the policy for tracking the last region arrival time for each
	 * person
	 */
	public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy() {
		return regionArrivalTrackingPolicy;
	}

	/**
	 * Returns the set of {@link RegionId} values that are defined by the
	 * {@link RegionsPluginData}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionId> Set<T> getRegionIds() {
		final Set<T> result = new LinkedHashSet<>(regionPopulationRecordMap.size());
		for (final RegionId regionId : regionPopulationRecordMap.keySet()) {
			result.add((T) regionId);
		}
		return result;
	}

	/**
	 * Returns the number of people currently in the given region.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known
	 */
	public int getRegionPopulationCount(final RegionId regionId) {
		validateRegionId(regionId);
		return regionPopulationRecordMap.get(regionId).populationCount;
	}

	/**
	 * Returns the time when the current population of the given region was
	 * established.
	 *
	 * @throwsContractException
	 *                          <li>{@linkplain RegionError#NULL_REGION_ID} if
	 *                          the region id is null
	 *                          <li>{@linkplain RegionError#UNKNOWN_REGION_ID}
	 *                          if the region id is not known
	 */
	public double getRegionPopulationTime(final RegionId regionId) {
		validateRegionId(regionId);
		return regionPopulationRecordMap.get(regionId).assignmentTime;
	}

	/**
	 * Returns the property definition for the given {@link RegionPropertyId}
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the region
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             region property id is unknown
	 *
	 */
	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
		validateRegionPropertyId(regionPropertyId);
		return regionPropertyDefinitions.get(regionPropertyId);
	}

	/**
	 * Returns the {@link RegionPropertyId} values
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds() {
		final Set<T> result = new LinkedHashSet<>(regionPropertyDefinitions.keySet().size());
		for (final RegionPropertyId regionPropertyId : regionPropertyDefinitions.keySet()) {
			result.add((T) regionPropertyId);
		}
		return result;
	}

	/**
	 * Returns the time when the of the region property was last assigned.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the region
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             region property id is unknown</li>
	 */
	public double getRegionPropertyTime(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		validateRegionId(regionId);
		validateRegionPropertyId(regionPropertyId);
		Map<RegionPropertyId, PropertyValueRecord> map = regionPropertyMap.get(regionId);
		PropertyValueRecord propertyValueRecord = map.get(regionPropertyId);
		if (propertyValueRecord != null) {
			return propertyValueRecord.getAssignmentTime();
		}
		return regionPropertyDefinitionTimes.get(regionPropertyId);
	}

	/**
	 * Returns the value of the region property.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the region
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             region property id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		validateRegionId(regionId);
		validateRegionPropertyId(regionPropertyId);
		Map<RegionPropertyId, PropertyValueRecord> map = regionPropertyMap.get(regionId);
		PropertyValueRecord propertyValueRecord = map.get(regionPropertyId);
		if (propertyValueRecord != null) {
			return (T) propertyValueRecord.getValue();
		}
		/*
		 * If the value is missing, then we are guaranteed that the property
		 * definition will have a default value.
		 */
		PropertyDefinition propertyDefinition = regionPropertyDefinitions.get(regionPropertyId);
		return (T) propertyDefinition.getDefaultValue().get();
	}

	private void handlePersonImminentAdditionEvent(final DataManagerContext dataManagerContext, final PersonImminentAdditionEvent personImminentAdditionEvent) {
		final PersonConstructionData personConstructionData = personImminentAdditionEvent.personConstructionData();
		final RegionId regionId = personConstructionData.getValue(RegionId.class).orElse(null);
		validateRegionId(regionId);
		final PersonId personId = personImminentAdditionEvent.personId();

		validatePersonExists(personId);
		validateRegionId(regionId);
		validatePersonNotContained(personId);

		/*
		 * Update the population count of the new region
		 */

		final PopulationRecord populationRecord = regionPopulationRecordMap.get(regionId);
		populationRecord.populationCount++;
		populationRecord.assignmentTime = dataManagerContext.getTime();

		final Integer regionIndex = regionToIndexMap.get(regionId).intValue();
		regionValues.setIntValue(personId.getValue(), regionIndex);

		if (regionArrivalTimes != null) {
			regionArrivalTimes.setValue(personId.getValue(), dataManagerContext.getTime());
		}
	}

	/*
	 * Removes the person from this data manager.
	 *
	 * Precondition : the person must exist and be stored in this manager
	 *
	 * @throws ContractException <li>{@linkplain PersonError.NULL_PERSON_ID} if
	 * the person id is null</li> <li>{@linkplain PersonError.UNKNOWN_PERSON_ID}
	 * if the person id is unknown</li>
	 *
	 */
	private void handlePersonRemovalEvent(final DataManagerContext dataManagerContext, final PersonRemovalEvent personRemovalEvent) {
		final PersonId personId = personRemovalEvent.personId();
		validatePersonContained(personId);
		final int regionIndex = regionValues.getValueAsInt(personId.getValue());
		final RegionId oldRegionId = indexToRegionMap.get(regionIndex);
		final PopulationRecord populationRecord = regionPopulationRecordMap.get(oldRegionId);
		populationRecord.populationCount--;
		populationRecord.assignmentTime = dataManagerContext.getTime();
		regionValues.setIntValue(personId.getValue(), 0);
	}

	/**
	 *
	 * <P>
	 * Initializes all event labelers defined by
	 * {@linkplain RegionPropertyUpdateEvent} and
	 * {@linkplain PersonRegionUpdateEvent}
	 * </P>
	 *
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if no region
	 *             id is provided for a person id</li>
	 *
	 *
	 *
	 *             <P>
	 *             Subscribes the following events:
	 *             <ul>
	 *
	 *             <li>{@linkplain PersonImminentAdditionEvent}<blockquote> Sets
	 *             the person's initial region in the
	 *             {@linkplain RegionLocationDataView} from the region reference
	 *             in the auxiliary data of the event.
	 *
	 *             <BR>
	 *             <BR>
	 *             Throws {@link ContractException}
	 *             <ul>
	 *             <li>{@linkplain PersonError.UNKNOWN_PERSON_ID} if the person
	 *             does not exist</li>
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if no region data
	 *             was included in the event</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             in the event is unknown</li>
	 *             <li>{@linkplain RegionError#DUPLICATE_PERSON_ADDITION} if the
	 *             person was previously added</li>
	 *             </ul>
	 * 
	 *             </blockquote></li>
	 *
	 *             <li>{@linkplain PersonRemovalEvent}<blockquote> Removes the
	 *             region assignment data for the person from the
	 *             {@linkplain RegionDataView} <BR>
	 *             <BR>
	 *             Throws {@linkplain ContractException}
	 *             <ul>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not currently tracked by the regions data manager</li>
	 *
	 *
	 *
	 *             </ul>
	 *
	 *             </blockquote></li>
	 *             <ul>
	 *             </p>
	 *
	 *
	 */
	@Override
	public void init(final DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);

		this.dataManagerContext = dataManagerContext;

		peopleDataManager = dataManagerContext.getDataManager(PeopleDataManager.class);

		/*
		 * By setting the default value to 0, we are allowing the container to
		 * grow without having to set values in its array. HOWEVER, THIS IMPLIES
		 * THAT REGIONS MUST BE CONVERTED TO INTEGER VALUES STARTING AT ONE, NOT
		 * ZERO.
		 *
		 *
		 */
		regionValues = new IntValueContainer(0);

		regionArrivalTrackingPolicy = regionsPluginData.getPersonRegionArrivalTrackingPolicy();
		if (regionArrivalTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
			regionArrivalTimes = new DoubleValueContainer(0);
		}

		final Set<RegionId> regionIds = regionsPluginData.getRegionIds();
		for (final RegionId regionId : regionIds) {
			regionPopulationRecordMap.put(regionId, new PopulationRecord());
		}

		int index = 1;
		for (final RegionId regionId : regionIds) {
			regionToIndexMap.put(regionId, index++);
		}

		indexToRegionMap.add(null);
		indexToRegionMap.addAll(regionIds);
		Double time = dataManagerContext.getTime();

		for (final RegionPropertyId regionPropertyId : regionsPluginData.getRegionPropertyIds()) {
			final PropertyDefinition propertyDefinition = regionsPluginData.getRegionPropertyDefinition(regionPropertyId);
			regionPropertyIds.add(regionPropertyId);
			regionPropertyDefinitions.put(regionPropertyId, propertyDefinition);
			regionPropertyDefinitionTimes.put(regionPropertyId, time);
			if (propertyDefinition.getDefaultValue().isEmpty()) {
				nonDefaultBearingPropertyIds.put(regionPropertyId, nonDefaultBearingPropertyIds.size());
			}
		}
		nonDefaultChecks = new boolean[nonDefaultBearingPropertyIds.size()];

		for (final RegionId regionId : regionIds) {
			final Map<RegionPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
			regionPropertyMap.put(regionId, map);
			Map<RegionPropertyId, Object> regionPropertyValues = regionsPluginData.getRegionPropertyValues(regionId);
			for (final RegionPropertyId regionPropertyId : regionPropertyValues.keySet()) {
				final Object regionPropertyValue = regionPropertyValues.get(regionPropertyId);
				final PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
				propertyValueRecord.setPropertyValue(regionPropertyValue);
				map.put(regionPropertyId, propertyValueRecord);
			}
		}

		final List<PersonId> people = peopleDataManager.getPeople();
		for (final PersonId personId : people) {
			final Optional<RegionId> optional = regionsPluginData.getPersonRegion(personId);
			if (optional.isEmpty()) {
				throw new ContractException(RegionError.UNKNOWN_REGION_ID);
			}
			final RegionId regionId = optional.get();
			final PopulationRecord populationRecord = regionPopulationRecordMap.get(regionId);
			populationRecord.populationCount++;
			final Integer regionIndex = regionToIndexMap.get(regionId).intValue();
			regionValues.setIntValue(personId.getValue(), regionIndex);
		}

		dataManagerContext.subscribe(PersonImminentAdditionEvent.class, this::handlePersonImminentAdditionEvent);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);
		dataManagerContext.subscribe(RegionAdditionMutationEvent.class, this::handleRegionAdditionMutationEvent);
		dataManagerContext.subscribe(RegionPropertyDefinitionMutationEvent.class, this::handleRegionPropertyDefinitionMutationEvent);
		dataManagerContext.subscribe(PersonRegionUpdateMutationEvent.class, this::handlePersonRegionUpdateMutationEvent);
		dataManagerContext.subscribe(RegionPropertyUpdateMutationEvent.class, this::handleRegionPropertyUpdateMutationEvent);

		dataManagerContext.subscribeToSimulationState(this::recordSimulationState);
	}

	private void recordSimulationState(DataManagerContext dataManagerContext, SimulationStateContext simulationStateContext) {

		RegionsPluginData.Builder builder = simulationStateContext.get(RegionsPluginData.Builder.class);
		
		Set<RegionId> regionIds = getRegionIds();
		for (RegionId regionId : regionIds) {
			builder.addRegion(regionId);			
		}
		
		for(RegionPropertyId regionPropertyId : getRegionPropertyIds()) {
			PropertyDefinition regionPropertyDefinition = getRegionPropertyDefinition(regionPropertyId);
			builder.defineRegionProperty(regionPropertyId, regionPropertyDefinition);
			for(RegionId regionId : regionIds) {
				Object regionPropertyValue = getRegionPropertyValue(regionId, regionPropertyId);
				builder.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
			}
		}
		
		for(PersonId personId : peopleDataManager.getPeople()) {
			RegionId regionId = getPersonRegion(personId);
			builder.setPersonRegion(personId, regionId);
		}
		
		builder.setPersonRegionArrivalTracking(getPersonRegionArrivalTrackingPolicy());
		
		

		// for (RegionId regionId : regionPopulationRecordMap.keySet()) {
		// builder.addRegion(regionId);
		// }
		//
		// for (RegionPropertyId regionPropertyId :
		// regionPropertyDefinitions.keySet()) {
		// PropertyDefinition propertyDefinition =
		// regionPropertyDefinitions.get(regionPropertyId);
		// builder.defineRegionProperty(regionPropertyId, propertyDefinition);
		// for (RegionId regionId : regionPopulationRecordMap.keySet()) {
		// Map<RegionPropertyId, PropertyValueRecord> map =
		// regionPropertyMap.get(regionId);
		// PropertyValueRecord propertyValueRecord = map.get(regionPropertyId);
		// if (propertyValueRecord != null) {
		// builder.setRegionPropertyValue(regionId, regionPropertyId,
		// propertyValueRecord.getValue());
		// } else {
		// builder.setRegionPropertyValue(regionId, regionPropertyId,
		// propertyDefinition.getDefaultValue().get());
		// }
		// }
		// }
		//
		// for (PersonId personId : peopleDataManager.getPeople()) {
		// final int r = regionValues.getValueAsInt(personId.getValue());
		// RegionId regionId = indexToRegionMap.get(r);
		// builder.setPersonRegion(personId, regionId);
		// }
		//
		// builder.setPersonRegionArrivalTracking(regionArrivalTrackingPolicy);

	}

	/**
	 * Return true if and only if the given {@link RegionId} exits. Null
	 * tolerant.
	 */
	public boolean regionIdExists(final RegionId regionId) {
		return regionPopulationRecordMap.containsKey(regionId);
	}

	/**
	 * Returns true if and only if the given {@link RegionPropertyId} exists.
	 * Tolerates nulls.
	 */
	public boolean regionPropertyIdExists(final RegionPropertyId regionPropertyId) {
		return regionPropertyDefinitions.containsKey(regionPropertyId);
	}

	private static record PersonRegionUpdateMutationEvent(PersonId personId, RegionId regionId) implements Event {
	}

	/**
	 *
	 * Updates the person's current region and region arrival time. Generates a
	 * corresponding {@linkplain PersonRegionUpdateEvent}
	 *
	 * Throws {@link ContractException}
	 *
	 *
	 * <li>{@link PersonError#NULL_PERSON_ID} if the person id is null</li>
	 * <li>{@link PersonError#UNKNOWN_PERSON_ID} if the person id is
	 * unknown</li>
	 * <li>{@link RegionError#NULL_REGION_ID} if the region id is null</li>
	 * <li>{@link RegionError#UNKNOWN_REGION_ID} if the region id is
	 * unknown</li>
	 *
	 */

	public void setPersonRegion(final PersonId personId, final RegionId regionId) {
		dataManagerContext.releaseMutationEvent(new PersonRegionUpdateMutationEvent(personId, regionId));
	}

	private void handlePersonRegionUpdateMutationEvent(DataManagerContext dataManagerContext, PersonRegionUpdateMutationEvent personRegionUpdateMutationEvent) {
		PersonId personId = personRegionUpdateMutationEvent.personId();
		RegionId regionId = personRegionUpdateMutationEvent.regionId();
		validatePersonExists(personId);
		validateRegionId(regionId);

		/*
		 * Retrieve the int value that represents the current region of the
		 * person
		 */
		int regionIndex = regionValues.getValueAsInt(personId.getValue());
		final RegionId oldRegionId = indexToRegionMap.get(regionIndex);
		PopulationRecord populationRecord = regionPopulationRecordMap.get(oldRegionId);
		/*
		 * Update the population count associated with the old region
		 */
		populationRecord.populationCount--;
		populationRecord.assignmentTime = dataManagerContext.getTime();

		/*
		 * Update the population count of the new region
		 */
		populationRecord = regionPopulationRecordMap.get(regionId);
		populationRecord.populationCount++;
		populationRecord.assignmentTime = dataManagerContext.getTime();
		/*
		 * Convert the new region id into an int
		 */

		regionIndex = regionToIndexMap.get(regionId).intValue();
		/*
		 * Store in the int at the person's index
		 */
		regionValues.setIntValue(personId.getValue(), regionIndex);
		/*
		 * If region arrival times are being tracked, do so.
		 */
		if (regionArrivalTimes != null) {
			regionArrivalTimes.setValue(personId.getValue(), dataManagerContext.getTime());
		}

		if (dataManagerContext.subscribersExist(PersonRegionUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(new PersonRegionUpdateEvent(personId, oldRegionId, regionId));
		}

	}

	private static record RegionPropertyUpdateMutationEvent(RegionId regionId, RegionPropertyId regionPropertyId, Object regionPropertyValue) implements Event {
	}

	/**
	 * Updates the region's property value and time. Generates a corresponding
	 * {@linkplain RegionPropertyUpdateEvent}
	 *
	 * Throws {@link ContractException}
	 *
	 * <li>{@link RegionError#NULL_REGION_ID} if the region id is null
	 * <li>{@link RegionError#UNKNOWN_REGION_ID} if the region id is unknown
	 * <li>{@link PropertyError#NULL_PROPERTY_ID} if the property id is null
	 * <li>{@link PropertyError#UNKNOWN_PROPERTY_ID} if the property id is
	 * unknown
	 * <li>{@link PropertyError#NULL_PROPERTY_VALUE} if the value is null
	 * <li>{@link PropertyError#INCOMPATIBLE_VALUE} if the value is incompatible
	 * with the defined type for the property
	 * <li>{@link PropertyError#IMMUTABLE_VALUE} if the property has been
	 * defined as immutable
	 *
	 * </blockquote></li>
	 */

	public void setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {
		dataManagerContext.releaseMutationEvent(new RegionPropertyUpdateMutationEvent(regionId, regionPropertyId, regionPropertyValue));
	}

	private void handleRegionPropertyUpdateMutationEvent(DataManagerContext dataManagerContext, RegionPropertyUpdateMutationEvent regionPropertyUpdateMutationEvent) {
		RegionId regionId = regionPropertyUpdateMutationEvent.regionId();
		RegionPropertyId regionPropertyId = regionPropertyUpdateMutationEvent.regionPropertyId();
		Object regionPropertyValue = regionPropertyUpdateMutationEvent.regionPropertyValue();

		validateRegionId(regionId);
		validateRegionPropertyId(regionPropertyId);
		validateRegionPropertyValueNotNull(regionPropertyValue);
		final PropertyDefinition propertyDefinition = getRegionPropertyDefinition(regionPropertyId);
		validateValueCompatibility(regionPropertyId, propertyDefinition, regionPropertyValue);
		validatePropertyMutability(propertyDefinition);
		Map<RegionPropertyId, PropertyValueRecord> map = regionPropertyMap.get(regionId);
		PropertyValueRecord propertyValueRecord = map.get(regionPropertyId);

		if (dataManagerContext.subscribersExist(RegionPropertyUpdateEvent.class)) {
			Object previousPropertyValue;
			if (propertyValueRecord == null) {
				propertyValueRecord = new PropertyValueRecord(dataManagerContext);
				map.put(regionPropertyId, propertyValueRecord);
				previousPropertyValue = regionPropertyDefinitions.get(regionPropertyId).getDefaultValue().get();
			} else {
				previousPropertyValue = propertyValueRecord.getValue();
			}
			propertyValueRecord.setPropertyValue(regionPropertyValue);
			dataManagerContext.releaseObservationEvent(new RegionPropertyUpdateEvent(regionId, regionPropertyId, previousPropertyValue, regionPropertyValue));
		} else {

			if (propertyValueRecord == null) {
				propertyValueRecord = new PropertyValueRecord(dataManagerContext);
				map.put(regionPropertyId, propertyValueRecord);

			}
			propertyValueRecord.setPropertyValue(regionPropertyValue);
		}

	}

	private void validateNewPropertyDefinition(final PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
	}

	private void validateNewRegionId(final RegionId regionId) {

		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}

		if (regionIdExists(regionId)) {
			throw new ContractException(RegionError.DUPLICATE_REGION_ID, regionId);
		}
	}

	private void validateNewRegionPropertyId(final RegionPropertyId regionPropertyId) {
		if (regionPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
		if (regionPropertyIdExists(regionPropertyId)) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION, regionPropertyId);
		}
	}

	private void validatePersonContained(final PersonId personId) {

		final int regionIndex = regionValues.getValueAsInt(personId.getValue());
		if (regionIndex == 0) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}

		if (!peopleDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void validatePersonNotContained(final PersonId personId) {

		final int regionIndex = regionValues.getValueAsInt(personId.getValue());
		if (regionIndex != 0) {
			throw new ContractException(RegionError.DUPLICATE_PERSON_ADDITION);
		}
	}

	private void validatePersonRegionArrivalsTimesTracked() {
		if (getPersonRegionArrivalTrackingPolicy() != TimeTrackingPolicy.TRACK_TIME) {
			throw new ContractException(RegionError.REGION_ARRIVAL_TIMES_NOT_TRACKED);
		}
	}

	private void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void validateRegionId(final RegionId regionId) {

		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionIdExists(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private void validateregionPropertyDefinitionInitializationNotNull(final RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization) {
		if (regionPropertyDefinitionInitialization == null) {
			throw new ContractException(RegionError.NULL_REGION_PROPERTY_DEFINITION_INITIALIZATION);
		}
	}

	private void validateRegionPropertyId(final RegionPropertyId regionPropertyId) {
		if (regionPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
		if (!regionPropertyIdExists(regionPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, regionPropertyId);
		}
	}

	private void validateRegionPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private static enum PersonRegionUpdateFunctionId {
		ARRIVAL, DEPARTURE, PERSON;

	}

	private IdentifiableFunctionMap<PersonRegionUpdateEvent> personRegionUpdateFunctionMap = //
			IdentifiableFunctionMap	.builder(PersonRegionUpdateEvent.class)//
									.put(PersonRegionUpdateFunctionId.ARRIVAL, e -> e.currentRegionId())//
									.put(PersonRegionUpdateFunctionId.DEPARTURE, e -> e.previousRegionId())//
									.put(PersonRegionUpdateFunctionId.PERSON, e -> e.personId())//
									.build();//

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonRegionUpdateEvent} events. Matches on the arrival region id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li> *
	 * 
	 */
	public EventFilter<PersonRegionUpdateEvent> getEventFilterForPersonRegionUpdateEvent_ByArrivalRegion(RegionId arrivalRegionId) {
		validateRegionId(arrivalRegionId);
		return EventFilter	.builder(PersonRegionUpdateEvent.class)//
							.addFunctionValuePair(personRegionUpdateFunctionMap.get(PersonRegionUpdateFunctionId.ARRIVAL), arrivalRegionId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonRegionUpdateEvent} events. Matches on the departure region
	 * id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li> *
	 * 
	 */
	public EventFilter<PersonRegionUpdateEvent> getEventFilterForPersonRegionUpdateEvent_ByDepartureRegion(RegionId departureRegionId) {
		validateRegionId(departureRegionId);
		return EventFilter	.builder(PersonRegionUpdateEvent.class)//
							.addFunctionValuePair(personRegionUpdateFunctionMap.get(PersonRegionUpdateFunctionId.DEPARTURE), departureRegionId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonRegionUpdateEvent} events. Matches on the person id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonError.NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError.UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 * 
	 */
	public EventFilter<PersonRegionUpdateEvent> getEventFilterForPersonRegionUpdateEvent(PersonId personId) {
		validatePersonExists(personId);
		return EventFilter	.builder(PersonRegionUpdateEvent.class)//
							.addFunctionValuePair(personRegionUpdateFunctionMap.get(PersonRegionUpdateFunctionId.PERSON), personId)//
							.build();

	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link PersonRegionUpdateEvent} events. Matches on all such events.
	 * 
	 */
	public EventFilter<PersonRegionUpdateEvent> getEventFilterForPersonRegionUpdateEvent() {
		return EventFilter	.builder(PersonRegionUpdateEvent.class)//
							.build();
	}

	private static enum RegionPropertyUpdateEventFunctionId {
		PROPERTY, REGION;
	}

	private IdentifiableFunctionMap<RegionPropertyUpdateEvent> regionPropertyUpdateFunctionMap = //
			IdentifiableFunctionMap	.builder(RegionPropertyUpdateEvent.class)//
									.put(RegionPropertyUpdateEventFunctionId.PROPERTY, e -> e.regionPropertyId())//
									.put(RegionPropertyUpdateEventFunctionId.REGION, e -> e.regionId())//
									.build();//

	/**
	 * Returns an event filter used to subscribe to
	 * {@link RegionPropertyUpdateEvent} events. Matches on the region id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PropertyError.NULL_PROPERTY_ID} if the region
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError.UNKNOWN_PROPERTY_ID} if the
	 *             region property id is not known</li>
	 * 
	 * 
	 */
	public EventFilter<RegionPropertyUpdateEvent> getEventFilterForRegionPropertyUpdateEvent(RegionPropertyId regionPropertyId) {
		validateRegionPropertyId(regionPropertyId);
		return EventFilter	.builder(RegionPropertyUpdateEvent.class)//
							.addFunctionValuePair(regionPropertyUpdateFunctionMap.get(RegionPropertyUpdateEventFunctionId.PROPERTY), regionPropertyId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link RegionPropertyUpdateEvent} events. Matches on the region property
	 * id and region id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain RegionError.NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError.UNKNOWN_REGION_ID} if the region
	 *             id is not known</li>
	 *             <li>{@linkplain PropertyError.NULL_PROPERTY_ID} if the region
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError.UNKNOWN_PROPERTY_ID} if the
	 *             region property id is not known</li>
	 * 
	 */
	public EventFilter<RegionPropertyUpdateEvent> getEventFilterForRegionPropertyUpdateEvent(RegionId regionId, RegionPropertyId regionPropertyId) {
		validateRegionId(regionId);
		validateRegionPropertyId(regionPropertyId);
		return EventFilter	.builder(RegionPropertyUpdateEvent.class)//
							.addFunctionValuePair(regionPropertyUpdateFunctionMap.get(RegionPropertyUpdateEventFunctionId.PROPERTY), regionPropertyId)//
							.addFunctionValuePair(regionPropertyUpdateFunctionMap.get(RegionPropertyUpdateEventFunctionId.REGION), regionId)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link RegionPropertyUpdateEvent} events. Matches all such events.
	 * 
	 */
	public EventFilter<RegionPropertyUpdateEvent> getEventFilterForRegionPropertyUpdateEvent() {
		return EventFilter	.builder(RegionPropertyUpdateEvent.class)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to {@link RegionAdditionEvent}
	 * events. Matches all such events.
	 * 
	 */
	public EventFilter<RegionAdditionEvent> getEventFilterForRegionAdditionEvent() {
		return EventFilter	.builder(RegionAdditionEvent.class)//
							.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link RegionPropertyDefinitionEvent} events. Matches all such events.
	 * 
	 */
	public EventFilter<RegionPropertyDefinitionEvent> getEventFilterForRegionPropertyDefinitionEvent() {
		return EventFilter	.builder(RegionPropertyDefinitionEvent.class)//
							.build();
	}

}
