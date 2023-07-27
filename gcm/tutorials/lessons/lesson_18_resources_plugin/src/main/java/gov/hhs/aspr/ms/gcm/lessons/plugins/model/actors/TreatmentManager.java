package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.PersonProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.Resource;
import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;

public class TreatmentManager {

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

	/*
	 * Determine whether the hospitalization was a success taking into account
	 * whether the person received an antiviral treatment before entering the
	 * hospital. If the treatment was successful then mark the person as immune.
	 * Otherwise, mark the person as a hospital death.
	 */
	/* start code_ref=resources_treatment_manager_assessHospitalization */
	private void assessHospitalization(PersonId personId) {

		boolean treatedWithAntiViral = personPropertiesDataManager.getPersonPropertyValue(personId,
				PersonProperty.TREATED_WITH_ANTIVIRAL);
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
	/* end */

	/*
	 * Try to allocate a hospital bed to the person. If the bed is available, then
	 * schedule an assessment of the treatment success. Otherwise, mark the person
	 * as a home death.
	 */

	/* start code_ref=resources_treatment_manager_hospitalizePerson */
	private void hospitalizePerson(PersonId personId) {
		RegionId regionId = regionsDataManager.getPersonRegion(personId);

		long availableHospitalBeds = resourcesDataManager.getRegionResourceLevel(regionId, Resource.HOSPITAL_BED);

		if (availableHospitalBeds > 0) {
			resourcesDataManager.transferResourceToPersonFromRegion(Resource.HOSPITAL_BED, personId, 1L);

			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.HOSPITALIZED, true);

			double hospitalizationDuration = (hospitalStayDurationMax - hospitalStayDurationMin)
					* randomGenerator.nextDouble() + hospitalStayDurationMin;

			actorContext.addPlan((c) -> assessHospitalization(personId),
					actorContext.getTime() + hospitalizationDuration);
		} else {
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.DEAD_IN_HOME, true);
		}
	}
	/* end */

	/*
	 * Expend the antiviral resource from the person. If the antiviral succeeded,
	 * then mark the person as immune. Otherwise, hospitalize the person immediately
	 */
	/* start code_ref=resources_treatment_manager_assessAntiviralTreatment */
	private void assessAntiviralTreatment(PersonId personId) {

		resourcesDataManager.removeResourceFromPerson(Resource.ANTI_VIRAL_MED, personId, 1L);
		if (randomGenerator.nextDouble() < antiviralSuccessRate) {
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.IMMUNE, true);
		} else {
			hospitalizePerson(personId);
		}
	}
	/* end */

	/*
	 * Try to allocate one dose of the antiviral drug to the person. If the dose is
	 * available, then schedule an assessment of its success after the necessary
	 * waiting period. Otherwise, hospitalize the person immediately.
	 */
	/* start code_ref=resources_treatment_manager_treatWithAntiviral */
	private void treatWithAntiviral(PersonId personId) {

		RegionId regionId = regionsDataManager.getPersonRegion(personId);

		long regionResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, Resource.ANTI_VIRAL_MED);

		if (regionResourceLevel > 0) {
			resourcesDataManager.transferResourceToPersonFromRegion(Resource.ANTI_VIRAL_MED, personId, 1L);

			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.TREATED_WITH_ANTIVIRAL, true);

			actorContext.addPlan((c) -> assessAntiviralTreatment(personId),
					actorContext.getTime() + antiviralCoverageTime);
		} else {
			hospitalizePerson(personId);
		}
	}
	/* end */

	/* start code_ref=resources_treatment_manager_init */
	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		StochasticsDataManager stochasticsDataManager = actorContext.getDataManager(StochasticsDataManager.class);
		randomGenerator = stochasticsDataManager.getRandomGenerator();
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		GlobalPropertiesDataManager globalPropertiesDataManager = actorContext
				.getDataManager(GlobalPropertiesDataManager.class);

		double maximumSymptomOnsetTime = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.MAXIMUM_SYMPTOM_ONSET_TIME);
		antiviralCoverageTime = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.ANTIVIRAL_COVERAGE_TIME);
		antiviralSuccessRate = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.ANTIVIRAL_SUCCESS_RATE);
		hospitalSuccessWithAntiviral = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITH_ANTIVIRAL);
		hospitalSuccessWithoutAntiviral = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITHOUT_ANTIVIRAL);
		hospitalStayDurationMin = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.HOSPITAL_STAY_DURATION_MIN);
		hospitalStayDurationMax = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.HOSPITAL_STAY_DURATION_MAX);

		List<PersonId> susceptiblePeople = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.IMMUNE,
				false);
		for (PersonId personId : susceptiblePeople) {
			double symptomOnsetTime = randomGenerator.nextDouble() * maximumSymptomOnsetTime;
			personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.INFECTED, true);

			actorContext.addPlan((c) -> treatWithAntiviral(personId), symptomOnsetTime);
		}
	}
	/* end */

}
