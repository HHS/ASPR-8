package plugins.gcm;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import nucleus.AgentContext;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.ReportId;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.components.ComponentPlugin;
import plugins.gcm.input.Scenario;
import plugins.globals.GlobalPlugin;
import plugins.globals.initialdata.GlobalInitialData;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalPropertyId;
import plugins.groups.GroupPlugin;
import plugins.groups.initialdata.GroupInitialData;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.materials.MaterialsPlugin;
import plugins.materials.initialdata.MaterialsInitialData;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.resources.ResourcesPlugin;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import plugins.stochastics.support.RandomNumberGeneratorId;

public final class GCMMonolithicSupport {

	private final Scenario scenario;
	private final long seed;

	private GCMMonolithicSupport(Scenario scenario, long seed) {
		if (scenario == null) {
			throw new RuntimeException("null scenario");
		}
		this.scenario = scenario;
		this.seed = seed;
	}

	private PeopleInitialData getPeopleInitialData(Scenario scenario) {
		PeopleInitialData.Builder builder = PeopleInitialData.builder();		
		for (PersonId personId : scenario.getPeopleIds()) {
			builder.addPersonId(personId);
		}
		return builder.build();
	}

	private GlobalInitialData getGlobalInitialData(Scenario scenario) {
		GlobalInitialData.Builder builder = GlobalInitialData.builder();

		for (GlobalComponentId globalComponentId : scenario.getGlobalComponentIds()) {
			Supplier<Consumer<AgentContext>> supplier = scenario.getGlobalInitialBehaviorSupplier(globalComponentId);
			builder.setGlobalComponentInitialBehaviorSupplier(globalComponentId, supplier);
		}

		for (GlobalPropertyId globalPropertyId : scenario.getGlobalPropertyIds()) {
			PropertyDefinition globalPropertyDefinition = scenario.getGlobalPropertyDefinition(globalPropertyId);
			builder.defineGlobalProperty(globalPropertyId, globalPropertyDefinition);
			Object globalPropertyValue = scenario.getGlobalPropertyValue(globalPropertyId);
			if (globalPropertyValue != null) {
				builder.setGlobalPropertyValue(globalPropertyId, globalPropertyValue);
			}
		}

		return builder.build();

	}

	private PersonPropertyInitialData getPersonPropertyInitialData(Scenario scenario) {
		PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
		Set<PersonPropertyId> personPropertyIds = scenario.getPersonPropertyIds();
		for (PersonPropertyId personPropertyId : personPropertyIds) {
			PropertyDefinition personPropertyDefinition = scenario.getPersonPropertyDefinition(personPropertyId);
			builder.definePersonProperty(personPropertyId, personPropertyDefinition);
		}

		for (PersonId personId : scenario.getPeopleIds()) {
			for (PersonPropertyId personPropertyId : personPropertyIds) {
				Object personPropertyValue = scenario.getPersonPropertyValue(personId, personPropertyId);
				if (personPropertyValue != null) {
					builder.setPersonPropertyValue(personId, personPropertyId, personPropertyValue);
				}
			}
		}

		return builder.build();
	}

	private StochasticsInitialData getStochasticsInitialData(Scenario scenario, long seed) {
		StochasticsInitialData.Builder builder = StochasticsInitialData.builder();
		for (RandomNumberGeneratorId randomNumberGeneratorId : scenario.getRandomNumberGeneratorIds()) {
			builder.addRandomGeneratorId(randomNumberGeneratorId);
		}
		builder.setSeed(seed);
		return builder.build();
	}

	private ReportsInitialData getReportsInitialData(Scenario scenario) {

		ReportsInitialData.Builder builder = ReportsInitialData.builder();
		for (ReportId reportId : scenario.getReportIds()) {
			builder.addReport(reportId, scenario.getReportInitialBehaviorSupplier(reportId));
		}
		return builder.build();
	}

	private RegionInitialData getRegionInitialData(Scenario scenario) {
		RegionInitialData.Builder builder = RegionInitialData.builder();

		for (PersonId personId : scenario.getPeopleIds()) {
			RegionId regionId = scenario.getPersonRegion(personId);
			builder.setPersonRegion(personId, regionId);
		}

		builder.setPersonRegionArrivalTracking(scenario.getPersonRegionArrivalTrackingPolicy());

		Set<RegionPropertyId> regionPropertyIds = scenario.getRegionPropertyIds();
		for (RegionPropertyId regionPropertyId : regionPropertyIds) {
			PropertyDefinition regionPropertyDefinition = scenario.getRegionPropertyDefinition(regionPropertyId);
			builder.defineRegionProperty(regionPropertyId, regionPropertyDefinition);
		}

		for (RegionId regionId : scenario.getRegionIds()) {
			Supplier<Consumer<AgentContext>> supplier = scenario.getRegionInitialBehaviorSupplier(regionId);
			builder.setRegionComponentInitialBehaviorSupplier(regionId, supplier);

			for (RegionPropertyId regionPropertyId : regionPropertyIds) {
				Object regionPropertyValue = scenario.getRegionPropertyValue(regionId, regionPropertyId);
				builder.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
			}
		}

		return builder.build();
	}

	private CompartmentInitialData getCompartmentInitialData(Scenario scenario) {

		CompartmentInitialData.Builder builder = CompartmentInitialData.builder();

		for (PersonId personId : scenario.getPeopleIds()) {
			CompartmentId compartmentId = scenario.getPersonCompartment(personId);
			builder.setPersonCompartment(personId, compartmentId);
		}

		builder.setPersonCompartmentArrivalTracking(scenario.getPersonCompartmentArrivalTrackingPolicy());

		for (CompartmentId compartmentId : scenario.getCompartmentIds()) {

			Set<CompartmentPropertyId> compartmentPropertyIds = scenario.getCompartmentPropertyIds(compartmentId);
			for (CompartmentPropertyId compartmentPropertyId : compartmentPropertyIds) {
				PropertyDefinition compartmentPropertyDefinition = scenario.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
				builder.defineCompartmentProperty(compartmentId, compartmentPropertyId, compartmentPropertyDefinition);
			}

			builder.setCompartmentInitialBehaviorSupplier(compartmentId, scenario.getCompartmentInitialBehaviorSupplier(compartmentId));

			for (CompartmentPropertyId compartmentPropertyId : compartmentPropertyIds) {
				Object compartmentPropertyValue = scenario.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
				builder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, compartmentPropertyValue);
			}
		}

		return builder.build();
	}

	private GroupInitialData getGroupInitialData(Scenario scenario) {
		GroupInitialData.Builder builder = GroupInitialData.builder();

		for (GroupTypeId groupTypeId : scenario.getGroupTypeIds()) {
			builder.addGroupTypeId(groupTypeId);
			Set<GroupPropertyId> groupPropertyIds = scenario.getGroupPropertyIds(groupTypeId);
			for (GroupPropertyId groupPropertyId : groupPropertyIds) {
				PropertyDefinition groupPropertyDefinition = scenario.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
				builder.defineGroupProperty(groupTypeId, groupPropertyId, groupPropertyDefinition);
			}
		}

		for (GroupId groupId : scenario.getGroupIds()) {
			GroupTypeId groupTypeId = scenario.getGroupTypeId(groupId);
			builder.addGroup(groupId, groupTypeId);

			Set<GroupPropertyId> groupPropertyIds = scenario.getGroupPropertyIds(groupTypeId);
			for (GroupPropertyId groupPropertyId : groupPropertyIds) {
				Object groupPropertyValue = scenario.getGroupPropertyValue(groupId, groupPropertyId);
				if (groupPropertyValue != null) {
					builder.setGroupPropertyValue(groupId, groupPropertyId, groupPropertyValue);
				}
			}

			Set<PersonId> groupMembers = scenario.getGroupMembers(groupId);
			for (PersonId personId : groupMembers) {
				builder.addPersonToGroup(groupId, personId);
			}

		}

		return builder.build();
	}

	private ResourceInitialData getResourceInitialData(Scenario scenario) {

		ResourceInitialData.Builder builder = ResourceInitialData.builder();

		Set<ResourceId> resourceIds = scenario.getResourceIds();
		for (ResourceId resourceId : resourceIds) {
			builder.addResource(resourceId);
			Set<ResourcePropertyId> resourcePropertyIds = scenario.getResourcePropertyIds(resourceId);
			for (ResourcePropertyId resourcePropertyId : resourcePropertyIds) {
				PropertyDefinition resourcePropertyDefinition = scenario.getResourcePropertyDefinition(resourceId, resourcePropertyId);
				builder.defineResourceProperty(resourceId, resourcePropertyId, resourcePropertyDefinition);

				Object resourcePropertyValue = scenario.getResourcePropertyValue(resourceId, resourcePropertyId);
				if (resourcePropertyValue != null) {
					builder.setResourcePropertyValue(resourceId, resourcePropertyId, resourcePropertyValue);
				}

			}
			TimeTrackingPolicy personResourceTimeTrackingPolicy = scenario.getPersonResourceTimeTrackingPolicy(resourceId);
			if (personResourceTimeTrackingPolicy != null) {
				builder.setResourceTimeTracking(resourceId, personResourceTimeTrackingPolicy);
			}

		}

		for (PersonId personId : scenario.getPeopleIds()) {
			for (ResourceId resourceId : resourceIds) {
				Long personResourceLevel = scenario.getPersonResourceLevel(personId, resourceId);
				if (personResourceLevel > 0) {
					builder.setPersonResourceLevel(personId, resourceId, personResourceLevel);
				}
			}
		}

		for (RegionId regionId : scenario.getRegionIds()) {
			for (ResourceId resourceId : resourceIds) {
				Long regionResourceLevel = scenario.getRegionResourceLevel(regionId, resourceId);
				if (regionResourceLevel > 0) {
					builder.setRegionResourceLevel(regionId, resourceId, regionResourceLevel);
				}
			}
		}

		return builder.build();
	}

	private MaterialsInitialData getMaterialsInitialization(Scenario scenario) {

		MaterialsInitialData.Builder builder = MaterialsInitialData.builder();

		for (BatchId batchId : scenario.getBatchIds()) {
			MaterialId materialId = scenario.getBatchMaterial(batchId);
			double amount = scenario.getBatchAmount(batchId);
			MaterialsProducerId materialsProducerId = scenario.getBatchMaterialsProducer(batchId);
			builder.addBatch(batchId, materialId, amount, materialsProducerId);
		}
		for (StageId stageId : scenario.getStageIds()) {
			Boolean stageOffered = scenario.isStageOffered(stageId);
			MaterialsProducerId materialsProducerId = scenario.getStageMaterialsProducer(stageId);
			builder.addStage(stageId, stageOffered, materialsProducerId);

			Set<BatchId> stageBatches = scenario.getStageBatches(stageId);
			for (BatchId batchId : stageBatches) {
				builder.addBatchToStage(stageId, batchId);
			}
		}

		for (MaterialId materialId : scenario.getMaterialIds()) {
			builder.addMaterial(materialId);
			Set<BatchPropertyId> batchPropertyIds = scenario.getBatchPropertyIds(materialId);
			for (BatchPropertyId batchPropertyId : batchPropertyIds) {
				PropertyDefinition batchPropertyDefinition = scenario.getBatchPropertyDefinition(materialId, batchPropertyId);
				builder.defineBatchProperty(materialId, batchPropertyId, batchPropertyDefinition);
			}
		}

		for (MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
			Supplier<Consumer<AgentContext>> supplier = scenario.getMaterialsProducerInitialBehaviorSupplier(materialsProducerId);
			builder.addMaterialsProducerId(materialsProducerId, supplier);
		}

		for (MaterialsProducerPropertyId materialsProducerPropertyId : scenario.getMaterialsProducerPropertyIds()) {
			PropertyDefinition materialsProducerPropertyDefinition = scenario.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
			builder.defineMaterialsProducerProperty(materialsProducerPropertyId, materialsProducerPropertyDefinition);
		}

		for (BatchId batchId : scenario.getBatchIds()) {
			MaterialId materialId = scenario.getBatchMaterial(batchId);
			Set<BatchPropertyId> batchPropertyIds = scenario.getBatchPropertyIds(materialId);
			for (BatchPropertyId batchPropertyId : batchPropertyIds) {
				Object batchPropertyValue = scenario.getBatchPropertyValue(batchId, batchPropertyId);
				builder.setBatchPropertyValue(batchId, batchPropertyId, batchPropertyValue);
			}
		}

		Set<MaterialsProducerPropertyId> materialsProducerPropertyIds = scenario.getMaterialsProducerPropertyIds();

		for (MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
			for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyIds) {
				Object materialsProducerPropertyValue = scenario.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
				builder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue);
			}
		}

		Set<ResourceId> resourceIds = scenario.getResourceIds();
		for (MaterialsProducerId materialsProducerId : scenario.getMaterialsProducerIds()) {
			for (ResourceId resourceId : resourceIds) {
				Long materialsProducerResourceLevel = scenario.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
				builder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, materialsProducerResourceLevel);
			}
		}

		return builder.build();
	}

	public static EngineBuilder getEngineBuilder(Scenario scenario, long seed) {
		EngineBuilder engineBuilder = Engine.builder();
		new GCMMonolithicSupport(scenario, seed).load(engineBuilder);
		return engineBuilder;
	}

	private void load(EngineBuilder engineBuilder) {
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID,new StochasticsPlugin(getStochasticsInitialData(scenario,seed))::init);
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID,new ComponentPlugin()::init);
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID,new PropertiesPlugin()::init);
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID,new ReportPlugin(getReportsInitialData(scenario))::init);
		engineBuilder.addPlugin(GlobalPlugin.PLUGIN_ID,new GlobalPlugin(getGlobalInitialData(scenario))::init);
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID,new PeoplePlugin(getPeopleInitialData(scenario))::init);
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID,new PartitionsPlugin()::init);
		engineBuilder.addPlugin(RegionPlugin.PLUGIN_ID,new RegionPlugin(getRegionInitialData(scenario))::init);
		engineBuilder.addPlugin(CompartmentPlugin.PLUGIN_ID,new CompartmentPlugin(getCompartmentInitialData(scenario))::init);
		engineBuilder.addPlugin(PersonPropertiesPlugin.PLUGIN_ID,new PersonPropertiesPlugin(getPersonPropertyInitialData(scenario))::init);
		engineBuilder.addPlugin(GroupPlugin.PLUGIN_ID,new GroupPlugin(getGroupInitialData(scenario))::init);
		engineBuilder.addPlugin(ResourcesPlugin.PLUGIN_ID,new ResourcesPlugin(getResourceInitialData(scenario))::init);
		engineBuilder.addPlugin(MaterialsPlugin.PLUGIN_ID,new MaterialsPlugin(getMaterialsInitialization(scenario))::init);
		engineBuilder.addPlugin(GCMPlugin.PLUGIN_ID,new GCMPlugin()::init);
	}

}
