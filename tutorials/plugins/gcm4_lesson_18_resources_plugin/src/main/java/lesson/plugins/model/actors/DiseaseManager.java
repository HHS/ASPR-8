package lesson.plugins.model.actors;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.PersonProperty;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.stochastics.StochasticsDataManager;

public class ExposureManager {

	public void init(ActorContext actorContext) {
		actorContext.addPlan(this::startTreatment, 0);		
	}
	
	private void treatWithAntiviral(PersonId personId) {
		
	}
	
	private void startTreatment(ActorContext actorContext) {
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
		PersonPropertiesDataManager personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		
		double probabilityOfInfection = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION);
		double maximumSymptomOnsetTime = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MAXIMUM_SYMPTOM_ONSET_TIME);

		double antiviralCoverageTime = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.ANTIVIRAL_COVERAGE_TIME);
		double antiviralSuccessRate = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.ANTIVIRAL_SUCCESS_RATE);
		double hospitalSuccessWithAntiviral = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITH_ANTIVIRAL);
		double hospitalSuccessWithoutAntiviral = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITHOUT_ANTIVIRAL);
		

		List<PersonId> susceptiblePeople = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.IMMUNE, false);
		for(PersonId personId : susceptiblePeople) {
			if(randomGenerator.nextDouble()<probabilityOfInfection) {
				double symptomOnsetTime = randomGenerator.nextDouble()*maximumSymptomOnsetTime;				
				actorContext.addPlan((c)->this.treatWithAntiviral(personId), symptomOnsetTime);
			}
		}
			
	}
}
