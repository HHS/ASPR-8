package lesson.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.PersonProperty;
import lesson.plugins.model.Resource;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.stochastics.StochasticsDataManager;

public class DiseaseManager {
	private ActorContext actorContext;
	private double antiviralCoverageTime;
	private double antiviralSuccessRate;
	private double hospitalSuccessWithAntiviral;
	private double hospitalSuccessWithoutAntiviral;
	private double hospitalStayDurationMin;
	private double hospitalStayDurationMax;

	private PersonPropertiesDataManager personPropertiesDataManager;
	private ResourcesDataManager resourcesDataManager;
	private RegionsDataManager regionsDataManager;
	private RandomGenerator randomGenerator;

	public void init(ActorContext actorContext) {
		actorContext.addPlan(this::startTreatment, 0);
	}

	private void assessHospitalization(PersonId personId) {
		boolean treatedWithAntiViral = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.TREATED_WITH_ANTIVIRAL);
		double probabilityOfSuccess;
		if (treatedWithAntiViral) {
			probabilityOfSuccess = hospitalSuccessWithAntiviral;
		} else {
			probabilityOfSuccess = hospitalSuccessWithoutAntiviral;
		}
		if (randomGenerator.nextDouble() < probabilityOfSuccess) {
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.IMMUNE, true);
		} else {
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOSPITAL, true);
		}
		resourcesDataManager.transferResourceFromPersonToRegion(Resource.HOSPITAL_BED, personId, 1L);
	}

	private void hospitalizePerson(PersonId personId) {
		RegionId regionId = regionsDataManager.getPersonRegion(personId);
		long availableHospitalBeds = resourcesDataManager.getRegionResourceLevel(regionId, Resource.HOSPITAL_BED);
		if (availableHospitalBeds > 0) {
			resourcesDataManager.transferResourceToPersonFromRegion(Resource.HOSPITAL_BED, personId, 1L);
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.HOSPITALIZED, true);
			double hospitalizationDuration = (hospitalStayDurationMax - hospitalStayDurationMin) * randomGenerator.nextDouble() + hospitalStayDurationMin;
			actorContext.addPlan((c) -> assessHospitalization(personId), actorContext.getTime() + hospitalizationDuration);
		}else {
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOME, true);
		}
	}

	private void assessAntiviralTreatment(PersonId personId) {
		resourcesDataManager.removeResourceFromPerson(Resource.ANTI_VIRAL_MED, personId, 1L);
		if (randomGenerator.nextDouble() < antiviralSuccessRate) {
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.IMMUNE, true);
		} else {
			hospitalizePerson(personId);
		}
	}

	private void treatWithAntiviral(PersonId personId) {
		RegionId regionId = regionsDataManager.getPersonRegion(personId);
		long regionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, Resource.ANTI_VIRAL_MED);
		if (regionResourceLevel > 0) {
			resourcesDataManager.transferResourceToPersonFromRegion(Resource.ANTI_VIRAL_MED, personId, 1L);
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.TREATED_WITH_ANTIVIRAL, true);
			actorContext.addPlan((c) -> assessAntiviralTreatment(personId), actorContext.getTime() + antiviralCoverageTime);
		} else {
			hospitalizePerson(personId);
		}
	}

	private void startTreatment(ActorContext actorContext) {
		this.actorContext = actorContext;
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

		double probabilityOfInfection = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION);
		double maximumSymptomOnsetTime = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MAXIMUM_SYMPTOM_ONSET_TIME);

		antiviralCoverageTime = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.ANTIVIRAL_COVERAGE_TIME);
		antiviralSuccessRate = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.ANTIVIRAL_SUCCESS_RATE);
		hospitalSuccessWithAntiviral = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITH_ANTIVIRAL);
		hospitalSuccessWithoutAntiviral = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITHOUT_ANTIVIRAL);
		hospitalStayDurationMin = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_STAY_DURATION_MIN);
		hospitalStayDurationMax = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_STAY_DURATION_MAX);

		List<PersonId> susceptiblePeople = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.IMMUNE, false);
		for (PersonId personId : susceptiblePeople) {						
			if (randomGenerator.nextDouble() < probabilityOfInfection) {
				double symptomOnsetTime = randomGenerator.nextDouble() * maximumSymptomOnsetTime;
				personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.INFECTED, true);
				actorContext.addPlan((c) -> this.treatWithAntiviral(personId), symptomOnsetTime);				
			}
		}

	}
}
