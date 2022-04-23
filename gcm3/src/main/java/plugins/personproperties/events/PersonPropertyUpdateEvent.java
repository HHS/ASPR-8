package plugins.personproperties.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesDataManager;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;

/**
 * An observation event indicating that a person's property assignment has
 * changed.
 *
 * @author Shawn Hatch
 *
 */

@Immutable
public class PersonPropertyUpdateEvent implements Event {

	private final PersonId personId;
	private final PersonPropertyId personPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	/**
	 * Creates this event from valid, non-null inputs
	 * 
	 */
	public PersonPropertyUpdateEvent(final PersonId personId, final PersonPropertyId personPropertyId, final Object previousPropertyValue, final Object currentPropertyValue) {
		super();
		this.personId = personId;
		this.personPropertyId = personPropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
	}

	/**
	 * Returns the current property value used to construct this event
	 */
	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
	}

	/**
	 * Returns the person property id used to construct this event
	 */
	public PersonPropertyId getPersonPropertyId() {
		return personPropertyId;
	}

	/**
	 * Returns the person id used to construct this event
	 */
	public PersonId getPersonId() {
		return personId;
	}

	/**
	 * Returns the previous property value used to construct this event
	 */
	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	/**
	 * Returns this event in the form:
	 * 
	 * "PersonPropertyUpdateEvent [personId=" + personId + ", personPropertyId="
	 * + personPropertyId + ", previousPropertyValue=" + previousPropertyValue +
	 * ", currentPropertyValue=" + currentPropertyValue + "]";
	 */
	@Override
	public String toString() {
		return "PersonPropertyUpdateEvent [personId=" + personId + ", personPropertyId=" + personPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue="
				+ currentPropertyValue + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		PERSON_PROPERTY, PROPERTY, REGION_PROPERTY
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonPropertyUpdateEvent} events. Matches on person id and person
	 * property id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonError#NULL_PERSON_ID} if the person id
	 *             is null</li>
	 *             <li>{@linkplain PersonError#UNKNOWN_PERSON_ID} if the person
	 *             id is not known</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is not known</li>
	 * 
	 */
	public static EventLabel<PersonPropertyUpdateEvent> getEventLabelByPersonAndProperty(SimulationContext simulationContext, PersonId personId, PersonPropertyId personPropertyId) {
		validatePersonPropertyId(simulationContext, personPropertyId);
		validatePersonId(simulationContext, personId);
		return _getEventLabelByPersonAndProperty(personId, personPropertyId);//
	}
	
	private static EventLabel<PersonPropertyUpdateEvent> _getEventLabelByPersonAndProperty(PersonId personId, PersonPropertyId personPropertyId) {
		
		return EventLabel	.builder(PersonPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PERSON_PROPERTY)//
							.addKey(personPropertyId)//
							.addKey(personId)//
							.build();//
	}

	/**
	 * Returns an event labeler for {@link PersonPropertyUpdateEvent} events
	 * that uses person id and person property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonPropertyUpdateEvent> getEventLabelerForPersonAndProperty() {
		return EventLabeler	.builder(PersonPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PERSON_PROPERTY)//
							.setLabelFunction((context, event) -> _getEventLabelByPersonAndProperty(event.getPersonId(), event.getPersonPropertyId()))//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonPropertyUpdateEvent} events. Matches on person property id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is not known</li>
	 * 
	 */
	public static EventLabel<PersonPropertyUpdateEvent> getEventLabelByProperty(SimulationContext simulationContext, PersonPropertyId personPropertyId) {
		validatePersonPropertyId(simulationContext, personPropertyId);
		return _getEventLabelByProperty(personPropertyId);//
	}
	
	private static EventLabel<PersonPropertyUpdateEvent> _getEventLabelByProperty(PersonPropertyId personPropertyId) {
		
		return EventLabel	.builder(PersonPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PROPERTY)//
							.addKey(personPropertyId)//
							.build();//

	}

	/**
	 * Returns an event labeler for {@link PersonPropertyUpdateEvent} events
	 * that uses only the person property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonPropertyUpdateEvent> getEventLabelerForProperty() {
		return EventLabeler	.builder(PersonPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.PROPERTY)//
							.setLabelFunction((context, event) -> _getEventLabelByProperty(event.getPersonPropertyId()))//
							.build();
	}

	/**
	 * Returns an event label used to subscribe to
	 * {@link PersonPropertyUpdateEvent} events. Matches on region id and person
	 * property id.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is not known</li>
	 *             <li>{@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_ID}
	 *             if the person property id is null</li>
	 *             <li>{@linkplain PersonPropertyError#UNKNOWN_PERSON_PROPERTY_ID}
	 *             if the person property id is not known</li>
	 * 
	 */
	public static EventLabel<PersonPropertyUpdateEvent> getEventLabelByRegionAndProperty(SimulationContext simulationContext, RegionId regionId, PersonPropertyId personPropertyId) {
		validatePersonPropertyId(simulationContext, personPropertyId);
		validateRegionId(simulationContext, regionId);	
		return _getEventLabelByRegionAndProperty(regionId, personPropertyId);//
	}
	
	private static EventLabel<PersonPropertyUpdateEvent> _getEventLabelByRegionAndProperty(RegionId regionId, PersonPropertyId personPropertyId) {
			
		return EventLabel	.builder(PersonPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.REGION_PROPERTY)//
							.addKey(personPropertyId)//
							.addKey(regionId)//
							.build();//
	}
	
	/**
	 * Returns an event labeler for {@link PersonPropertyUpdateEvent} events
	 * that uses the region id and person property id. Automatically added at
	 * initialization.
	 */
	public static EventLabeler<PersonPropertyUpdateEvent> getEventLabelerForRegionAndProperty(RegionsDataManager regionsDataManager) {
		return EventLabeler	.builder(PersonPropertyUpdateEvent.class)//
							.setEventLabelerId(LabelerId.REGION_PROPERTY)//
							.setLabelFunction((context, event) -> {
								RegionId regionId = regionsDataManager.getPersonRegion(event.getPersonId());
								return _getEventLabelByRegionAndProperty(regionId, event.getPersonPropertyId());
							}).build();
	}

	private static void validatePersonPropertyId(SimulationContext simulationContext, PersonPropertyId personPropertyId) {
		if (personPropertyId == null) {
			throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_ID);
		}

		if (!simulationContext.getDataManager(PersonPropertiesDataManager.class).personPropertyIdExists(personPropertyId)) {
			throw new ContractException(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID);
		}
	}

	private static void validateRegionId(SimulationContext simulationContext, RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}

		if (!simulationContext.getDataManager(RegionsDataManager.class).regionIdExists(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID);
		}
	}

	private static void validatePersonId(SimulationContext simulationContext, PersonId personId) {
		if (personId == null) {
			throw new ContractException(PersonError.NULL_PERSON_ID);
		}

		if (!simulationContext.getDataManager(PeopleDataManager.class).personExists(personId)) {
			throw new ContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	/**
	 * Returns the person property id used to create this event
	 */
	@Override
	public Object getPrimaryKeyValue() {
		return personPropertyId;
	}

}
