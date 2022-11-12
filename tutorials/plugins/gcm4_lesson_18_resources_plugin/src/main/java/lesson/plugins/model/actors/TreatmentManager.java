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
		/*
		 * Determine whether the hospitalization was a success taking into
		 * account whether the person received an antiviral treatment before
		 * entering the hospital. If the treatment was successful then mark the
		 * person as immune. Otherwise, mark the person as a hospital death.
		 */
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
		/*
		 * Try to allocate a hospital bed to the person. If the bed is
		 * available, then schedule an assessment of the treatment success.
		 * Otherwise, mark the person as a home death.
		 */
		RegionId regionId = regionsDataManager.getPersonRegion(personId);
		long availableHospitalBeds = resourcesDataManager.getRegionResourceLevel(regionId, Resource.HOSPITAL_BED);
		if (availableHospitalBeds > 0) {
			resourcesDataManager.transferResourceToPersonFromRegion(Resource.HOSPITAL_BED, personId, 1L);
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.HOSPITALIZED, true);
			double hospitalizationDuration = (hospitalStayDurationMax - hospitalStayDurationMin) * randomGenerator.nextDouble() + hospitalStayDurationMin;
			actorContext.addPlan((c) -> assessHospitalization(personId), actorContext.getTime() + hospitalizationDuration);
		} else {
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOME, true);
		}
	}

	private void assessAntiviralTreatment(PersonId personId) {
		/*
		 * Expend the antiviral resource from the person. If the antiviral
		 * succeeded, then mark the person as immune. Otherwise, hospitalize the
		 * person immediately
		 */
		resourcesDataManager.removeResourceFromPerson(Resource.ANTI_VIRAL_MED, personId, 1L);
		if (randomGenerator.nextDouble() < antiviralSuccessRate) {
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.IMMUNE, true);
		} else {
			hospitalizePerson(personId);
		}
	}

	private void treatWithAntiviral(PersonId personId) {
		/*
		 * Try to allocate one dose of the antiviral drug to the person. If the
		 * dose is available, then schedule an assessment of its success after
		 * the necessary waiting period. Otherwise, hospitalize the person
		 * immediately.
		 */
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
		/*
		 * Get access to various data managers
		 */
		this.actorContext = actorContext;
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

		/*
		 * Gather various values that will be used later
		 */
		
		double maximumSymptomOnsetTime = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MAXIMUM_SYMPTOM_ONSET_TIME);

		antiviralCoverageTime = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.ANTIVIRAL_COVERAGE_TIME);
		antiviralSuccessRate = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.ANTIVIRAL_SUCCESS_RATE);
		hospitalSuccessWithAntiviral = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITH_ANTIVIRAL);
		hospitalSuccessWithoutAntiviral = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITHOUT_ANTIVIRAL);
		hospitalStayDurationMin = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_STAY_DURATION_MIN);
		hospitalStayDurationMax = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.HOSPITAL_STAY_DURATION_MAX);

		/*
		 * Infect non-immune people. Those who become infected start antiviral
		 * treatment immediately.
		 */
		List<PersonId> susceptiblePeople = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.IMMUNE, false);
		for (PersonId personId : susceptiblePeople) {			
				double symptomOnsetTime = randomGenerator.nextDouble() * maximumSymptomOnsetTime;
				personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.INFECTED, true);
				actorContext.addPlan((c) -> treatWithAntiviral(personId), symptomOnsetTime);			
		}

	}
}
