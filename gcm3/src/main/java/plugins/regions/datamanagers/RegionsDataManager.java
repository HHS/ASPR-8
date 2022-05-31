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
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.events.BulkPersonImminentAdditionEvent;
import plugins.people.events.PersonImminentAdditionEvent;
import plugins.people.events.PersonImminentRemovalEvent;
import plugins.people.events.PersonRemovalEvent;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.events.PersonRegionUpdateEvent;
import plugins.regions.events.RegionAdditionEvent;
import plugins.regions.events.RegionPropertyAdditionEvent;
import plugins.regions.events.RegionPropertyUpdateEvent;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.PropertyValueRecord;
import plugins.util.properties.TimeTrackingPolicy;
import plugins.util.properties.arraycontainers.DoubleValueContainer;
import plugins.util.properties.arraycontainers.IntValueContainer;
import util.errors.ContractException;
import util.time.StopwatchManager;
import util.time.Watch;

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
 * @author Shawn Hatch
 *
 */
public final class RegionsDataManager extends DataManager {
	private DataManagerContext dataManagerContext;

	private final RegionsPluginData regionsPluginData;

	/**
	 * Creates a Region Data Manager from the given resolver context.
	 * Preconditions: The context must be a valid and non-null.
	 */
	public RegionsDataManager(RegionsPluginData regionsPluginData) {
		if (regionsPluginData == null) {
			throw new ContractException(RegionError.NULL_REGION_PLUGIN_DATA);
		}
		this.regionsPluginData = regionsPluginData;
	}

	private PeopleDataManager peopleDataManager;

	/**
	 * 
	 * <P>
	 * Initializes all event labelers defined by
	 * {@linkplain RegionPropertyUpdateEvent} and
	 * {@linkplain PersonRegionUpdateEvent}
	 * </P>
	 * 
	 * 
	 * <P>
	 * Subscribes the following events:
	 * <ul>
	 * 
	 * <li>{@linkplain PersonImminentAdditionEvent}<blockquote> Sets the
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
	 * <li>{@linkplain BulkPersonImminentAdditionEvent}<blockquote> Sets each
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
	 * <li>{@linkplain PersonImminentRemovalEvent}<blockquote> Removes the
	 * region assignment data for the person from the
	 * {@linkplain RegionDataView} <BR>
	 * <BR>
	 * Throws {@linkplain ContractException}
	 * <ul>
	 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person id is
	 * unknown</li>
	 * <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person was
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
		StopwatchManager.start(Watch.REGIONS_DM_INIT);
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

		for (RegionPropertyId regionPropertyId : regionsPluginData.getRegionPropertyIds()) {
			PropertyDefinition propertyDefinition = regionsPluginData.getRegionPropertyDefinition(regionPropertyId);
			allPropertyDefinitionsHaveDefaultValues &= propertyDefinition.getDefaultValue().isPresent();
			regionPropertyIds.add(regionPropertyId);
			regionPropertyDefinitions.put(regionPropertyId, propertyDefinition);
		}

		for (final RegionId regionId : regionIds) {
			Map<RegionPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
			regionPropertyMap.put(regionId, map);
			for (RegionPropertyId regionPropertyId : regionPropertyIds) {
				final Object regionPropertyValue = regionsPluginData.getRegionPropertyValue(regionId, regionPropertyId);
				PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
				propertyValueRecord.setPropertyValue(regionPropertyValue);
				map.put(regionPropertyId, propertyValueRecord);
			}
		}

		List<PersonId> people = peopleDataManager.getPeople();
		for (PersonId personId : people) {
			RegionId regionId = regionsPluginData.getPersonRegion(personId);
			final PopulationRecord populationRecord = regionPopulationRecordMap.get(regionId);
			populationRecord.populationCount++;
			Integer regionIndex = regionToIndexMap.get(regionId).intValue();
			regionValues.setIntValue(personId.getValue(), regionIndex);
		}

		if (regionsPluginData.getPersonIds().size() > people.size()) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID, "There are people in the region plugin data that are not contained in the person data manager");
		}

		dataManagerContext.subscribe(PersonImminentAdditionEvent.class, this::handlePersonAdditionEvent);
		dataManagerContext.subscribe(BulkPersonImminentAdditionEvent.class, this::handleBulkPersonAdditionEvent);
		dataManagerContext.subscribe(PersonRemovalEvent.class, this::handlePersonRemovalEvent);

		dataManagerContext.addEventLabeler(RegionPropertyUpdateEvent.getEventLabelerForProperty());
		dataManagerContext.addEventLabeler(RegionPropertyUpdateEvent.getEventLabelerForRegionAndProperty());
		dataManagerContext.addEventLabeler(PersonRegionUpdateEvent.getEventLabelerForArrivalRegion());
		dataManagerContext.addEventLabeler(PersonRegionUpdateEvent.getEventLabelerForDepartureRegion());
		dataManagerContext.addEventLabeler(PersonRegionUpdateEvent.getEventLabelerForPerson());
		StopwatchManager.stop(Watch.REGIONS_DM_INIT);
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

	private boolean allPropertyDefinitionsHaveDefaultValues = true;

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
	 * {@link RegionsPluginData}.
	 */
	@SuppressWarnings("unchecked")
	public <T extends RegionId> Set<T> getRegionIds() {
		Set<T> result = new LinkedHashSet<>(regionPropertyMap.size());
		for (RegionId regionId : regionPropertyMap.keySet()) {
			result.add((T) regionId);
		}
		return result;

	}

	private void validateNewRegionId(final RegionId regionId) {

		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}

		if (regionIdExists(regionId)) {
			throw new ContractException(RegionError.DUPLICATE_REGION_ID, regionId);
		}
	}

	private void validateAllRegionPropertiesHaveDefaultValues() {
		if (!allPropertyDefinitionsHaveDefaultValues) {
			throw new ContractException(RegionError.REGION_ADDITION_BLOCKED);
		}
	}

	/**
	 * Adds a new region id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#DUPLICATE_REGION_ID} if the
	 *             region is already present</li>
	 *             <li>{@linkplain RegionError#REGION_ADDITION_BLOCKED} if not
	 *             all region properties have default values</li>
	 * 
	 */
	public void addRegionId(RegionId regionId) {

		validateNewRegionId(regionId);
		validateAllRegionPropertiesHaveDefaultValues();

		regionPopulationRecordMap.put(regionId, new PopulationRecord());
		regionToIndexMap.put(regionId, regionToIndexMap.size() + 1);
		indexToRegionMap.add(regionId);

		Map<RegionPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
		regionPropertyMap.put(regionId, map);
		for (RegionPropertyId regionPropertyId : regionPropertyDefinitions.keySet()) {
			PropertyDefinition propertyDefinition = regionPropertyDefinitions.get(regionPropertyId);
			final Object regionPropertyValue = propertyDefinition.getDefaultValue().get();
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
			propertyValueRecord.setPropertyValue(regionPropertyValue);
			map.put(regionPropertyId, propertyValueRecord);
		}
		dataManagerContext.releaseEvent(new	RegionAdditionEvent(regionId));

	}

	/**
	 * Updates the region's property value and time. Generates a corresponding
	 * {@linkplain RegionPropertyUpdateEvent}
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
		dataManagerContext.releaseEvent(new RegionPropertyUpdateEvent(regionId, regionPropertyId, previousPropertyValue, regionPropertyValue));

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
	 * Record for maintaining the number of people either globally or
	 * regionally. Also maintains the time when the population count was last
	 * changed. PopulationRecords are maintained to eliminate iterations over
	 * other tracking structures to answer queries about population counts.
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
	private List<RegionId> indexToRegionMap = new ArrayList<>();

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
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the c id is
	 *             null
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known
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
				PersonId personId = peopleDataManager.getBoxedPersonId(personIndex).get();
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

		validatePersonExists(personId);
		validateRegionId(regionId);

		/*
		 * Retrieve the int value that represents the current region of the
		 * person
		 */
		int regionIndex = regionValues.getValueAsInt(personId.getValue());
		RegionId oldRegionId = indexToRegionMap.get(regionIndex);
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

		dataManagerContext.releaseEvent(new PersonRegionUpdateEvent(personId, oldRegionId, regionId));

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

		if (!peopleDataManager.personExists(personId)) {
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
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void handlePersonAdditionEvent(final DataManagerContext dataManagerContext, final PersonImminentAdditionEvent personImminentAdditionEvent) {
		PersonConstructionData personConstructionData = personImminentAdditionEvent.getPersonConstructionData();
		RegionId regionId = personConstructionData.getValue(RegionId.class).orElse(null);
		validateRegionId(regionId);
		PersonId personId = personImminentAdditionEvent.getPersonId();

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

	private void handleBulkPersonAdditionEvent(final DataManagerContext dataManagerContext, final BulkPersonImminentAdditionEvent bulkPersonImminentAdditionEvent) {
		StopwatchManager.start(Watch.REGIONS_BULK);
		BulkPersonConstructionData bulkPersonConstructionData = bulkPersonImminentAdditionEvent.getBulkPersonConstructionData();
		List<PersonConstructionData> personConstructionDatas = bulkPersonConstructionData.getPersonConstructionDatas();


		PersonId personId = bulkPersonImminentAdditionEvent.getPersonId();
		validatePersonExists(personId);
		int pId = personId.getValue();

		for (PersonConstructionData personConstructionData : personConstructionDatas) {
			RegionId regionId = personConstructionData.getValue(RegionId.class).orElse(null);
			validateRegionId(regionId);
			Optional<PersonId> optionalBoxedPersonId = peopleDataManager.getBoxedPersonId(pId);
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
		StopwatchManager.stop(Watch.REGIONS_BULK);
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
		PersonId personId = personRemovalEvent.getPersonId();
		validatePersonContained(personId);
		final int regionIndex = regionValues.getValueAsInt(personId.getValue());
		final RegionId oldRegionId = indexToRegionMap.get(regionIndex);
		final PopulationRecord populationRecord = regionPopulationRecordMap.get(oldRegionId);
		populationRecord.populationCount--;
		populationRecord.assignmentTime = dataManagerContext.getTime();
		regionValues.setIntValue(personId.getValue(), 0);
	}

	private void validateNewRegionPropertyId(final RegionPropertyId regionPropertyId) {
		if (regionPropertyId == null) {
			throw new ContractException(RegionError.NULL_REGION_PROPERTY_ID);
		}
		if (regionPropertyIdExists(regionPropertyId)) {
			throw new ContractException(RegionError.DUPLICATE_REGION_PROPERTY_VALUE, regionPropertyId);
		}
	}

	private void validateNewPropertyDefinition(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
		if (propertyDefinition.getDefaultValue().isEmpty()) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT);
		}
	}

	/**
	 * Adds a new region property
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain RegionError#NULL_REGION_PROPERTY_ID} if the
	 *             region property is null</li>
	 *             <li>{@linkplain RegionError#DUPLICATE_REGION_PROPERTY_VALUE}
	 *             if the region property is already defined</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li>
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_MISSING_DEFAULT}
	 *             if the property definition does not have a default value</li>
	 */
	public void defineRegionProperty(RegionPropertyId regionPropertyId, PropertyDefinition propertyDefinition) {

		validateNewRegionPropertyId(regionPropertyId);
		validateNewPropertyDefinition(propertyDefinition);

		regionPropertyIds.add(regionPropertyId);
		regionPropertyDefinitions.put(regionPropertyId, propertyDefinition);
		for (final RegionId regionId : regionPropertyMap.keySet()) {
			Map<RegionPropertyId, PropertyValueRecord> map = regionPropertyMap.get(regionId);
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
			propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue().get());
			map.put(regionPropertyId, propertyValueRecord);
		}

		dataManagerContext.releaseEvent(new RegionPropertyAdditionEvent(regionPropertyId));
	}

}
