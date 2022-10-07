package temp.filtereventtests.plugins.model;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.EventFilter;
import nucleus.EventLabel;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.stochastics.StochasticsDataManager;
import temp.filtereventtests.PersonPropertyIdentifier;

public class PropertyObserver {
	private RandomGenerator randomGenerator;
	

	public void init(ActorContext actorContext) {
		PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();

		ModelDataManager modelDataManager = actorContext.getDataManager(ModelDataManager.class);
		boolean useEventFilters = modelDataManager.getUseEventFilters();

		
//		if (useEventFilters) {
//			EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilter();
//			actorContext.subscribe(eventFilter, this::handlePersonPropertyUpdateEvent);
//		} else {
//			actorContext.subscribe(PersonPropertyUpdateEvent.class, this::handlePersonPropertyUpdateEvent);
//		}
		
		if (useEventFilters) {
			EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterByProperty(PersonPropertyIdentifier.PROP_BOOLEAN_16);
			actorContext.subscribe(eventFilter, this::handlePersonPropertyUpdateEvent);
		} else {
			EventLabel<PersonPropertyUpdateEvent> eventLabel = PersonPropertyUpdateEvent.getEventLabelByProperty(actorContext, PersonPropertyIdentifier.PROP_BOOLEAN_16);
			actorContext.subscribe(eventLabel, this::handlePersonPropertyUpdateEvent);
		}
		
		
	}

	private void handlePersonPropertyUpdateEvent(ActorContext actorContext, PersonPropertyUpdateEvent personPropertyUpdateEvent) {
		double value;
		do {
			value = randomGenerator.nextDouble();
		} while (value > 0.2);
	}

}
