package lesson.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.PersonProperty;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.stochastics.StochasticsDataManager;

public class Manipulator {

	private int updateCount;

	private int maxUpdateCount;

	private List<PersonId> people;

	private RandomGenerator randomGenerator;

	private PersonPropertiesDataManager personPropertiesDataManager;


	

	public void init(final ActorContext actorContext) {
	
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		maxUpdateCount = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MAX_UPDATE_COUNT);

		PeopleDataManager peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		people = peopleDataManager.getPeople();

		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);

		actorContext.addPlan(this::updatePerson, actorContext.getTime());

	}

	private void updatePerson(ActorContext actorContext) {
		
		PersonId personId = people.get(randomGenerator.nextInt(people.size()));
		PersonProperty personProperty = PersonProperty.getRandomPersonProperty(randomGenerator);
		Object value = personProperty.getRandomPersonPropertyValue(randomGenerator);
		personPropertiesDataManager.setPersonPropertyValue(personId, personProperty, value);
		
		
		updateCount++;
		if (updateCount < maxUpdateCount) {
			actorContext.addPlan(this::updatePerson, actorContext.getTime() + 0.001);
		}
	}

}
