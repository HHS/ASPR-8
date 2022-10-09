package temp.filtereventtests.plugins.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.EventFilter;
import nucleus.EventLabel;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.personproperties.support.PersonPropertyId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.stochastics.StochasticsDataManager;
import temp.filtereventtests.PersonPropertyIdentifier;

public class PropertyObserver {

	boolean useEventFilters;
	private ActorContext actorContext;
	private PersonPropertiesDataManager personPropertiesDataManager;
	private RandomGenerator randomGenerator;

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		List<PersonId> people = peopleDataManager.getPeople();

		RegionsDataManager regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();

		ModelDataManager modelDataManager = actorContext.getDataManager(ModelDataManager.class);
		useEventFilters = modelDataManager.getUseEventFilters();

		if (randomGenerator.nextDouble() < 0.1) {
			subscribeToAll();
		}

		for (PersonPropertyIdentifier personPropertyIdentifier : PersonPropertyIdentifier.values()) {
			if (randomGenerator.nextDouble() < 0.15) {
				subscribeToProperty(personPropertyIdentifier);
			}
		}
		
		for (PersonPropertyIdentifier personPropertyIdentifier : PersonPropertyIdentifier.values()) {
			if (randomGenerator.nextDouble() < 0.15) {
				PersonId personId = people.get(randomGenerator.nextInt(people.size()));
				subscribeToPersonAndProperty(personId,personPropertyIdentifier);
			}
		}
		
		for (PersonPropertyIdentifier personPropertyIdentifier : PersonPropertyIdentifier.values()) {
			if (randomGenerator.nextDouble() < 0.15) {
				RegionId regionId = regionIds.get(randomGenerator.nextInt(regionIds.size()));
				subscribeToRegionAndProperty(regionId,personPropertyIdentifier);
			}
		}		

	}
	
	private void subscribeToRegionAndProperty(RegionId regionId, PersonPropertyId personPropertyId) {
		if (useEventFilters) {
			EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(regionId, PersonPropertyIdentifier.PROP_BOOLEAN_16);
			actorContext.subscribe(eventFilter, this::handlePersonPropertyUpdateEvent);
		} else {
			EventLabel<PersonPropertyUpdateEvent> eventLabel = PersonPropertyUpdateEvent.getEventLabelByRegionAndProperty(actorContext, regionId, PersonPropertyIdentifier.PROP_BOOLEAN_16);
			actorContext.subscribe(eventLabel, this::handlePersonPropertyUpdateEvent);
		}
	}

	private void subscribeToPersonAndProperty(PersonId personId, PersonPropertyId personPropertyId) {
		if (useEventFilters) {
			// by person and property			
			EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(personId, personPropertyId);
			actorContext.subscribe(eventFilter, this::handlePersonPropertyUpdateEvent);

		} else {
			// by person and property			
			EventLabel<PersonPropertyUpdateEvent> eventLabel = PersonPropertyUpdateEvent.getEventLabelByPersonAndProperty(actorContext, personId, personPropertyId);
			actorContext.subscribe(eventLabel, this::handlePersonPropertyUpdateEvent);

		}
	}

	private void subscribeToAll() {
		if (useEventFilters) {

			EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent();
			actorContext.subscribe(eventFilter, this::handlePersonPropertyUpdateEvent);

		} else {
			actorContext.subscribe(PersonPropertyUpdateEvent.class, this::handlePersonPropertyUpdateEvent);
		}
	}

	private void subscribeToProperty(PersonPropertyId personPropertyId) {
		if (useEventFilters) {
			// by property
			EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(personPropertyId);
			actorContext.subscribe(eventFilter, this::handlePersonPropertyUpdateEvent);

		} else {
			// by property
			EventLabel<PersonPropertyUpdateEvent> eventLabel = PersonPropertyUpdateEvent.getEventLabelByProperty(actorContext, personPropertyId);
			actorContext.subscribe(eventLabel, this::handlePersonPropertyUpdateEvent);
		}
	}

	private void handlePersonPropertyUpdateEvent(ActorContext actorContext, PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		//System.out.println(actorContext.getActorId().getValue()+"\t"+personPropertyUpdateEvent.id);
//		double value;
//		do {
//			value = randomGenerator.nextDouble();
//		} while (value > 0.2);
	}

}
