package temp.filtereventtests.plugins.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.stochastics.StochasticsDataManager;
import temp.filtereventtests.PersonPropertyIdentifier;

public class PropertyChanger {

	private PersonPropertiesDataManager personPropertiesDataManager;
	private RandomGenerator randomGenerator;
	private List<PersonId> people;
	private List<PersonPropertyIdentifier> personPropertyIds;

	public void init(ActorContext actorContext) {
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		people = peopleDataManager.getPeople();
		ModelDataManager modelDataManager = actorContext.getDataManager(ModelDataManager.class);
		int eventCount = modelDataManager.getEventCount();
        personPropertyIds = new ArrayList<>(personPropertiesDataManager.getPersonPropertyIds());
		for (int i = 0; i < eventCount; i++) {
			actorContext.addPlan(this::updatePersonProperty, i + 1);
		}

	}

	private void updatePersonProperty(ActorContext actorContext) {
		PersonId personId = people.get(randomGenerator.nextInt(people.size()));
		PersonPropertyIdentifier personPropertyId = personPropertyIds.get(randomGenerator.nextInt(personPropertyIds.size()));
		Object propertyValue = personPropertyId.getRandomPropertyValue(randomGenerator);
		personPropertiesDataManager.setPersonPropertyValue(personId, personPropertyId, propertyValue);
	}
}
