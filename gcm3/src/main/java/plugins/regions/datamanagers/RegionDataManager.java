package plugins.regions.datamanagers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.util.ContractException;
import plugins.people.PersonDataManager;
import plugins.people.events.BulkPersonCreationObservationEvent;
import plugins.people.events.PersonCreationObservationEvent;
import plugins.people.events.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.RegionPlugin;
import plugins.regions.RegionPluginData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.PropertyValueRecord;
import plugins.util.properties.TimeTrackingPolicy;
import plugins.util.properties.arraycontainers.DoubleValueContainer;
import plugins.util.properties.arraycontainers.IntValueContainer;

/**
 * Mutable data manager that backs the {@linkplain RegionDataView}. This data
 * manager is for internal use by the {@link RegionPlugin} and should not be
 * published.
 * 
 * All regions and region properties are established during construction and
 * cannot be changed. Region property values are mutable. Limited validation of
 * inputs are performed and mutation methods have invocation ordering
 * requirements.
 * 
 * @author Shawn Hatch
 *
 */
public final class RegionDataManager extends DataManager {
	private DataManagerContext dataManagerContext;

	private final RegionPluginData regionPluginData;

	/**
	 * Creates a Region Data Manager from the given resolver context.
	 * Preconditions: The context must be a valid and non-null.
	 */
	public RegionDataManager(RegionPluginData regionPluginData) {
		if (regionPluginData == null) {
			throw new ContractException(RegionError.NULL_REGION_PLUGIN_DATA);
		}
		this.regionPluginData = regionPluginData;
	}

	private PersonDataManager personDataManager;

	/**
	 * 
	 * <P>
	 * Initializes all event labelers defined by
	 * {@linkplain RegionPropertyChangeObservationEvent} and
	 * {@linkplain PersonRegionChangeObservationEvent}
	 * </P>
	 * 
	 * 
	 * <P>
	 * Subscribes the following events:
	 * <ul>
	 * 
	 * <li>{@linkplain PersonCreationObservationEvent}<blockquote> Sets the
	 * person's initial region in the {@linkplain RegionLocationDataView} from
	 * the region reference in the auxiliary data of the event.
	 * 
	 * <BR>
	 * <BR>
	 * Throws {@link ContractException}
	 * <ul> 
	 * <li>{@linkplain PersonError.UNKNOWN_PERSON_ID} if the person does not
	 * exist</li>
	 * <li>{@linkplain RegionError#NULL_REGION_ID} if no region data was
	 * included in the event</li>
	 * <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region in the event
	 * is unknown</li>
	 * <li>{@linkplain RegionError#DUPLICATE_PERSON_ADDITION} if the person was
	 * previously added</li>
	 * </ul>
	 * 
	 * </blockquote></li>
	 * 
	 * <li>{@linkplain BulkPersonCreationObservationEvent}<blockquote> Sets each
	 * person's initial region in the {@linkplain RegionLocationDataView} from
	 * the region references in the auxiliary data of the event.
	 * 
	 * <BR>
	 * <BR>
	 * Throws {@link ContractException}
	 * <ul> 
	 * <li>{@linkplain PersonError.UNKNOWN_PERSON_ID} if the person does not
	 * exist</li>
	 * 
	 * <li>{@linkplain RegionError#NULL_REGION_ID} if no region data was
	 * included in the for some person in event</li>
	 * 
	 * <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region is unknown
	 * for some person in the event</li>
	 * </ul>
	 * 
	 * <li>{@linkplain RegionError#DUPLICATE_PERSON_ADDITION} if a person was
	 * previously added</li>
	 * 
	 * 
	 * </blockquote></li>
	 * 
	 * <li>{@linkplain PersonImminentRemovalObservationEvent}<blockquote>
	 * Removes the region assignment data for the person from the
	 * {@linkplain RegionDataView} <BR>
	 * <BR>
	 * Throws {@linkplain ContractException}
	 * <ul>
	 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
	 * unknown</li>
	 * <li>{@linkplain RegionError#DUPLICATE_PERSON_REMOVAL} if the person was
	 * previously removed. Note : this exception will be delayed until the
	 * person is finally removed and cannot be found due to a previous
	 * removal</li>
	 * 
	 * 
	 * </ul>
	 * 
	 * </blockquote></li>
	 * <ul>
	 * </p>
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public void init(DataManagerContext dataManagerContext) {

		super.init(dataManagerContext);

		this.dataManagerContext = dataManagerContext;

		personDataManager = dataManagerContext.getDataManager(PersonDataManager.class).get();

		/*
		 * By setting the default value to 0, we are allowing the container to
		 * grow without having to set values in its array. HOWEVER, THIS IMPLIES
		 * THAT REGIONS MUST BE CONVERTED TO INTEGER VALUES STARTING AT ONE, NOT
		 * ZERO.
		 *
		 * The same holds true for compartments.
		 */
		regionValues = new IntValueContainer(0);

		regionArrivalTrackingPolicy = regionPluginData.getPersonRegionArrivalTrackingPolicy();
		if (regionArrivalTrackingPolicy == TimeTrackingPolicy.TRACK_TIME) {
			regionArrivalTimes = new DoubleValueContainer(0);
		}

		final Set<RegionId> regionIds = regionPluginData.getRegionIds();
		for (final RegionId regionId : regionIds) {
			regionPopulationRecordMap.put(regionId, new PopulationRecord());
		}

		int index = 1;
		for (final RegionId regionId : regionIds) {
			regionToIndexMap.put(regionId, index++);
		}

		indexToRegionMap = new RegionId[regionIds.size() + 1];

		index = 1;
		for (final RegionId regionId : regionIds) {
			indexToRegionMap[index++] = regionId;
		}

		for (RegionPropertyId regionPropertyId : regionPluginData.getRegionPropertyIds()) {
			PropertyDefinition propertyDefinition = regionPluginData.getRegionPropertyDefinition(regionPropertyId);
			regionPropertyIds.add(regionPropertyId);
			regionPropertyDefinitions.put(regionPropertyId, propertyDefinition);
		}

		for (final RegionId regionId : regionIds) {
			Map<RegionPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
			regionPropertyMap.put(regionId, map);
			for (RegionPropertyId regionPropertyId : regionPropertyIds) {
				final Object regionPropertyValue = regionPluginData.getRegionPropertyValue(regionId, regionPropertyId);
				PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
				propertyValueRecord.setPropertyValue(regionPropertyValue);
				map.put(regionPropertyId,propertyValueRecord);
			}
		}

		dataManagerContext.subscribe(PersonCreationObservationEvent.class, this::handlePersonCreationObservationEvent);
		dataManagerContext.subscribe(BulkPersonCreationObservationEvent.class, this::handleBulkPersonCreationObservationEvent);
		dataManagerContext.subscribe(PersonImminentRemovalObservationEvent.class, this::handlePersonImminentRemovalObservationEvent);

		dataManagerContext.addEventLabeler(RegionPropertyChangeObservationEvent.getEventLabelerForProperty());
		dataManagerContext.addEventLabeler(RegionPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty());
		dataManagerContext.addEventLabeler(PersonRegionChangeObservationEvent.getEventLabelerForArrivalRegion());
		dataManagerContext.addEventLabeler(PersonRegionChangeObservationEvent.getEventLabelerForDepartureRegion());
		dataManagerContext.addEventLabeler(PersonRegionChangeObservationEvent.getEventLabelerForPerson());
	}

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	/**
	 * Expands the capacity of data structures to hold people by the given
	 * count. Used to more efficiently prepare for bulk population additions.
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

	private Map<RegionId, Map<RegionPropertyId, PropertyValueRecord>> regionPropertyMap = new LinkedHashMap<>();

	private Set<RegionPropertyId> regionPropertyIds = new LinkedHashSet<>();

	private Map<RegionPropertyId, PropertyDefinition> regionPropertyDefinitions = new LinkedHashMap<>();

	/**
	 * Returns the property definition for the given {@link RegionPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID} if the
	 *             region property id is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_PROPERTY_ID} if
	 *             the region property id is unknown
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
		Set<T> result = new LinkedHashSet<>(regionPropertyDefinitions.keySet().size());
		for (RegionPropertyId regionPropertyId : regionPropertyDefinitions.keySet()) {
			result.add((T) regionPropertyId);
		}
		return result;
	}

	/**
	 * Return true if and only if the given {@link RegionId} exits. Null
	 * tolerant.
	 */
	public boolean regionIdExists(RegionId regionId) {
		return regionPropertyMap.containsKey(regionId);
	}

	/**
	 * Returns true if and only if the given {@link RegionPropertyId} exists.
	 * Tolerates nulls.
	 */
	public boolean regionPropertyIdExists(RegionPropertyId regionPropertyId) {
		return regionPropertyDefinitions.containsKey(regionPropertyId);
	}

	/**
	 * Returns the set of {@link RegionId} values that are defined by the
	 * {@link RegionPluginData}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionId> Set<T> getRegionIds() {
		Set<T> result = new LinkedHashSet<>(regionPropertyMap.size());
		for (RegionId regionId : regionPropertyMap.keySet()) {
			result.add((T) regionId);
		}
		return result;

	}

	/**
	 * Updates the region's property value and time. Generates a corresponding
	 * {@linkplain RegionPropertyChangeObservationEvent}
	 * 
	 * Throws {@link ContractException}
	 *
	 * <li>{@link RegionError#NULL_REGION_ID} if the region id is null
	 * <li>{@link RegionError#UNKNOWN_REGION_ID} if the region id is unknown
	 * <li>{@link RegionError#NULL_REGION_PROPERTY_ID} if the property id is
	 * null
	 * <li>{@link RegionError#UNKNOWN_REGION_PROPERTY_ID} if the property id is
	 * unknown
	 * <li>{@link RegionError#NULL_REGION_PROPERTY_VALUE} if the value is null
	 * <li>{@link PropertyError#INCOMPATIBLE_VALUE} if the value is incompatible
	 * with the defined type for the property
	 * <li>{@link PropertyError#IMMUTABLE_VALUE} if the property has been
	 * defined as immutable
	 * 
	 * </blockquote></li>
	 */

	public void setRegionPropertyValue(RegionId regionId, RegionPropertyId regionPropertyId, Object regionPropertyValue) {

		validateRegionId(regionId);
		validateRegionPropertyId(regionPropertyId);
		validateRegionPropertyValueNotNull(regionPropertyValue);
		final PropertyDefinition propertyDefinition = getRegionPropertyDefinition(regionPropertyId);
		validateValueCompatibility(regionPropertyId, propertyDefinition, regionPropertyValue);
		validatePropertyMutability(propertyDefinition);

		final Object previousPropertyValue = getRegionPropertyValue(regionId, regionPropertyId);
		regionPropertyMap.get(regionId).get(regionPropertyId).setPropertyValue(regionPropertyValue);
		dataManagerContext.releaseEvent(new RegionPropertyChangeObservationEvent(regionId, regionPropertyId, previousPropertyValue, regionPropertyValue));

	}

	private void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void validateRegionPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(RegionError.NULL_REGION_PROPERTY_VALUE);
		}
	}

	/**
	 * Returns the value of the region property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li>
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID} if the
	 *             region property id is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_PROPERTY_ID} if
	 *             the region property id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getRegionPropertyValue(RegionId regionId, RegionPropertyId regionPropertyId) {
		validateRegionId(regionId);
		validateRegionPropertyId(regionPropertyId);
		return (T) regionPropertyMap.get(regionId).get(regionPropertyId).getValue();
	}

	/**
	 * Returns the time when the of the region property was last assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li>
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID} if the
	 *             region property id is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_PROPERTY_ID} if
	 *             the region property id is unknown</li>
	 */
	public double getRegionPropertyTime(RegionId regionId, RegionPropertyId regionPropertyId) {
		validateRegionId(regionId);
		validateRegionPropertyId(regionPropertyId);
		return regionPropertyMap.get(regionId).get(regionPropertyId).getAssignmentTime();
	}

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
	private RegionId[] indexToRegionMap;

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
	 * Returns as a List the person identifiers of the people in the given
	 * region. List elements are unique.
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain RegionError#NULL_REGION_ID} if
	 *                          the c id is null
	 *                          <li>{@linkplain CompartmentError#UNKNOWN_REGION_ID}
	 *                          if the region id is not known
	 */
	public List<PersonId> getPeopleInRegion(final RegionId regionId) {
		validateRegionId(regionId);

		int targetRegionIndex = regionToIndexMap.get(regionId).intValue();

		List<PersonId> result = new ArrayList<>();

		int n = regionValues.size();
		for (int personIndex = 0; personIndex < n; personIndex++) {
			final int regionIndex = regionValues.getValueAsInt(personIndex);
			/*
			 * a region index of zero will not match any valid region,
			 * indicating that person does not exist
			 */
			if (targetRegionIndex == regionIndex) {
				PersonId personId = personDataManager.getBoxedPersonId(personIndex).get();
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
	 *                          if the compartment id is unknown
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionId> T getPersonRegion(final PersonId personId) {
		validatePersonExists(personId);
		final int r = regionValues.getValueAsInt(personId.getValue());
		return (T) indexToRegionMap[r];
	}

	/**
	 * Returns the time when then person arrived at their current region.
	 * 
	 * @throwsContractException
	 *                          <li>{@linkplain PersonError#NULL_PERSON_ID} if
	 *                          the person id is null
	 *                          <li>{@linkplain PersonError#UNKNOWN_PERSON_ID}
	 *                          if the compartment id is unknown
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

	/*
	 * Precondition : person and region exist
	 */
	private void validatePersonNotInRegion(final DataManagerContext dataManagerContext, final PersonId personId, final RegionId regionId) {
		final RegionId currentRegionId = getPersonRegion(personId);
		if (currentRegionId.equals(regionId)) {
			throw new ContractException(RegionError.SAME_REGION, regionId);
		}
	}

	/**
	 * 
	 * Updates the person's current region and region arrival time. Generates a
	 * corresponding {@linkplain PersonRegionChangeObservationEvent}
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
	 * <li>{@link RegionError#SAME_REGION} if the region id is currently
	 * assigned to the person</li>
	 */

	public void setPersonRegion(final PersonId personId, final RegionId regionId) {

		validatePersonExists(personId);
		validateRegionId(regionId);
		validatePersonNotInRegion(dataManagerContext, personId, regionId);

		/*
		 * Retrieve the int value that represents the current region of the
		 * person
		 */
		int regionIndex = regionValues.getValueAsInt(personId.getValue());
		RegionId oldRegionId = indexToRegionMap[regionIndex];
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

		dataManagerContext.releaseEvent(new PersonRegionChangeObservationEvent(personId, oldRegionId, regionId));

	}

	private void validateRegionPropertyId(final RegionPropertyId regionPropertyId) {
		if (regionPropertyId == null) {
			throw new ContractException(RegionError.NULL_REGION_PROPERTY_ID);
		}
		if (!regionPropertyIdExists(regionPropertyId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_PROPERTY_ID, regionPropertyId);
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

	private void validatePersonExists(final PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}

		if (!personDataManager.personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void validatePersonRegionArrivalsTimesTracked() {
		if (getPersonRegionArrivalTrackingPolicy() != TimeTrackingPolicy.TRACK_TIME) {
			throw new ContractException(RegionError.REGION_ARRIVAL_TIMES_NOT_TRACKED);
		}
	}

	private void validatePersonNotContained(PersonId personId) {

		int regionIndex = regionValues.getValueAsInt(personId.getValue());
		if (regionIndex != 0) {
			throw new ContractException(RegionError.DUPLICATE_PERSON_ADDITION);
		}
	}

	private void validatePersonContained(PersonId personId) {

		int regionIndex = regionValues.getValueAsInt(personId.getValue());
		if (regionIndex == 0) {
			throw new ContractException(RegionError.DUPLICATE_PERSON_REMOVAL);
		}
	}

	private void handlePersonCreationObservationEvent(final DataManagerContext dataManagerContext, final PersonCreationObservationEvent personCreationObservationEvent) {
		PersonConstructionData personConstructionData = personCreationObservationEvent.getPersonConstructionData();
		RegionId regionId = personConstructionData.getValue(RegionId.class).orElse(null);
		validateRegionId(regionId);
		PersonId personId = personCreationObservationEvent.getPersonId();

		validatePersonExists(personId);
		validateRegionId(regionId);
		validatePersonNotContained(personId);

		/*
		 * Update the population count of the new region
		 */

		final PopulationRecord populationRecord = regionPopulationRecordMap.get(regionId);
		populationRecord.populationCount++;
		populationRecord.assignmentTime = dataManagerContext.getTime();

		Integer regionIndex = regionToIndexMap.get(regionId).intValue();
		regionValues.setIntValue(personId.getValue(), regionIndex);

		if (regionArrivalTimes != null) {
			regionArrivalTimes.setValue(personId.getValue(), dataManagerContext.getTime());
		}
	}

	private void handleBulkPersonCreationObservationEvent(final DataManagerContext dataManagerContext, final BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		BulkPersonConstructionData bulkPersonConstructionData = bulkPersonCreationObservationEvent.getBulkPersonConstructionData();
		List<PersonConstructionData> personConstructionDatas = bulkPersonConstructionData.getPersonConstructionDatas();
		for (PersonConstructionData personConstructionData : personConstructionDatas) {
			RegionId regionId = personConstructionData.getValue(RegionId.class).orElse(null);
			validateRegionId(regionId);
		}

		PersonId personId = bulkPersonCreationObservationEvent.getPersonId();
		validatePersonExists(personId);
		int pId = personId.getValue();

		for (PersonConstructionData personConstructionData : personConstructionDatas) {
			RegionId regionId = personConstructionData.getValue(RegionId.class).get();
			Optional<PersonId> optionalBoxedPersonId = personDataManager.getBoxedPersonId(pId);
			if (!optionalBoxedPersonId.isPresent()) {
				throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
			}
			PersonId boxedPersonId = optionalBoxedPersonId.get();
			validatePersonNotContained(boxedPersonId);

			final PopulationRecord populationRecord = regionPopulationRecordMap.get(regionId);
			populationRecord.populationCount++;
			populationRecord.assignmentTime = dataManagerContext.getTime();

			Integer regionIndex = regionToIndexMap.get(regionId).intValue();
			regionValues.setIntValue(boxedPersonId.getValue(), regionIndex);

			if (regionArrivalTimes != null) {
				regionArrivalTimes.setValue(boxedPersonId.getValue(), dataManagerContext.getTime());
			}
			pId++;
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
	private void handlePersonImminentRemovalObservationEvent(final DataManagerContext dataManagerContext, final PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		PersonId personId = personImminentRemovalObservationEvent.getPersonId();
		validatePersonExists(personId);
		dataManagerContext.addPlan((context) -> removePerson(personId), dataManagerContext.getTime());
	}

	private void removePerson(final PersonId personId) {
		validatePersonContained(personId);
		final int regionIndex = regionValues.getValueAsInt(personId.getValue());
		final RegionId oldRegionId = indexToRegionMap[regionIndex];
		final PopulationRecord populationRecord = regionPopulationRecordMap.get(oldRegionId);
		populationRecord.populationCount--;
		populationRecord.assignmentTime = dataManagerContext.getTime();
		regionValues.setIntValue(personId.getValue(), 0);
	}

}
