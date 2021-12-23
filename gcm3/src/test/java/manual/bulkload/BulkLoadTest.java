package manual.bulkload;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Context;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.components.ComponentPlugin;
import plugins.globals.GlobalPlugin;
import plugins.globals.initialdata.GlobalInitialData;
import plugins.globals.support.GlobalComponentId;
import plugins.materials.MaterialsPlugin;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.mutation.BatchCreationEvent;
import plugins.materials.events.mutation.MoveBatchToStageEvent;
import plugins.materials.events.mutation.ProducedResourceTransferToRegionEvent;
import plugins.materials.events.mutation.StageCreationEvent;
import plugins.materials.events.mutation.StageToResourceConversionEvent;
import plugins.materials.initialdata.MaterialsInitialData;
import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.datacontainers.PartitionDataView;
import plugins.partitions.events.PartitionAdditionEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.LabelSet;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionSampler;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.personproperties.support.PersonPropertyLabeler;
import plugins.personproperties.support.PropertyFilter;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.regions.RegionPlugin;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionLabeler;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.resources.ResourcesPlugin;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.mutation.ResourceTransferToPersonEvent;
import plugins.resources.events.observation.RegionResourceChangeObservationEvent;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.resources.support.ResourceFilter;
import plugins.resources.support.ResourceId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.initialdata.StochasticsInitialData;

@Tag("manual")
public class BulkLoadTest {
	private static enum GlobalComponent implements GlobalComponentId {
		POPULATION_LOADER, VACCINATOR;
	}

	private static enum Material implements MaterialId {
		MRNA, LIPID, SALT, SUGAR;
	}

	private static enum Resource implements ResourceId {
		VACCINE;
	}

	private static enum MaterialsProducer implements MaterialsProducerId {
		VACCINE_PRODUCER
	}

	private static enum Compartment implements CompartmentId {
		SUSCEPTIBLE, EXPOSED, INFECTIOUS, RECOVERED
	}

	private static enum Region implements RegionId {
		REGION_1, REGION_2;
	}
	
	

	private static enum PersonProperty implements PersonPropertyId {
		WILLINGNESS(PropertyDefinition.builder().setDefaultValue(0.0).setType(Double.class).build()), //
		IMMUNE(PropertyDefinition.builder().setDefaultValue(false).setType(Boolean.class).build()), //
		AGE(PropertyDefinition.builder().setDefaultValue(0.0).setType(Double.class).build()),//
		;

		public PropertyDefinition getPropertyDefinition() {
			return propertyDefinition;
		}

		private final PropertyDefinition propertyDefinition;

		private PersonProperty(PropertyDefinition propertyDefinition) {
			this.propertyDefinition = propertyDefinition;
		}
	}

	private static enum WillingnessCategory {
		LOW, MEDIUM, HIGH;

		public static WillingnessCategory getWillingnessCategory(double willingness) {
			if (willingness < 0.3) {
				return LOW;
			}
			if (willingness < 0.7) {
				return MEDIUM;
			}
			return HIGH;
		}
	}

	private static void loadGlobalsPlugin(EngineBuilder engineBuilder) {
		GlobalInitialData.Builder builder = GlobalInitialData.builder();
		for (GlobalComponent globalComponent : GlobalComponent.values()) {
			builder.setGlobalComponentInitialBehaviorSupplier(globalComponent, () -> {
				return new ActionAgent(globalComponent)::init;
			});
		}
		engineBuilder.addPlugin(GlobalPlugin.PLUGIN_ID,new GlobalPlugin(builder.build())::init);
	}

	private static void loadRegionPlugin(EngineBuilder engineBuilder) {
		RegionInitialData.Builder builder = RegionInitialData.builder();
		for (Region region : Region.values()) {
			builder.setRegionComponentInitialBehaviorSupplier(region, () -> (c) -> {
			});
		}
		engineBuilder.addPlugin(RegionPlugin.PLUGIN_ID,new RegionPlugin(builder.build())::init);
	}

	private static void loadCompartmentPlugin(EngineBuilder engineBuilder) {
		CompartmentInitialData.Builder builder = CompartmentInitialData.builder();

		for (Compartment compartment : Compartment.values()) {
			builder.setCompartmentInitialBehaviorSupplier(compartment, () -> new ActionAgent(compartment)::init);
		}

		CompartmentPlugin compartmentPlugin = new CompartmentPlugin(builder.build());
		engineBuilder.addPlugin(CompartmentPlugin.PLUGIN_ID,compartmentPlugin::init);
	}

	private static void loadComponentPlugin(EngineBuilder engineBuilder) {
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID,new ComponentPlugin()::init);
	}

	private static void loadPartitionPlugin(EngineBuilder engineBuilder) {
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID,new PartitionsPlugin()::init);
	}

	private static void loadPeoplePlugin(EngineBuilder engineBuilder) {
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID,new PeoplePlugin(PeopleInitialData.builder().build())::init);
	}

	private static void loadPropertiesPlugin(EngineBuilder engineBuilder) {
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID,new PropertiesPlugin()::init);
	}

	private static void loadReportPlugin(EngineBuilder engineBuilder) {
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID,new ReportPlugin(ReportsInitialData.builder().build())::init);
	}

	private static void loadStochasticsPlugin(EngineBuilder engineBuilder) {
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID,new StochasticsPlugin(StochasticsInitialData.builder().setSeed(13452456453L).build())::init);
	}

	private static void loadResourcesPlugin(EngineBuilder engineBuilder) {
		ResourceInitialData.Builder builder = ResourceInitialData.builder();
		for (Resource resource : Resource.values()) {
			builder.addResource(resource);
		}
		ResourcesPlugin resourcesPlugin = new ResourcesPlugin(builder.build());
		engineBuilder.addPlugin(ResourcesPlugin.PLUGIN_ID,resourcesPlugin::init);
	}

	private static void loadMaterialsPlugin(EngineBuilder engineBuilder) {
		MaterialsInitialData.Builder builder = MaterialsInitialData.builder();
		for (Material material : Material.values()) {
			builder.addMaterial(material);
		}
		for (MaterialsProducer materialsProducer : MaterialsProducer.values()) {
			builder.addMaterialsProducerId(materialsProducer, () -> {
				return new ActionAgent(materialsProducer)::init;
			});
		}
		MaterialsPlugin materialsPlugin = new MaterialsPlugin(builder.build());
		engineBuilder.addPlugin(MaterialsPlugin.PLUGIN_ID,materialsPlugin::init);

	}

	private static void loadPersonPropertiesPlugin(EngineBuilder engineBuilder) {
		PersonPropertyInitialData.Builder builder = PersonPropertyInitialData.builder();
		for (PersonProperty personProperty : PersonProperty.values()) {
			builder.definePersonProperty(personProperty, personProperty.getPropertyDefinition());
		}

		engineBuilder.addPlugin(PersonPropertiesPlugin.PLUGIN_ID,new PersonPropertiesPlugin(builder.build())::init);
	}

	private final static Object KEY = "VACCINE_READY";

	private static void definePopulationLoader(ActionPlugin.Builder builder) {
		/*
		 * Initial behavior for population loader.
		 */
		builder.addAgentActionPlan(GlobalComponent.POPULATION_LOADER, new AgentActionPlan(0, (c) -> {
			int regionCount = Region.values().length;

			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();

			for (int i = 0; i < 100; i++) {
				Region region = Region.values()[(i % regionCount)];
				double willingness = randomGenerator.nextDouble();

				boolean immune = randomGenerator.nextDouble() < 0.1;
				Compartment compartment = Compartment.SUSCEPTIBLE;
				if (immune) {
					compartment = Compartment.RECOVERED;
				}
				double age = (1-FastMath.sqrt(randomGenerator.nextDouble()))*80;
				

				PersonContructionData personContructionData = PersonContructionData	.builder()//
																					.add(compartment)//
																					.add(region)//
																					.add(new PersonPropertyInitialization(PersonProperty.WILLINGNESS, willingness))//
																					.add(new PersonPropertyInitialization(PersonProperty.IMMUNE, immune))//
																					.add(new PersonPropertyInitialization(PersonProperty.AGE, age))//
																					.build();//
				c.resolveEvent(new PersonCreationEvent(personContructionData));
			}

		}));

	}

	private static void vaccineProduction(AgentContext context) {

		MaterialsDataView materialsDataView = context.getDataView(MaterialsDataView.class).get();

		context.resolveEvent(new StageCreationEvent());
		StageId stageId = materialsDataView.getLastIssuedStageId().get();

		context.resolveEvent(new BatchCreationEvent(Material.MRNA, 1));
		BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
		context.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));

		context.resolveEvent(new BatchCreationEvent(Material.LIPID, 1));
		batchId = materialsDataView.getLastIssuedBatchId().get();
		context.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));

		context.resolveEvent(new BatchCreationEvent(Material.SALT, 1));
		batchId = materialsDataView.getLastIssuedBatchId().get();
		context.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));

		context.resolveEvent(new BatchCreationEvent(Material.SUGAR, 1));
		batchId = materialsDataView.getLastIssuedBatchId().get();
		context.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));

		context.resolveEvent(new StageToResourceConversionEvent(stageId, Resource.VACCINE, 10));

		long doses = materialsDataView.getMaterialsProducerResourceLevel(MaterialsProducer.VACCINE_PRODUCER, Resource.VACCINE);
		Set<RegionId> regionIds = context.getDataView(RegionDataView.class).get().getRegionIds();
		long dosesPerRegion = doses / regionIds.size();

		for (RegionId regionId : regionIds) {
			ProducedResourceTransferToRegionEvent event = new ProducedResourceTransferToRegionEvent(MaterialsProducer.VACCINE_PRODUCER, Resource.VACCINE, regionId, dosesPerRegion);
			context.resolveEvent(event);
		}
	}

	private static void defineVaccineProducer(ActionPlugin.Builder builder) {
		builder.addAgentActionPlan(MaterialsProducer.VACCINE_PRODUCER, new AgentActionPlan(0, (c) -> {
			// determine how many days we will produce vaccine
			int populationCount = c.getDataView(PersonDataView.class).get().getPopulationCount();
			int dayCount = 2 * (populationCount / 10) + 1;

			/*
			 * schedule that much production per day until all doses are
			 * produced
			 */
			for (int i = 0; i < dayCount; i++) {
				c.addPlan(BulkLoadTest::vaccineProduction, c.getTime() + 1.0 + i);
			}
		}));
	}

	private static double getResourceWeight(Context context, LabelSet labelSet) {
		Optional<Object> optional = labelSet.getLabel(PersonProperty.WILLINGNESS);
		WillingnessCategory willingnessCategory = (WillingnessCategory) optional.get();
		switch (willingnessCategory) {
		case HIGH:
			return 5;
		case MEDIUM:
			return 3;
		case LOW:
			return 1;
		default:
			throw new RuntimeException("unhandled case " + willingnessCategory);
		}
	}

	private static class VaccineAttemptPlan implements Consumer<AgentContext> {
		private final PersonId personId;
		private final double firstDoseTime;

		public VaccineAttemptPlan(PersonId personId, double firstDoseTime) {
			this.personId = personId;
			this.firstDoseTime = firstDoseTime;
		}

		public void accept(AgentContext context) {
			RegionLocationDataView regionLocationDataView = context.getDataView(RegionLocationDataView.class).get();
			RegionId regionId = regionLocationDataView.getPersonRegion(personId);
			ResourceDataView resourceDataView = context.getDataView(ResourceDataView.class).get();
			long regionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, Resource.VACCINE);
			if (regionResourceLevel > 0) {
				context.releaseOutput(new PersonTrace(context.getTime(), personId, "second dose"));

				context.resolveEvent(new ResourceTransferToPersonEvent(Resource.VACCINE, personId, 1));
			} else {
				if (context.getTime() < firstDoseTime + 50) {
					context.releaseOutput(new PersonTrace(context.getTime(), personId, "resheduling second dose"));
					context.addPlan(this, context.getTime() + 5);
				} else {
					context.releaseOutput(new PersonTrace(context.getTime(), personId, "failed second dose"));
				}
			}
		}
	}

	private static void vaccinatorResourceObservation(AgentContext context, RegionResourceChangeObservationEvent event) {

		if (event.getResourceId() != Resource.VACCINE) {
			throw new RuntimeException("unexpected resource " + event.getResourceId());
		}
		// distribute the vaccine doses to those who need them
		RegionId regionId = event.getRegionId();

		ResourceDataView resourceDataView = context.getDataView(ResourceDataView.class).get();
		long availableDoses = resourceDataView.getRegionResourceLevel(regionId, Resource.VACCINE);
		PartitionDataView partitionDataView = context.getDataView(PartitionDataView.class).get();
		LabelSet labelSet = LabelSet.builder().setLabel(RegionId.class, regionId).build();
		PartitionSampler partitionSampler = PartitionSampler.builder()//
															.setLabelSet(labelSet)//
															.setLabelSetWeightingFunction(BulkLoadTest::getResourceWeight).build();//
		double secondVaccinationTime = context.getTime() + 5;
		while (availableDoses > 0) {
			Optional<PersonId> optional = partitionDataView.samplePartition(KEY, partitionSampler);
			if (optional.isPresent()) {
				PersonId personId = optional.get();

				context.releaseOutput(new PersonTrace(context.getTime(), personId, "first vaccine"));
				context.resolveEvent(new ResourceTransferToPersonEvent(Resource.VACCINE, personId, 1));
				availableDoses--;

				// schedule 2nd vaccine
				VaccineAttemptPlan vaccineAttemptPlan = new VaccineAttemptPlan(personId, context.getTime());
				context.addPlan(vaccineAttemptPlan, secondVaccinationTime);
			} else {
				break;
			}
		}

	}

	private static void defineVaccinator(ActionPlugin.Builder builder) {
		// start watching the production of vaccine and its arrival in the
		// regions
		builder.addAgentActionPlan(GlobalComponent.VACCINATOR, new AgentActionPlan(0, (c) -> {
			// create the partition
			PropertyFilter immuneFilter = new PropertyFilter(PersonProperty.IMMUNE, Equality.EQUAL, false);
			ResourceFilter resourceFilter = new ResourceFilter(Resource.VACCINE, Equality.EQUAL, 0);
			PropertyFilter ageFilter = new PropertyFilter(PersonProperty.AGE, Equality.GREATER_THAN_EQUAL, 18.0);
			Filter filter = immuneFilter.and(resourceFilter).and(ageFilter);
					

			PersonPropertyLabeler willingnesslabeler = new PersonPropertyLabeler(PersonProperty.WILLINGNESS, (willingness) -> {
				return WillingnessCategory.getWillingnessCategory((Double) willingness);
			});

			RegionLabeler regionLabeler = new RegionLabeler((r) -> r);

			Partition partition = Partition	.builder().setFilter(filter)//
											.addLabeler(willingnesslabeler)//
											.addLabeler(regionLabeler)//
											.build();//
			c.resolveEvent(new PartitionAdditionEvent(partition, KEY));

			// start observing changes to the
			Set<RegionId> regionIds = c.getDataView(RegionDataView.class).get().getRegionIds();
			for (RegionId regionId : regionIds) {
				c.subscribe(RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(//
						c, regionId, //
						Resource.VACCINE), //
						BulkLoadTest::vaccinatorResourceObservation);//
			}
		}));
	}

	@Test
	public void test() {
		EngineBuilder engineBuilder = Engine.builder();
		loadRegionPlugin(engineBuilder);
		loadComponentPlugin(engineBuilder);
		loadCompartmentPlugin(engineBuilder);
		loadPartitionPlugin(engineBuilder);
		loadPeoplePlugin(engineBuilder);
		loadPropertiesPlugin(engineBuilder);
		loadReportPlugin(engineBuilder);
		loadStochasticsPlugin(engineBuilder);
		loadPersonPropertiesPlugin(engineBuilder);
		loadGlobalsPlugin(engineBuilder);
		loadMaterialsPlugin(engineBuilder);
		loadResourcesPlugin(engineBuilder);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		definePopulationLoader(pluginBuilder);
		defineVaccinator(pluginBuilder);
		defineVaccineProducer(pluginBuilder);

		// build the plugin
		ActionPlugin actionPlugin = pluginBuilder.build();
		// build and execute the engine
		engineBuilder.setOutputConsumer((o) -> {
			if(o instanceof PersonTrace) {
				return;
			}
			System.out.println(o);
		});
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID,actionPlugin::init).build().execute();

		// show that all actions executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	private static class PersonTrace {
		private final double time;
		private final PersonId personId;
		private final String activity;

		public PersonTrace(double time, PersonId personId, String activity) {
			this.time = time;
			this.personId = personId;
			this.activity = activity;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PersonTrace [time=");
			builder.append(time);
			builder.append(", personId=");
			builder.append(personId);
			builder.append(", activity=");
			builder.append(activity);
			builder.append("]");
			return builder.toString();
		}

	}
}
