package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.MaterialsProducer;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.Resource;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.datamangers.MaterialsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events.MaterialsProducerResourceUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.events.PersonPropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.datamanagers.ResourcesDataManager;
import gov.hhs.aspr.ms.util.wrappers.MutableDouble;
import gov.hhs.aspr.ms.util.wrappers.MutableLong;

public class Vaccinator {

	private MaterialsDataManager materialsDataManager;

	private RegionsDataManager regionsDataManager;

	private PeopleDataManager peopleDataManager;

	private PersonPropertiesDataManager personPropertiesDataManager;

	private GlobalPropertiesDataManager globalPropertiesDataManager;

	private ResourcesDataManager resourcesDataManager;

	private final Map<RegionId, MutableDouble> vaccinationSchedules = new LinkedHashMap<>();

	private final Map<RegionId, MutableLong> availableVaccines = new LinkedHashMap<>();

	private final int vaccinationsPerRegionPerDay = 100;

	private ActorContext actorContext;

	private int infectionPersonCountThreshold;
	private int infectedPersonCount;

	private boolean manufactureStarted;

	/* start code_ref=materials_plugin_vaccinator_manufacture_start|code_cap=If vaccine manufacture has not yet started and the number of infected people exceeds a threshold, then the vaccinator set the MANUFACTURE_VACCINE global property to true, signaling to the vaccine related materials producers to start.*/
	private void determineVaccineManufacutureStart() {
		if (!manufactureStarted) {
			if (infectedPersonCount >= infectionPersonCountThreshold) {
				manufactureStarted = true;
				globalPropertiesDataManager.setGlobalPropertyValue(GlobalProperty.MANUFACTURE_VACCINE, true);
				actorContext.unsubscribe(personPropertiesDataManager
						.getEventFilterForPersonPropertyUpdateEvent(PersonProperty.DISEASE_STATE));
			}
		}
	}

	/* end */
	/* start code_ref=materials_plugin_vaccinator_producer_resource_update|code_cap=When a resource change occurs on a materials producer, the vaccinator determines if the change represents doses of vaccine and whether there is any remaining demand. */
	private void handleMaterialsProducerResourceUpdateEvent(final ActorContext actorContext,
			final MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent) {
		if (isCapturableResource(materialsProducerResourceUpdateEvent)) {

			final MaterialsProducerId materialsProducerId = materialsProducerResourceUpdateEvent.materialsProducerId();

			final long resourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId,
					Resource.VACCINE);
			final List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

			final long resourceToTransfer = resourceLevel / regionIds.size();
			long remainderResource = resourceLevel % regionIds.size();

			for (final RegionId regionId : regionIds) {
				final MutableLong availableVaccine = availableVaccines.get(regionId);
				if (remainderResource > 0) {
					materialsDataManager.transferResourceToRegion(materialsProducerId, Resource.VACCINE, regionId,
							resourceToTransfer + 1);
					remainderResource--;
					availableVaccine.increment(resourceToTransfer + 1);
				} else {
					materialsDataManager.transferResourceToRegion(materialsProducerId, Resource.VACCINE, regionId,
							resourceToTransfer);
					availableVaccine.increment(resourceToTransfer);
				}
			}
			scheduleVaccinations();
		}
	}

	/* end */
	/* start code_ref=materials_plugin_vaccinator_person_property_update|code_cap=If a person become infectious, the vaccinator reviews whether to start vaccine manufacture.*/
	private void handlePersonPropertyUpdateEvent(final ActorContext actorContext,
			final PersonPropertyUpdateEvent personPropertyUpdateEvent) {

		final DiseaseState diseaseState = (DiseaseState) personPropertyUpdateEvent.getCurrentPropertyValue();
		if (diseaseState == DiseaseState.INFECTIOUS) {
			infectedPersonCount++;
			determineVaccineManufacutureStart();
		}
	}

	/* end */
	/* start code_ref=materials_plugin_vaccinator_init |code_cap=The vaccinator initializes by subscribing to changes in materials producer resource levels so that it can distribute vaccines to regions. It also subscribes to changes in person disease state to select people for vaccination.*/
	public void init(final ActorContext actorContext) {
		this.actorContext = actorContext;
		actorContext.addActor(new VaccineProducer(MaterialsProducer.VACCINE_PRODUCER)::init);
		actorContext.addActor(new AntigenProducer(MaterialsProducer.ANTIGEN_PRODUCER)::init);

		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		peopleDataManager = actorContext.getDataManager(PeopleDataManager.class);
		personPropertiesDataManager = actorContext.getDataManager(PersonPropertiesDataManager.class);
		resourcesDataManager = actorContext.getDataManager(ResourcesDataManager.class);
		regionsDataManager = actorContext.getDataManager(RegionsDataManager.class);
		materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);
		actorContext.subscribe(materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(),
				this::handleMaterialsProducerResourceUpdateEvent);

		for (final RegionId regionId : regionsDataManager.getRegionIds()) {
			vaccinationSchedules.put(regionId, new MutableDouble());
			availableVaccines.put(regionId, new MutableLong());
		}

		actorContext.subscribe(
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(PersonProperty.DISEASE_STATE),
				this::handlePersonPropertyUpdateEvent);

		final double infectionThreshold = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.INFECTION_THRESHOLD);
		infectedPersonCount = personPropertiesDataManager
				.getPeopleWithPropertyValue(PersonProperty.DISEASE_STATE, DiseaseState.INFECTIOUS).size();
		infectionPersonCountThreshold = (int) (peopleDataManager.getPopulationCount() * infectionThreshold);
		determineVaccineManufacutureStart();
		scheduleVaccinations();
	}

	/* end */
	/* start code_ref=materials_plugin_vaccinator_producer_capturable_resource|code_cap= When a materials producer updates its resource level, the vaccinator confirms that the resource is vaccine doses that have been added to the producer's inventory and that there is current demand for the vaccine.   */
	private boolean isCapturableResource(
			final MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent) {
		if (!materialsProducerResourceUpdateEvent.resourceId().equals(Resource.VACCINE)) {
			return false;
		}
		final boolean isResourceAdditionToProducer = materialsProducerResourceUpdateEvent
				.currentResourceLevel() > materialsProducerResourceUpdateEvent.previousResourceLevel();
		if (!isResourceAdditionToProducer) {
			return false;
		}

		long distributedVaccineCount = personPropertiesDataManager
				.getPersonCountForPropertyValue(PersonProperty.VACCINATED, true);

		for (final RegionId regionId : regionsDataManager.getRegionIds()) {
			distributedVaccineCount += resourcesDataManager.getRegionResourceLevel(regionId, Resource.VACCINE);
		}
		if (distributedVaccineCount >= peopleDataManager.getPopulationCount()) {
			return false;
		}
		return true;
	}

	/* end */
	/* start code_ref=materials_plugin_vaccinator_producer_schedule_vaccinations|code_cap=The vaccinator schedules vaccinations at initialization and whenever a materials producer produces resources. The vaccinator tries to distribute the available doses of vaccine with a standard delay time between scheduled vaccinations. */
	private void scheduleVaccinations() {
		final double delayTime = 1 / (double) vaccinationsPerRegionPerDay;

		for (final RegionId regionId : vaccinationSchedules.keySet()) {
			final MutableLong availableVaccine = availableVaccines.get(regionId);
			final MutableDouble vaccineTime = vaccinationSchedules.get(regionId);
			vaccineTime.increment(delayTime);
			if (vaccineTime.getValue() < actorContext.getTime()) {
				vaccineTime.setValue(actorContext.getTime());
			}
			final List<PersonId> peopleInRegion = regionsDataManager.getPeopleInRegion(regionId);
			for (final PersonId personId : peopleInRegion) {
				final boolean vaccine_scheduled = personPropertiesDataManager.getPersonPropertyValue(personId,
						PersonProperty.VACCINE_SCHEDULED);
				if (availableVaccine.getValue() <= 0) {
					break;
				}
				if (!vaccine_scheduled) {
					availableVaccine.decrement();
					personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINE_SCHEDULED,
							true);
					actorContext.addPlan((c) -> vaccinatePerson(personId), vaccineTime.getValue());
					vaccineTime.increment(delayTime);
				}
			}
		}
		final int populationSize = peopleDataManager.getPopulationCount();
		final int scheduledVaccinationCount = personPropertiesDataManager
				.getPersonCountForPropertyValue(PersonProperty.VACCINE_SCHEDULED, true);
		if (scheduledVaccinationCount >= populationSize) {
			globalPropertiesDataManager.setGlobalPropertyValue(GlobalProperty.MANUFACTURE_VACCINE, false);
		}
	}
	/* end */

	/* start code_ref=materials_plugin_vaccinator_producer_vaccinate_person|code_cap=The vaccinator sets the person's VACCINATED property to true and moves one unit of vaccine from the person's region to the person. */
	private void vaccinatePerson(final PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINATED, true);
		resourcesDataManager.transferResourceToPersonFromRegion(Resource.VACCINE, personId, 1L);
	}
	/* end */

}
