package lesson.plugins.model.actors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.MaterialsProducer;
import lesson.plugins.model.support.PersonProperty;
import lesson.plugins.model.support.Resource;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.MaterialsProducerResourceUpdateEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourcesDataManager;
import util.wrappers.MutableDouble;
import util.wrappers.MutableLong;

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

	private void determineVaccineManufacutureStart() {
		if (!manufactureStarted) {
			if (infectedPersonCount >= infectionPersonCountThreshold) {
				manufactureStarted = true;
				globalPropertiesDataManager.setGlobalPropertyValue(GlobalProperty.MANUFACTURE_VACCINE, true);
				actorContext.unsubscribe(personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(PersonProperty.DISEASE_STATE));
			}
		}
	}

	private void handleMaterialsProducerResourceUpdateEvent(final ActorContext actorContext, final MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent) {
		if (isCapturableResource(materialsProducerResourceUpdateEvent)) {

			final MaterialsProducerId materialsProducerId = materialsProducerResourceUpdateEvent.materialsProducerId();

			final long resourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, Resource.VACCINE);
			final List<RegionId> regionIds = new ArrayList<>(regionsDataManager.getRegionIds());

			final long resourceToTransfer = resourceLevel / regionIds.size();
			long remainderResource = resourceLevel % regionIds.size();

			for (final RegionId regionId : regionIds) {
				final MutableLong availableVaccine = availableVaccines.get(regionId);
				if (remainderResource > 0) {
					materialsDataManager.transferResourceToRegion(materialsProducerId, Resource.VACCINE, regionId, resourceToTransfer + 1);
					remainderResource--;
					availableVaccine.increment(resourceToTransfer + 1);
				} else {
					materialsDataManager.transferResourceToRegion(materialsProducerId, Resource.VACCINE, regionId, resourceToTransfer);
					availableVaccine.increment(resourceToTransfer);
				}
			}
			scheduleVaccinations();
		}
	}

	private void handlePersonPropertyUpdateEvent(final ActorContext actorContext, final PersonPropertyUpdateEvent personPropertyUpdateEvent) {

		final DiseaseState diseaseState = (DiseaseState) personPropertyUpdateEvent.getCurrentPropertyValue();
		if (diseaseState == DiseaseState.INFECTIOUS) {
			infectedPersonCount++;
			determineVaccineManufacutureStart();
		}
	}

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
		actorContext.subscribe(materialsDataManager.getEventFilterForMaterialsProducerResourceUpdateEvent(), this::handleMaterialsProducerResourceUpdateEvent);

		for (final RegionId regionId : regionsDataManager.getRegionIds()) {
			vaccinationSchedules.put(regionId, new MutableDouble());
			availableVaccines.put(regionId, new MutableLong());
		}

		actorContext.subscribe(personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(PersonProperty.DISEASE_STATE), this::handlePersonPropertyUpdateEvent);

		final double infectionThreshold = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.INFECTION_THRESHOLD);
		infectedPersonCount = personPropertiesDataManager.getPeopleWithPropertyValue(PersonProperty.DISEASE_STATE, DiseaseState.INFECTIOUS).size();
		infectionPersonCountThreshold = (int) (peopleDataManager.getPopulationCount() * infectionThreshold);
		determineVaccineManufacutureStart();
	}

	private boolean isCapturableResource(final MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent) {
		if (!materialsProducerResourceUpdateEvent.resourceId().equals(Resource.VACCINE)) {
			return false;
		}
		final boolean isResourceAdditionToProducer = materialsProducerResourceUpdateEvent.currentResourceLevel() > materialsProducerResourceUpdateEvent.previousResourceLevel();
		if (!isResourceAdditionToProducer) {
			return false;
		}

		long distributedVaccineCount = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.VACCINATED, true);

		for (final RegionId regionId : regionsDataManager.getRegionIds()) {
			distributedVaccineCount += resourcesDataManager.getRegionResourceLevel(regionId, Resource.VACCINE);
		}
		if (distributedVaccineCount >= peopleDataManager.getPopulationCount()) {
			return false;
		}
		return true;
	}

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
				final boolean vaccine_scheduled = personPropertiesDataManager.getPersonPropertyValue(personId, PersonProperty.VACCINE_SCHEDULED);
				if (availableVaccine.getValue() <= 0) {
					break;
				}
				if (!vaccine_scheduled) {
					availableVaccine.decrement();
					personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINE_SCHEDULED, true);
					actorContext.addPlan((c) -> vaccinatePerson(personId), vaccineTime.getValue());
					vaccineTime.increment(delayTime);
				}
			}
		}
		final int populationSize = peopleDataManager.getPopulationCount();
		final int scheduledVaccinationCount = personPropertiesDataManager.getPersonCountForPropertyValue(PersonProperty.VACCINE_SCHEDULED, true);
		if (scheduledVaccinationCount >= populationSize) {
			globalPropertiesDataManager.setGlobalPropertyValue(GlobalProperty.MANUFACTURE_VACCINE, false);
		}
	}

	private void vaccinatePerson(final PersonId personId) {
		personPropertiesDataManager.setPersonPropertyValue(personId, PersonProperty.VACCINATED, true);
		resourcesDataManager.transferResourceToPersonFromRegion(Resource.VACCINE, personId, 1L);
	}

}
