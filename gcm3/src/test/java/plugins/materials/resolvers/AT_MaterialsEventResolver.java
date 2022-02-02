package plugins.materials.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.AgentId;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.components.datacontainers.ComponentDataView;
import plugins.materials.MaterialsPlugin;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.mutation.BatchConstructionEvent;
import plugins.materials.events.mutation.BatchContentShiftEvent;
import plugins.materials.events.mutation.BatchCreationEvent;
import plugins.materials.events.mutation.BatchPropertyValueAssignmentEvent;
import plugins.materials.events.mutation.BatchRemovalRequestEvent;
import plugins.materials.events.mutation.MaterialsProducerPropertyValueAssignmentEvent;
import plugins.materials.events.mutation.MoveBatchToInventoryEvent;
import plugins.materials.events.mutation.MoveBatchToStageEvent;
import plugins.materials.events.mutation.OfferedStageTransferToMaterialsProducerEvent;
import plugins.materials.events.mutation.ProducedResourceTransferToRegionEvent;
import plugins.materials.events.mutation.StageCreationEvent;
import plugins.materials.events.mutation.StageOfferEvent;
import plugins.materials.events.mutation.StageRemovalRequestEvent;
import plugins.materials.events.mutation.StageToBatchConversionEvent;
import plugins.materials.events.mutation.StageToResourceConversionEvent;
import plugins.materials.events.observation.BatchAmountChangeObservationEvent;
import plugins.materials.events.observation.BatchCreationObservationEvent;
import plugins.materials.events.observation.BatchImminentRemovalObservationEvent;
import plugins.materials.events.observation.BatchPropertyChangeObservationEvent;
import plugins.materials.events.observation.MaterialsProducerPropertyChangeObservationEvent;
import plugins.materials.events.observation.MaterialsProducerResourceChangeObservationEvent;
import plugins.materials.events.observation.StageCreationObservationEvent;
import plugins.materials.events.observation.StageImminentRemovalObservationEvent;
import plugins.materials.events.observation.StageMaterialsProducerChangeObservationEvent;
import plugins.materials.events.observation.StageMembershipAdditionObservationEvent;
import plugins.materials.events.observation.StageMembershipRemovalObservationEvent;
import plugins.materials.events.observation.StageOfferChangeObservationEvent;
import plugins.materials.initialdata.MaterialsInitialData;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.resources.ResourcesPlugin;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.observation.RegionResourceChangeObservationEvent;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsDataView;
import plugins.stochastics.StochasticsPlugin;
import util.ContractException;
import util.MultiKey;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = MaterialsEventResolver.class)

public final class AT_MaterialsEventResolver {

	@Test
	@UnitTestConstructor(args = { MaterialsInitialData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new MaterialsEventResolver(null));
		assertEquals(MaterialsError.NULL_MATERIALS_INITIAL_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testMaterialsProducerInitialization() {

		// show that the materials producers exist as agents
		MaterialsActionSupport.testConsumer(8739655321932621979L, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

				// convert the compartment id into its corresponding agent id
				ComponentDataView componentDataView = c.getDataView(ComponentDataView.class).get();
				AgentId agentId = componentDataView.getAgentId(testMaterialsProducerId);
				assertNotNull(agentId);

				/*
				 * Ask the context if the agent exists. Note that it is possible
				 * that we can convert the component id to an agent id without
				 * the agent existing.
				 */
				assertTrue(c.agentExists(agentId));
			}
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testMaterialsDataViewInitialization() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5272599676969321594L);

		Builder builder = Simulation.builder();

		/*
		 * Add the materials plugin, utilizing all materials initial data
		 * methods to provide sufficient variety to test the proper loading of
		 * initial data. Note that stage and batch ids do not start with zero.
		 * This will force the renumbering of all batches and stages and
		 * complicate the testing a bit, but will show that the resolver is
		 * correctly renumbering the ids.
		 */
		MaterialsInitialData.Builder materialsBuilder = MaterialsInitialData.builder();

		int baseBatchId = 7;
		int rollingBatchId = baseBatchId;

		int baseStageId = 13;
		int rollingStageId = baseStageId;

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			List<BatchId> batches = new ArrayList<>();

			for (int i = 0; i < 50; i++) {

				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				double amount = randomGenerator.nextDouble();
				BatchId batchId = new BatchId(rollingBatchId++);
				materialsBuilder.addBatch(batchId, testMaterialId, amount, testMaterialsProducerId);
				batches.add(batchId);
				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					if (randomGenerator.nextBoolean()) {
						materialsBuilder.setBatchPropertyValue(batchId, testBatchPropertyId, testBatchPropertyId.getRandomPropertyValue(randomGenerator));
					}
				}

			}

			List<StageId> stages = new ArrayList<>();

			for (int i = 0; i < 10; i++) {
				StageId stageId = new StageId(rollingStageId++);
				stages.add(stageId);
				boolean offered = i % 2 == 0;
				materialsBuilder.addStage(stageId, offered, testMaterialsProducerId);
			}

			Collections.shuffle(batches, new Random(randomGenerator.nextLong()));
			for (int i = 0; i < 30; i++) {
				BatchId batchId = batches.get(i);
				StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
				materialsBuilder.addBatchToStage(stageId, batchId);
			}
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			materialsBuilder.addMaterial(testMaterialId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			materialsBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new ActionAgent(testMaterialsProducerId)::init);
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
				if (randomGenerator.nextBoolean()) {
					materialsBuilder.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId,
							testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator));
				}
			}
			for (TestResourceId testResourceId : TestResourceId.values()) {
				if (randomGenerator.nextBoolean()) {
					long value = randomGenerator.nextInt(15) + 1;
					materialsBuilder.setMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId, value);
				}
			}
		}

		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			materialsBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
			for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
				materialsBuilder.defineBatchProperty(testMaterialId, testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
			}
		}
		MaterialsInitialData materialsInitialData = materialsBuilder.build();

		builder.addPlugin(MaterialsPlugin.PLUGIN_ID, new MaterialsPlugin(materialsInitialData)::init);

		// add the resources plugin
		ResourceInitialData.Builder resourcesBuilder = ResourceInitialData.builder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			resourcesBuilder.addResource(testResourceId);
			resourcesBuilder.setResourceTimeTracking(testResourceId, testResourceId.getTimeTrackingPolicy());
		}

		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
			PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
			Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
			resourcesBuilder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
		}

		builder.addPlugin(ResourcesPlugin.PLUGIN_ID, new ResourcesPlugin(resourcesBuilder.build())::init);

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		// add the people plugin

		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the compartments plugin
		CompartmentInitialData.Builder compartmentsBuilder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentsBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> new ActionAgent(testCompartmentId)::init);
		}

		builder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentsBuilder.build())::init);

		// add the regions plugin
		RegionInitialData.Builder regionsBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}

		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionsBuilder.build())::init);

		// add the report plugin

		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);

		// add the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(randomGenerator.nextLong()).build()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create an agent to show that materials initial data was properly
		// loaded as reflected in the data view
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();

			// show that the correct materials producer ids are present
			assertEquals(materialsInitialData.getMaterialsProducerIds(), materialsDataView.getMaterialsProducerIds());

			// show that the correct material ids are present
			assertEquals(materialsInitialData.getMaterialIds(), materialsDataView.getMaterialIds());

			// show that the resource ids used for initial resource levels are
			// all contained in the resource plugin
			assertTrue(resourceDataView.getResourceIds().containsAll(materialsInitialData.getResourceIds()));

			// show that the material property ids are correct
			assertEquals(materialsInitialData.getMaterialsProducerPropertyIds(), materialsDataView.getMaterialsProducerPropertyIds());

			// show that the material producer property definitions are correct
			for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsInitialData.getMaterialsProducerPropertyIds()) {
				assertEquals(materialsInitialData.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId),
						materialsDataView.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId));
			}

			/*
			 * Show that the material producer property values are correct. Show
			 * that the initial resource levels are correct.
			 */
			for (MaterialsProducerId materialsProducerId : materialsInitialData.getMaterialsProducerIds()) {
				for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsInitialData.getMaterialsProducerPropertyIds()) {
					Object expectedValue = materialsInitialData.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
					Object actualValue = materialsDataView.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
					assertEquals(expectedValue, actualValue);
				}
				for (ResourceId resourceId : resourceDataView.getResourceIds()) {
					Long expectedResourceLevel = materialsInitialData.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
					Long actualResourceLevel = materialsDataView.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
					assertEquals(expectedResourceLevel, actualResourceLevel);
				}
			}
			/*
			 * Show that each material is associated with the correct batch
			 * property ids. Show that each batch property id has the correct
			 * definition.
			 */
			for (MaterialId materialId : materialsInitialData.getMaterialIds()) {
				assertEquals(materialsInitialData.getBatchPropertyIds(materialId), materialsDataView.getBatchPropertyIds(materialId));
				for (BatchPropertyId batchPropertyId : materialsInitialData.getBatchPropertyIds(materialId)) {
					PropertyDefinition expectedPropertyDefinition = materialsInitialData.getBatchPropertyDefinition(materialId, batchPropertyId);
					PropertyDefinition actualPropertyDefinition = materialsDataView.getBatchPropertyDefinition(materialId, batchPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
				}
			}

			// Show that the correct initial batches are present with their
			// normalized id values.
			Set<BatchId> expectedBatchIds = new LinkedHashSet<>();
			for (BatchId batchId : materialsInitialData.getBatchIds()) {
				expectedBatchIds.add(new BatchId(batchId.getValue() - baseBatchId));
			}

			Set<BatchId> actualBatchIds = new LinkedHashSet<>();
			for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
				actualBatchIds.addAll(materialsDataView.getInventoryBatches(materialsProducerId));
				List<StageId> stages = materialsDataView.getStages(materialsProducerId);
				for (StageId stageId : stages) {
					actualBatchIds.addAll(materialsDataView.getStageBatches(stageId));
				}
			}
			assertEquals(expectedBatchIds, actualBatchIds);

			// Show that the batches have the correct material id, materials
			// producer and amounts

			for (BatchId batchId : materialsInitialData.getBatchIds()) {

				BatchId altBatchId = new BatchId(batchId.getValue() - baseBatchId);

				MaterialId expectedMaterialId = materialsInitialData.getBatchMaterial(batchId);
				MaterialId actualMaterialId = materialsDataView.getBatchMaterial(altBatchId);
				assertEquals(expectedMaterialId, actualMaterialId);

				MaterialsProducerId expectedMaterialsProducerId = materialsInitialData.getBatchMaterialsProducer(batchId);
				MaterialsProducerId actualMaterialsProducerId = materialsDataView.getBatchProducer(altBatchId);
				assertEquals(expectedMaterialsProducerId, actualMaterialsProducerId);

				double expectedAmount = materialsInitialData.getBatchAmount(batchId);
				double actualAmount = materialsDataView.getBatchAmount(altBatchId);
				assertEquals(expectedAmount, actualAmount);

				for (BatchPropertyId batchPropertyId : materialsInitialData.getBatchPropertyIds(expectedMaterialId)) {
					Object expectedValue = materialsInitialData.getBatchPropertyValue(batchId, batchPropertyId);
					Object actualValue = materialsDataView.getBatchPropertyValue(altBatchId, batchPropertyId);
					assertEquals(expectedValue, actualValue);
				}

			}

			// Show that the correct initial stages are present with their
			// normalized id values.
			Set<StageId> expectedStageIds = new LinkedHashSet<>();
			for (StageId stageId : materialsInitialData.getStageIds()) {
				expectedStageIds.add(new StageId(stageId.getValue() - baseStageId));
			}

			Set<StageId> actualStageIds = new LinkedHashSet<>();
			for (MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
				actualStageIds.addAll(materialsDataView.getStages(materialsProducerId));

			}
			assertEquals(expectedStageIds, actualStageIds);

			// show that the stage/batch relationship are correct after the id
			// normalization
			for (StageId stageId : materialsInitialData.getStageIds()) {
				StageId altStageId = new StageId(stageId.getValue() - baseStageId);
				MaterialsProducerId expectedMaterialsProducerId = materialsInitialData.getStageMaterialsProducer(stageId);
				MaterialsProducerId actualMaterialsProducerId = materialsDataView.getStageProducer(altStageId);
				assertEquals(expectedMaterialsProducerId, actualMaterialsProducerId);

				boolean expectedOfferState = materialsInitialData.isStageOffered(stageId);
				boolean actualOfferStage = materialsDataView.isStageOffered(altStageId);
				assertEquals(expectedOfferState, actualOfferStage);

				Set<BatchId> expectedBatches = new LinkedHashSet<>();
				for (BatchId batchId : materialsInitialData.getStageBatches(stageId)) {
					expectedBatches.add(new BatchId(batchId.getValue() - baseBatchId));
				}
				Set<BatchId> actualBatches = new LinkedHashSet<>(materialsDataView.getStageBatches(altStageId));
				assertEquals(expectedBatches, actualBatches);
			}

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		// add the action plugin
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		if (!actionPlugin.allActionsExecuted()) {
			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
		}

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageCreationObservationEventLabelers() {
		MaterialsActionSupport.testConsumer(645534075810555962L, (c) -> {
			EventLabeler<StageCreationObservationEvent> eventLabeler = StageCreationObservationEvent.getEventLabelerForAll();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageImminentRemovalObservationEventLabelers() {
		MaterialsActionSupport.testConsumer(645534075810555962L, (c) -> {
			EventLabeler<StageImminentRemovalObservationEvent> eventLabeler = StageImminentRemovalObservationEvent.getEventLabelerForAll();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageMembershipAdditionObservationEventLabelers() {
		MaterialsActionSupport.testConsumer(645534075810555962L, (c) -> {
			EventLabeler<StageMembershipAdditionObservationEvent> eventLabeler = StageMembershipAdditionObservationEvent.getEventLabelerForAll();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageMembershipRemovalObservationEventLabelers() {
		MaterialsActionSupport.testConsumer(645534075810555962L, (c) -> {
			EventLabeler<StageMembershipRemovalObservationEvent> eventLabeler = StageMembershipRemovalObservationEvent.getEventLabelerForAll();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testBatchPropertyChangeObservationEventLabelers() {
		MaterialsActionSupport.testConsumer(645534075810555962L, (c) -> {
			EventLabeler<BatchPropertyChangeObservationEvent> eventLabeler = BatchPropertyChangeObservationEvent.getEventLabelerForAll();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testBatchAmountChangeObservationEventLabelers() {
		MaterialsActionSupport.testConsumer(645534075810555962L, (c) -> {
			EventLabeler<BatchAmountChangeObservationEvent> eventLabeler = BatchAmountChangeObservationEvent.getEventLabelerForAll();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testBatchImminentRemovalObservationEventLabelers() {
		MaterialsActionSupport.testConsumer(645534075810555962L, (c) -> {
			EventLabeler<BatchImminentRemovalObservationEvent> eventLabeler = BatchImminentRemovalObservationEvent.getEventLabelerForAll();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageOfferChangeObservationEventLabelers() {
		MaterialsActionSupport.testConsumer(645534075810555962L, (c) -> {
			EventLabeler<StageOfferChangeObservationEvent> eventLabeler1 = StageOfferChangeObservationEvent.getEventLabelerForStage();
			assertNotNull(eventLabeler1);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler1));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<StageOfferChangeObservationEvent> eventLabeler2 = StageOfferChangeObservationEvent.getEventLabelerForAll();
			assertNotNull(eventLabeler2);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler2));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testBatchCreationObservationEventLabelers() {

		MaterialsActionSupport.testConsumer(6871307284439549691L, (c) -> {
			EventLabeler<BatchCreationObservationEvent> eventLabeler = BatchCreationObservationEvent.getEventLabelerForAll();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageMaterialsProducerChangeObservationEventLabelers() {

		MaterialsActionSupport.testConsumer(6871307284439549691L, (c) -> {
			EventLabeler<StageMaterialsProducerChangeObservationEvent> eventLabeler1 = StageMaterialsProducerChangeObservationEvent.getEventLabelerForAll();
			assertNotNull(eventLabeler1);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler1));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<StageMaterialsProducerChangeObservationEvent> eventLabeler2 = StageMaterialsProducerChangeObservationEvent.getEventLabelerForDestination();
			assertNotNull(eventLabeler2);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler2));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<StageMaterialsProducerChangeObservationEvent> eventLabeler3 = StageMaterialsProducerChangeObservationEvent.getEventLabelerForSource();
			assertNotNull(eventLabeler3);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler3));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<StageMaterialsProducerChangeObservationEvent> eventLabeler4 = StageMaterialsProducerChangeObservationEvent.getEventLabelerForStage();
			assertNotNull(eventLabeler4);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler4));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testMaterialsProducerPropertyChangeObservationEventLabelers() {
		/*
		 * Have the agent attempt to add the event labeler and show that a
		 * contract exception is thrown, indicating that the labeler was
		 * previously added by the resolver.
		 */
		MaterialsActionSupport.testConsumer(301724267100798742L, (c) -> {
			EventLabeler<MaterialsProducerPropertyChangeObservationEvent> eventLabeler = MaterialsProducerPropertyChangeObservationEvent.getEventLabelerForMaterialsProducerAndProperty();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testMaterialsProducerResourceChangeObservationEventLabelers() {

		/*
		 * Have the agent attempt to add the event labeler and show that a
		 * contract exception is thrown, indicating that the labeler was
		 * previously added by the resolver.
		 */
		MaterialsActionSupport.testConsumer(13119761810425715L, (c) -> {
			EventLabeler<MaterialsProducerResourceChangeObservationEvent> eventLabeler1 = MaterialsProducerResourceChangeObservationEvent.getEventLabelerForMaterialsProducerAndResource();
			assertNotNull(eventLabeler1);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler1));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

			EventLabeler<MaterialsProducerResourceChangeObservationEvent> eventLabeler2 = MaterialsProducerResourceChangeObservationEvent.getEventLabelerForResource();
			assertNotNull(eventLabeler2);
			contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler2));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testBatchConstructionEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create containers to hold observations
		Set<BatchId> expectedBatchObservations = new LinkedHashSet<>();
		Set<BatchId> actualBatchObservations = new LinkedHashSet<>();

		/* create an observer agent that will observe the batch creations */
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(BatchCreationObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualBatchObservations.add(e.getBatchId());
			});
		}));

		// create some batches and show that their various features are correct
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(1, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			for (int i = 0; i < 20; i++) {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				builder.setMaterialId(testMaterialId);
				double amount = randomGenerator.nextDouble();
				builder.setAmount(amount);//
				for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId)) {
					builder.setPropertyValue(testBatchPropertyId, testBatchPropertyId.getRandomPropertyValue(randomGenerator));
				}
				BatchConstructionInfo batchConstructionInfo = builder.build();//
				c.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));

				BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
				assertEquals(testMaterialId, materialsDataView.getBatchMaterial(batchId));
				assertEquals(amount, materialsDataView.getBatchAmount(batchId));

				expectedBatchObservations.add(batchId);
			}

		}));

		/*
		 * have the observer show that the observations are properly generated
		 */
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(2, (c) -> {
			assertTrue(expectedBatchObservations.size() > 0);
			assertEquals(expectedBatchObservations, actualBatchObservations);

		}));

		// precondition tests
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			// if the requesting agent is not a materials producer
			ContractException contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				c.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));
			});
			assertEquals(MaterialsError.MATERIALS_PRODUCER_REQUIRED, contractException.getErrorType());
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(4, (c) -> {
			// if the batch construction info in the event is null
			ContractException contractException = assertThrows(ContractException.class, () -> {
				c.resolveEvent(new BatchConstructionEvent(null));
			});
			assertEquals(MaterialsError.NULL_BATCH_CONSTRUCTION_INFO, contractException.getErrorType());

			// if the material id in the batch construction info is null
			contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setAmount(0.1234);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				c.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));
			});
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id in the batch construction info is unknown
			contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.getUnknownMaterialId());
				builder.setAmount(0.1234);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				c.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));
			});
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

			// if the amount in the batch construction info is not finite
			contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(Double.POSITIVE_INFINITY);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				c.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));
			});
			assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

			// if the amount in the batch construction info is negative
			contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(-1.0);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				c.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));
			});
			assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

			// if the batch construction info contains a null batch property id
			contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setPropertyValue(null, 15);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				c.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));
			});
			assertEquals(MaterialsError.NULL_BATCH_PROPERTY_ID, contractException.getErrorType());

			// if the batch construction info contains an unknown batch property
			// id
			contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setPropertyValue(TestBatchPropertyId.getUnknownBatchPropertyId(), 15);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				c.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));
			});
			assertEquals(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID, contractException.getErrorType());

			// if the batch construction info contains a null batch property
			// value
			contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setPropertyValue(TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, null);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				c.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));
			});
			assertEquals(MaterialsError.NULL_BATCH_PROPERTY_VALUE, contractException.getErrorType());

			// if the batch construction info contains a batch property value
			// that is incompatible with the corresponding property def
			contractException = assertThrows(ContractException.class, () -> {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				builder.setMaterialId(TestMaterialId.MATERIAL_1);
				builder.setAmount(0.1234);
				builder.setPropertyValue(TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 2.3);
				BatchConstructionInfo batchConstructionInfo = builder.build();
				c.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));
			});
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(1063265892920576062L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testBatchContentShiftEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		double actionTime = 0;

		// create data structures to

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe the movement of materials between batches
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			c.subscribe(BatchAmountChangeObservationEvent.getEventLabelByAll(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(e.getBatchId(), e.getPreviousAmount(), e.getCurrentAmount());
				actualObservations.add(multiKey);
			});
		}));

		// Have the materials producers create a few batches, ensuring the
		// amount in each batch is positive
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				for (TestMaterialId testMaterialId : TestMaterialId.values()) {
					for (int i = 0; i < 10; i++) {
						c.resolveEvent(new BatchCreationEvent(testMaterialId, randomGenerator.nextDouble() + 0.1));
					}
				}
			}));
		}

		/*
		 * We will concentrate on just producer 2 for most of the test but will
		 * utilize batches in other producers in some tests
		 */
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;

		// have the materials producer swap material amount around
		pluginBuilder.addAgentActionPlan(materialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			for (TestMaterialId testMaterialId : TestMaterialId.values()) {
				List<BatchId> batches = materialsDataView.getInventoryBatchesByMaterialId(materialsProducerId, testMaterialId);
				int index1 = randomGenerator.nextInt(batches.size());
				int index2 = randomGenerator.nextInt(batches.size());
				if (index2 == index1) {
					index2++;
					index2 = index2 % batches.size();
				}
				BatchId batchId1 = batches.get(index1);
				BatchId batchId2 = batches.get(index2);
				double batchAmount1 = materialsDataView.getBatchAmount(batchId1);
				double batchAmount2 = materialsDataView.getBatchAmount(batchId2);
				// ensure that we transfer a positive amount, but not the whole
				// amount
				double portion = randomGenerator.nextDouble() * 0.8 + 0.1;
				double transferAmount = batchAmount1 * portion;
				c.resolveEvent(new BatchContentShiftEvent(batchId1, batchId2, transferAmount));

				double batchAmount3 = materialsDataView.getBatchAmount(batchId1);
				double batchAmount4 = materialsDataView.getBatchAmount(batchId2);

				assertEquals(batchAmount1 - transferAmount, batchAmount3, 0.00000000001);
				assertEquals(batchAmount2 + transferAmount, batchAmount4, 0.00000000001);

				MultiKey multiKey = new MultiKey(batchId1, batchAmount1, batchAmount3);
				expectedObservations.add(multiKey);

				multiKey = new MultiKey(batchId2, batchAmount2, batchAmount4);
				expectedObservations.add(multiKey);
			}
		}));

		// Have the observer show that the observations were generated correctly
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(materialsProducerId, new AgentActionPlan(actionTime++, (c) -> {

			// if the source batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId destinationBatchId = materialsDataView.getLastIssuedBatchId().get();
				c.resolveEvent(new BatchContentShiftEvent(null, destinationBatchId, 0.1));
			});
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the source batch id is unknown
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				BatchId sourceBatchId = new BatchId(100000);
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId destinationBatchId = materialsDataView.getLastIssuedBatchId().get();
				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, 0.1));
			});
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

			// if the destination batch id is null
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();
				BatchId destinationBatchId = null;
				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, 0.1));
			});
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the destination batch id is unknown
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();
				BatchId destinationBatchId = new BatchId(10000000);
				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, 0.1));
			});
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

			// if the source and destination batches are the same
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();
				BatchId destinationBatchId = sourceBatchId;
				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, 0.1));
			});
			assertEquals(MaterialsError.REFLEXIVE_BATCH_SHIFT, contractException.getErrorType());

			// if the batches do not have the same material type
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_2, 1.0));
				BatchId destinationBatchId = materialsDataView.getLastIssuedBatchId().get();
				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, 0.1));
			});
			assertEquals(MaterialsError.MATERIAL_TYPE_MISMATCH, contractException.getErrorType());

			// if the batches have different owning materials producers
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();
				List<MaterialsProducerId> candidateProducerIds = new ArrayList<>(materialsDataView.getMaterialsProducerIds());
				candidateProducerIds.remove(materialsProducerId);
				MaterialsProducerId altMaterialsProducerId = candidateProducerIds.get(randomGenerator.nextInt(candidateProducerIds.size()));
				BatchId destinationBatchId = materialsDataView.getInventoryBatchesByMaterialId(altMaterialsProducerId, TestMaterialId.MATERIAL_1).get(0);
				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, 0.1));
			});
			assertEquals(MaterialsError.BATCH_SHIFT_WITH_MULTIPLE_OWNERS, contractException.getErrorType());

			// if the source batch is on a stage is offered
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();

				c.resolveEvent(new StageCreationEvent());
				StageId stageId = materialsDataView.getLastIssuedStageId().get();
				c.resolveEvent(new MoveBatchToStageEvent(sourceBatchId, stageId));
				c.resolveEvent(new StageOfferEvent(stageId, true));

				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId destinationBatchId = materialsDataView.getLastIssuedBatchId().get();
				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, 0.1));
			});
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

			// if the destination batch is on a stage is offered
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();

				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId destinationBatchId = materialsDataView.getLastIssuedBatchId().get();

				c.resolveEvent(new StageCreationEvent());
				StageId stageId = materialsDataView.getLastIssuedStageId().get();
				c.resolveEvent(new MoveBatchToStageEvent(destinationBatchId, stageId));
				c.resolveEvent(new StageOfferEvent(stageId, true));

				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, 0.1));
			});
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

			// if the shift amount is not a finite number
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();

				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId destinationBatchId = materialsDataView.getLastIssuedBatchId().get();

				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, Double.POSITIVE_INFINITY));
			});
			assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

			// if the shift amount is negative
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();

				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId destinationBatchId = materialsDataView.getLastIssuedBatchId().get();

				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, -0.5));
			});
			assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

			// if the shift amount exceeds the available material on the source
			// batch
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();

				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
				BatchId destinationBatchId = materialsDataView.getLastIssuedBatchId().get();

				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, 2.0));
			});
			assertEquals(MaterialsError.INSUFFICIENT_MATERIAL_AVAILABLE, contractException.getErrorType());

			// if the shift amount would cause an overflow on the destination
			// batch
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, Double.MAX_VALUE));
				BatchId sourceBatchId = materialsDataView.getLastIssuedBatchId().get();

				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, Double.MAX_VALUE));
				BatchId destinationBatchId = materialsDataView.getLastIssuedBatchId().get();
				double amount = Double.MAX_VALUE / 2;
				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, amount));
			});
			assertEquals(MaterialsError.MATERIAL_ARITHMETIC_EXCEPTION, contractException.getErrorType());

			// if the requesting agent is not the owning materials producer for
			// the two batches
			contractException = assertThrows(ContractException.class, () -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				List<MaterialsProducerId> candidateProducerIds = new ArrayList<>(materialsDataView.getMaterialsProducerIds());
				candidateProducerIds.remove(materialsProducerId);
				MaterialsProducerId altMaterialsProducerId = candidateProducerIds.get(randomGenerator.nextInt(candidateProducerIds.size()));

				List<BatchId> batches = materialsDataView.getInventoryBatchesByMaterialId(altMaterialsProducerId, TestMaterialId.MATERIAL_1);
				BatchId sourceBatchId = batches.get(0);
				BatchId destinationBatchId = batches.get(1);

				double batchAmount = materialsDataView.getBatchAmount(sourceBatchId);
				double transferAmount = (randomGenerator.nextDouble() * 0.8 + 0.1) * batchAmount;

				c.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, transferAmount));
			});
			assertEquals(MaterialsError.MATERIALS_OWNERSHIP, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(6185579658134885353L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testBatchCreationEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create containers to hold observations
		Set<BatchId> expectedBatchObservations = new LinkedHashSet<>();
		Set<BatchId> actualBatchObservations = new LinkedHashSet<>();

		/* create an observer agent that will observe the batch creations */
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			c.subscribe(BatchCreationObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualBatchObservations.add(e.getBatchId());
			});
		}));

		// create some batches and show that their various features are correct
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(1, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			for (int i = 0; i < 20; i++) {
				BatchConstructionInfo.Builder builder = BatchConstructionInfo.builder();
				TestMaterialId testMaterialId = TestMaterialId.getRandomMaterialId(randomGenerator);
				builder.setMaterialId(testMaterialId);
				double amount = randomGenerator.nextDouble();
				builder.setAmount(amount);//

				BatchCreationEvent batchCreationEvent = new BatchCreationEvent(testMaterialId, amount);
				c.resolveEvent(batchCreationEvent);

				BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
				assertEquals(testMaterialId, materialsDataView.getBatchMaterial(batchId));
				assertEquals(amount, materialsDataView.getBatchAmount(batchId));

				expectedBatchObservations.add(batchId);
			}

		}));

		/*
		 * have the observer show that the observations are properly generated
		 */
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(2, (c) -> {
			assertEquals(expectedBatchObservations, actualBatchObservations);

		}));

		// precondition tests
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			// if the requesting agent is not a materials producer
			ContractException contractException = assertThrows(ContractException.class, () -> {
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 0.1234));
			});
			assertEquals(MaterialsError.MATERIALS_PRODUCER_REQUIRED, contractException.getErrorType());
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(4, (c) -> {

			// if the material id in the batch construction info is null
			ContractException contractException = assertThrows(ContractException.class, () -> {
				c.resolveEvent(new BatchCreationEvent(null, 0.1234));
			});
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is unknown
			contractException = assertThrows(ContractException.class, () -> {
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.getUnknownMaterialId(), 0.1234));
			});
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

			// if the amount is not finite
			contractException = assertThrows(ContractException.class, () -> {
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, Double.POSITIVE_INFINITY));
			});
			assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

			// if the amount is negative
			contractException = assertThrows(ContractException.class, () -> {
				c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, -1.0));
			});
			assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(8688242236073344545L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testBatchRemovalRequestEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// construct data structures to hold observations
		Set<BatchId> expectedObservations = new LinkedHashSet<>();
		Set<BatchId> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			c.subscribe(BatchImminentRemovalObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(e.getBatchId());
			});
		}));

		/*
		 * Add batches -- although we will concentrate on producer 1, we will
		 * have the other producers generate batches for use in precondition
		 * tests
		 */
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					c.resolveEvent(new BatchCreationEvent(TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble()));
				}
			}));
		}

		// remove some batches
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			List<BatchId> inventoryBatches = materialsDataView.getInventoryBatches(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			for (BatchId batchId : inventoryBatches) {
				if (randomGenerator.nextBoolean()) {
					c.resolveEvent(new BatchRemovalRequestEvent(batchId));
					expectedObservations.add(batchId);
				}
			}
		}));

		// show that the batches were indeed removed
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			for (BatchId batchId : expectedObservations) {
				assertFalse(materialsDataView.batchExists(batchId));
			}
		}));

		// show that the observations were properly generated
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchRemovalRequestEvent(null)));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchRemovalRequestEvent(new BatchId(100000))));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

			// if the batch is on an offered stage
			c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 1.0));
			BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
			c.resolveEvent(new StageCreationEvent());
			StageId stageId = materialsDataView.getLastIssuedStageId().get();
			c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
			c.resolveEvent(new StageOfferEvent(stageId, true));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchRemovalRequestEvent(batchId)));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

			// if the requesting agent is not the owning materials producer
			List<MaterialsProducerId> candiateProducers = new ArrayList<>(materialsDataView.getMaterialsProducerIds());
			candiateProducers.remove(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			MaterialsProducerId materialsProducerId = candiateProducers.get(randomGenerator.nextInt(candiateProducers.size()));
			List<BatchId> inventoryBatches = materialsDataView.getInventoryBatches(materialsProducerId);
			BatchId batchId2 = inventoryBatches.get(randomGenerator.nextInt(inventoryBatches.size()));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchRemovalRequestEvent(batchId2)));
			assertEquals(MaterialsError.MATERIALS_OWNERSHIP, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(2869388661813620663L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testBatchPropertyValueAssignmentEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// create containers for observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			c.subscribe(BatchPropertyChangeObservationEvent.getEventLabelByAll(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getBatchId(), e.getBatchPropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue());
				actualObservations.add(multiKey);
			});
		}));

		// create some batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				for (int i = 0; i < 10; i++) {
					c.resolveEvent(new BatchCreationEvent(TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble() + 0.01));
				}
			}));
		}

		// Alter about half of the mutable properties of batches over 5
		// different times
		for (int i = 0; i < 5; i++) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
					StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
					RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
					MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
					List<BatchId> inventoryBatches = materialsDataView.getInventoryBatches(testMaterialsProducerId);
					for (BatchId batchId : inventoryBatches) {
						TestMaterialId batchMaterial = materialsDataView.getBatchMaterial(batchId);
						for (TestBatchPropertyId testBatchPropertyId : TestBatchPropertyId.getTestBatchPropertyIds(batchMaterial)) {
							if (testBatchPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
								if (randomGenerator.nextBoolean()) {
									Object newPropertyValue = testBatchPropertyId.getRandomPropertyValue(randomGenerator);
									Object oldPropertyValue = materialsDataView.getBatchPropertyValue(batchId, testBatchPropertyId);
									expectedObservations.add(new MultiKey(c.getTime(), batchId, testBatchPropertyId, oldPropertyValue, newPropertyValue));
									c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, testBatchPropertyId, newPropertyValue));
									// show that the new property value is
									// present
									assertEquals(newPropertyValue, materialsDataView.getBatchPropertyValue(batchId, testBatchPropertyId));
								}
							}
						}
					}
				}));
			}
		}

		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			c.resolveEvent(new BatchCreationEvent(materialId, 1.0));
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
			BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
			Object propertyValue = 56;

			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchPropertyValueAssignmentEvent(null, batchPropertyId, propertyValue)));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchPropertyValueAssignmentEvent(new BatchId(100000), batchPropertyId, propertyValue)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

			// if the batch property id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, null, propertyValue)));
			assertEquals(MaterialsError.NULL_BATCH_PROPERTY_ID, contractException.getErrorType());

			// if the batch property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, TestBatchPropertyId.getUnknownBatchPropertyId(), propertyValue)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID, contractException.getErrorType());

			// if batch property is not mutable
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK, false)));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

			// if the batch property value is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, batchPropertyId, null)));
			assertEquals(MaterialsError.NULL_BATCH_PROPERTY_VALUE, contractException.getErrorType());

			// if the batch property value is not compatible with the
			// corresponding property definition
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, batchPropertyId, 12.4)));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

			// if the batch in on an offered stage
			c.resolveEvent(new StageCreationEvent());
			StageId stageId = materialsDataView.getLastIssuedStageId().get();
			c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
			c.resolveEvent(new StageOfferEvent(stageId, true));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, batchPropertyId, propertyValue)));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

			// if the requesting agent is not the owning materials producer
			List<BatchId> inventoryBatches = materialsDataView.getInventoryBatchesByMaterialId(TestMaterialsProducerId.MATERIALS_PRODUCER_2, materialId);
			BatchId batchId2 = inventoryBatches.get(0);
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId2, batchPropertyId, propertyValue)));
			assertEquals(MaterialsError.MATERIALS_OWNERSHIP, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(6834204103777199004L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testMaterialsProducerPropertyValueAssignmentEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe all changes to all producer property values
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					EventLabel<MaterialsProducerPropertyChangeObservationEvent> eventLabel = MaterialsProducerPropertyChangeObservationEvent.getEventLabelByMaterialsProducerAndProperty(c,
							testMaterialsProducerId, testMaterialsProducerPropertyId);
					c.subscribe(eventLabel, (c2, e) -> {
						MultiKey multiKey = new MultiKey(c2.getTime(), e.getMaterialsProducerId(), e.getMaterialsProducerPropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue());
						actualObservations.add(multiKey);
					});
				}
			}
		}));

		pluginBuilder.addAgent("agent");

		for (int i = 0; i < 200; i++) {
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {
				// pick a random materials producer property and update it to a
				// random value
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				MaterialsProducerId materialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
				Object oldValue = materialsDataView.getMaterialsProducerPropertyValue(materialsProducerId, testMaterialsProducerPropertyId);
				Object newValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				MaterialsProducerPropertyValueAssignmentEvent event = new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId, testMaterialsProducerPropertyId, newValue);
				c.resolveEvent(event);

				// show that the new value is present
				assertEquals(newValue, materialsDataView.getMaterialsProducerPropertyValue(materialsProducerId, testMaterialsProducerPropertyId));

				// record the expected observation
				MultiKey multiKey = new MultiKey(c.getTime(), materialsProducerId, testMaterialsProducerPropertyId, oldValue, newValue);
				expectedObservations.add(multiKey);
			}));
		}

		// have the observer show that the proper observations were generated
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {

			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
			MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
			Object propertyValue = 5;

			// if the materials producer id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new MaterialsProducerPropertyValueAssignmentEvent(null, materialsProducerPropertyId, propertyValue)));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producer id is unknown
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new MaterialsProducerPropertyValueAssignmentEvent(TestMaterialsProducerId.getUnknownMaterialsProducerId(), materialsProducerPropertyId, propertyValue)));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producer property id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId, null, propertyValue)));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

			// if the materials producer property id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(
					new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId(), propertyValue)));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, contractException.getErrorType());

			// if the materials producer property is immutable
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(
					new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId, TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_8_INTEGER_IMMUTABLE_NO_TRACK, propertyValue)));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

			// if the property value is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId, materialsProducerPropertyId, null)));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_VALUE, contractException.getErrorType());

			// if the property value is incompatible with the corresponding
			// property definition
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId, materialsProducerPropertyId, 12.5)));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(3888305479377600267L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testMoveBatchToInventoryEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an observer record batches being removed from stages
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			c.subscribe(StageMembershipRemovalObservationEvent.getEventLabelByAll(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getBatchId(), e.getStageId());
				actualObservations.add(multiKey);
			});
		}));

		/*
		 * Have the materials producers create batches and place about half of
		 * them on stages
		 */
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				for (int i = 0; i < 40; i++) {
					c.resolveEvent(new BatchCreationEvent(TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble() + 0.01));
				}

				for (int i = 0; i < 5; i++) {
					c.resolveEvent(new StageCreationEvent());
				}

				List<StageId> stages = materialsDataView.getStages(testMaterialsProducerId);
				List<BatchId> batches = materialsDataView.getInventoryBatches(testMaterialsProducerId);
				for (BatchId batchId : batches) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
					}
				}

			}));
		}

		/*
		 * Have the materials producers return some about half of the staged
		 * batches to inventory and show that the batches are now in inventory
		 */
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

				List<StageId> stages = materialsDataView.getStages(testMaterialsProducerId);
				for (StageId stageId : stages) {
					List<BatchId> batches = materialsDataView.getStageBatches(stageId);
					for (BatchId batchId : batches) {
						if (randomGenerator.nextBoolean()) {
							c.resolveEvent(new MoveBatchToInventoryEvent(batchId));
							// show that the batch was returned to inventory
							assertFalse(materialsDataView.getBatchStageId(batchId).isPresent());
							assertTrue(materialsDataView.getInventoryBatches(testMaterialsProducerId).contains(batchId));
							// create the observation
							MultiKey multiKey = new MultiKey(c.getTime(), batchId, stageId);
							expectedObservations.add(multiKey);
						}
					}
				}
			}));
		}

		// have the observer show that the correct observations were made
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {

			// create a batch
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_1, 5.0));
			BatchId batchId = materialsDataView.getLastIssuedBatchId().get();

			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToInventoryEvent(null)));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToInventoryEvent(new BatchId(10000000))));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

			// if the batch is not staged
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToInventoryEvent(batchId)));
			assertEquals(MaterialsError.BATCH_NOT_STAGED, contractException.getErrorType());

			// create a stage, place the batch on the stage and then offer the
			// stage
			c.resolveEvent(new StageCreationEvent());
			StageId stageId = materialsDataView.getLastIssuedStageId().get();
			c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
			c.resolveEvent(new StageOfferEvent(stageId, true));

			// if the stage containing the batch is offered
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToInventoryEvent(batchId)));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());

			// stop offering the stage so that another producer can attempt to
			// remove the batch from the stage
			c.resolveEvent(new StageOfferEvent(stageId, false));

		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_2, new AgentActionPlan(actionTime++, (c) -> {

			// if the requesting agent is not the owning materials producer

			// find a batch that was staged on a non-offered stage in another
			// producer
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			BatchId selectedBatchId = null;
			List<StageId> stages = materialsDataView.getStages(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			for (StageId stageId : stages) {
				if (!materialsDataView.isStageOffered(stageId)) {
					List<BatchId> batches = materialsDataView.getStageBatches(stageId);
					if (!batches.isEmpty()) {
						selectedBatchId = batches.get(0);
						break;
					}
				}
			}
			assertNotNull(selectedBatchId);
			BatchId batchId = selectedBatchId;
			// attempt to move that batch from its stage
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToInventoryEvent(batchId)));
			assertEquals(MaterialsError.MATERIALS_OWNERSHIP, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(5136057466059323708L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testMoveBatchToStageEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an observer record observations of batches being assigned to
		// stages
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			c.subscribe(StageMembershipAdditionObservationEvent.getEventLabelByAll(), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getBatchId(), e.getStageId());
				actualObservations.add(multiKey);
			});
		}));

		// have the producers create several batches and stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				for (int i = 0; i < 40; i++) {
					c.resolveEvent(new BatchCreationEvent(TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble() + 0.01));
				}
				for (int i = 0; i < 5; i++) {
					c.resolveEvent(new StageCreationEvent());
				}
			}));
		}

		// have the producers put about half of their batches onto stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				List<StageId> stages = materialsDataView.getStages(testMaterialsProducerId);
				List<BatchId> batches = materialsDataView.getInventoryBatches(testMaterialsProducerId);
				for (BatchId batchId : batches) {
					if (randomGenerator.nextBoolean()) {
						StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));

						// show that the batch is now on the stage
						Optional<StageId> optionalStageId = materialsDataView.getBatchStageId(batchId);
						assertTrue(optionalStageId.isPresent());
						StageId actualStageId = optionalStageId.get();
						assertEquals(stageId, actualStageId);

						// add the expected observation
						MultiKey multiKey = new MultiKey(c.getTime(), batchId, stageId);
						expectedObservations.add(multiKey);
					}
				}

			}));
		}
		// have the observer show that the correct observations were generated
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			c.resolveEvent(new BatchCreationEvent(TestMaterialId.MATERIAL_2, 5.6));
			BatchId batchId = materialsDataView.getLastIssuedBatchId().get();

			c.resolveEvent(new StageCreationEvent());
			StageId stageId = materialsDataView.getLastIssuedStageId().get();

			// if the batch id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToStageEvent(null, stageId)));
			assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

			// if the batch id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToStageEvent(new BatchId(100000000), stageId)));
			assertEquals(MaterialsError.UNKNOWN_BATCH_ID, contractException.getErrorType());

			// if the batch is already staged
			c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId)));
			assertEquals(MaterialsError.BATCH_ALREADY_STAGED, contractException.getErrorType());
			c.resolveEvent(new MoveBatchToInventoryEvent(batchId));

			// if the stage id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToStageEvent(batchId, null)));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToStageEvent(batchId, new StageId(10000000))));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

			// if the stage is offered
			c.resolveEvent(new StageOfferEvent(stageId, true));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId)));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
			c.resolveEvent(new StageOfferEvent(stageId, false));

			// if batch and stage do not have the same owning materials producer
			List<BatchId> foreignBatches = materialsDataView.getInventoryBatches(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
			assertFalse(foreignBatches.isEmpty());
			BatchId foreignBatchId = foreignBatches.get(0);
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToStageEvent(foreignBatchId, stageId)));
			assertEquals(MaterialsError.BATCH_STAGED_TO_DIFFERENT_OWNER, contractException.getErrorType());

			// if the requesting agent not the owning material producer
			List<StageId> foreignStages = materialsDataView.getStages(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
			assertFalse(foreignStages.isEmpty());
			StageId foreignStageId = foreignStages.get(0);
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new MoveBatchToStageEvent(foreignBatchId, foreignStageId)));
			assertEquals(MaterialsError.MATERIALS_OWNERSHIP, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(6845954292451913670L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testOfferedStageTransferToMaterialsProducerEvent() {
		//
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			c.subscribe(StageMaterialsProducerChangeObservationEvent.getEventLabelByAll(c), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c.getTime(), e.getStageId(), e.getPreviousMaterialsProducerId(), e.getCurrentMaterialsProducerId());
				actualObservations.add(multiKey);
			});

			c.subscribe(StageOfferChangeObservationEvent.getEventLabelByAll(c), (c2, e) -> {
				MultiKey multiKey = new MultiKey(c.getTime(), e.getStageId());
				actualObservations.add(multiKey);

			});
		}));

		int stagesPerProducer = 5;
		int transferCount = stagesPerProducer * TestMaterialsProducerId.values().length;

		// have the materials producers create a few offered stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				for (int i = 0; i < stagesPerProducer; i++) {
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					for (int j = 0; j < 3; j++) {
						c.resolveEvent(new BatchCreationEvent(TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble() + 0.01));
						BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
					}
					c.resolveEvent(new StageOfferEvent(stageId, true));
					MultiKey multiKey = new MultiKey(c.getTime(), stageId);
					expectedObservations.add(multiKey);
				}
			}));
		}

		// have an agent transfer offered stages
		pluginBuilder.addAgent("agent");
		for (int i = 0; i < transferCount; i++) {
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				// determine the stages to transfer

				List<StageId> stagesToTransfer = new ArrayList<>();
				for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
					List<StageId> offeredStages = materialsDataView.getOfferedStages(testMaterialsProducerId);
					stagesToTransfer.addAll(offeredStages);
				}
				StageId stageId = stagesToTransfer.get(randomGenerator.nextInt(stagesToTransfer.size()));

				// select a producer at random to receive the transfered
				// stage

				MaterialsProducerId stageProducer = materialsDataView.getStageProducer(stageId);
				List<MaterialsProducerId> candidateProducers = new ArrayList<>(materialsDataView.getMaterialsProducerIds());
				candidateProducers.remove(stageProducer);
				MaterialsProducerId altProducer = candidateProducers.get(randomGenerator.nextInt(candidateProducers.size()));

				// transfer the stage
				c.resolveEvent(new OfferedStageTransferToMaterialsProducerEvent(stageId, altProducer));

				// show that the stage was properly transferred
				assertEquals(altProducer, materialsDataView.getStageProducer(stageId));

				// record expected observations

				MultiKey multiKey = new MultiKey(c.getTime(), stageId, stageProducer, altProducer);
				expectedObservations.add(multiKey);

				multiKey = new MultiKey(c.getTime(), stageId);
				expectedObservations.add(multiKey);

			}));
		}

		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// test preconditions
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			// create an offered stage
			c.resolveEvent(new StageCreationEvent());
			StageId stageId = materialsDataView.getLastIssuedStageId().get();
			MaterialsProducerId altProducer = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			c.resolveEvent(new StageOfferEvent(stageId, true));

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new OfferedStageTransferToMaterialsProducerEvent(null, altProducer)));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new OfferedStageTransferToMaterialsProducerEvent(new StageId(10000000), altProducer)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

			// if the materials producer id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new OfferedStageTransferToMaterialsProducerEvent(stageId, null)));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producer id is unknown
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new OfferedStageTransferToMaterialsProducerEvent(stageId, TestMaterialsProducerId.getUnknownMaterialsProducerId())));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the stage is not offered
			c.resolveEvent(new StageOfferEvent(stageId, false));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new OfferedStageTransferToMaterialsProducerEvent(stageId, altProducer)));
			assertEquals(MaterialsError.UNOFFERED_STAGE_NOT_TRANSFERABLE, contractException.getErrorType());
			c.resolveEvent(new StageOfferEvent(stageId, true));

			// if the source and destination materials producers are the same
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new OfferedStageTransferToMaterialsProducerEvent(stageId, TestMaterialsProducerId.MATERIALS_PRODUCER_1)));
			assertEquals(MaterialsError.REFLEXIVE_STAGE_TRANSFER, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(3739485201643969207L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testProducedResourceTransferToRegionEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe resource transfers from producers to regions.
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			/*
			 * When transferring resources from a producer to a region, a
			 * RegionResourceAdditionEvent is generated. This is not an
			 * observation, but is the contracted reaction event that is then
			 * processed by the resources package. To assess that this event is
			 * indeed generated we could add a custom resolver, but choose
			 * instead to have the observer agent subscribe to the resulting
			 * RegionResourceChangeObservationEvent
			 */

			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					c.subscribe(RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, testRegionId, testResourceId), (c2, e) -> {
						MultiKey multiKey = new MultiKey(c.getTime(), e.getResourceId(), e.getRegionId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel());
						actualObservations.add(multiKey);
					});
				}
			}

			for (TestResourceId testResourceId : TestResourceId.values()) {
				c.subscribe(MaterialsProducerResourceChangeObservationEvent.getEventLabelByResource(c, testResourceId), (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.getResourceId(), e.getMaterialsProducerId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel());
					actualObservations.add(multiKey);
				});
			}

		}));

		// have the producers generate some resources
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				for (TestResourceId testResourceId : TestResourceId.values()) {
					if (randomGenerator.nextBoolean()) {
						c.resolveEvent(new StageCreationEvent());
						StageId stageId = materialsDataView.getLastIssuedStageId().get();
						long amount = (randomGenerator.nextInt(1000) + 100);
						c.resolveEvent(new StageToResourceConversionEvent(stageId, testResourceId, amount));

						MultiKey multiKey = new MultiKey(c.getTime(), testResourceId, testMaterialsProducerId, 0L, amount);
						expectedObservations.add(multiKey);
					}
				}

			}));
		}

		// have an agent distribute the resources over time
		pluginBuilder.addAgent("agent");
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {
					MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
					ResourceDataView resourceDataView = c.getDataView(ResourceDataView.class).get();
					StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
					RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
					long materialsProducerResourceLevel = materialsDataView.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
					if (materialsProducerResourceLevel > 0) {
						long amountToTransfer = randomGenerator.nextInt((int) materialsProducerResourceLevel) + 1;
						TestRegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
						long regionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, testResourceId);
						c.resolveEvent(new ProducedResourceTransferToRegionEvent(testMaterialsProducerId, testResourceId, regionId, amountToTransfer));

						// show that the resource was transfered
						long currentProducerResourceLevel = materialsDataView.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
						long currentRegionResourceLevel = resourceDataView.getRegionResourceLevel(regionId, testResourceId);

						assertEquals(materialsProducerResourceLevel - amountToTransfer, currentProducerResourceLevel);
						assertEquals(regionResourceLevel + amountToTransfer, currentRegionResourceLevel);

						// record the expected observations

						MultiKey multiKey = new MultiKey(c.getTime(), testResourceId, regionId, regionResourceLevel, currentRegionResourceLevel);
						expectedObservations.add(multiKey);
						multiKey = new MultiKey(c.getTime(), testResourceId, testMaterialsProducerId, materialsProducerResourceLevel, currentProducerResourceLevel);
						expectedObservations.add(multiKey);

					}
				}));
			}
		}

		// have the observer show that the observations are correct
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// preconditions
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_2, new AgentActionPlan(actionTime++, (c) -> {

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
			ResourceId resourceId = TestResourceId.RESOURCE_3;
			RegionId regionId = TestRegionId.REGION_4;
			long amountToTransfer = 45L;

			c.resolveEvent(new StageCreationEvent());
			StageId stageId = materialsDataView.getLastIssuedStageId().get();
			c.resolveEvent(new StageToResourceConversionEvent(stageId, resourceId, amountToTransfer));

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new ProducedResourceTransferToRegionEvent(materialsProducerId, null, regionId, amountToTransfer)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new ProducedResourceTransferToRegionEvent(materialsProducerId, TestResourceId.getUnknownResourceId(), regionId, amountToTransfer)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the region id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, null, amountToTransfer)));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, TestRegionId.getUnknownRegionId(), amountToTransfer)));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// if the materials producer id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ProducedResourceTransferToRegionEvent(null, resourceId, regionId, amountToTransfer)));
			assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials producer id is unknown
			contractException = assertThrows(ContractException.class,
					() -> c.resolveEvent(new ProducedResourceTransferToRegionEvent(TestMaterialsProducerId.getUnknownMaterialsProducerId(), resourceId, regionId, amountToTransfer)));
			assertEquals(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, contractException.getErrorType());

			// if the materials amount is negative
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, -1L)));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

			// if the materials amount exceeds the resource level of the
			// materials producer
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, amountToTransfer * 2)));
			assertEquals(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE, contractException.getErrorType());

			// if the materials amount would cause an overflow of the regions
			// resource level

			/*
			 * There is currently some of the resource present, so we will add
			 * half of the max value of long two times in a row. That will cause
			 * the region to overflow while keeping the producer from doing so
			 * 
			 */
			long hugeAmount = Long.MAX_VALUE / 2;
			c.resolveEvent(new StageCreationEvent());
			stageId = materialsDataView.getLastIssuedStageId().get();
			c.resolveEvent(new StageToResourceConversionEvent(stageId, resourceId, hugeAmount));
			c.resolveEvent(new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, hugeAmount));

			c.resolveEvent(new StageCreationEvent());
			stageId = materialsDataView.getLastIssuedStageId().get();
			c.resolveEvent(new StageToResourceConversionEvent(stageId, resourceId, hugeAmount));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, hugeAmount)));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(2289322490697828226L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageCreationEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe stage creations
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			c.subscribe(StageCreationObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getStageId()));
			});
		}));

		// produce stages at various times
		for (int i = 0; i < 10; i++) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
					MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();

					// show that the stage exists and belongs to the producer
					assertTrue(materialsDataView.stageExists(stageId));
					assertEquals(testMaterialsProducerId, materialsDataView.getStageProducer(stageId));

					// generated expected observations
					expectedObservations.add(new MultiKey(c.getTime(), stageId));
				}));
			}
		}

		// have the observer show that the observations are correct
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {
			// if the requesting agent is not a materials producer
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageCreationEvent()));
			assertEquals(MaterialsError.MATERIALS_PRODUCER_REQUIRED, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(1344617610771747654L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageRemovalRequestEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe stage creations
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {

			c.subscribe(StageImminentRemovalObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getStageId()));
			});

			c.subscribe(StageMembershipRemovalObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getBatchId(), e.getStageId()));
			});

			c.subscribe(BatchImminentRemovalObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getBatchId()));
			});
		}));

		// have the producers create some stages with batches
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();
					int batchCount = randomGenerator.nextInt(3);
					for (int j = 0; j < batchCount; j++) {
						c.resolveEvent(new BatchCreationEvent(TestMaterialId.getRandomMaterialId(randomGenerator), randomGenerator.nextDouble() + 0.01));
						BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
					}
				}
			}));
		}

		// have the producers destroy all their stages, returning about half of
		// the batches to inventory
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			
			List<StageId> stagesToConfirm = new ArrayList<>();
			List<BatchId> batchesToConfirm = new ArrayList<>();
			
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

				List<StageId> stages = materialsDataView.getStages(testMaterialsProducerId);
				for (StageId stageId : stages) {
					boolean destroyBatches = randomGenerator.nextBoolean();
					List<BatchId> stageBatches = materialsDataView.getStageBatches(stageId);
					c.resolveEvent(new StageRemovalRequestEvent(stageId, destroyBatches));
					
					/*
					 * record the batch and stage ids that will be removed after the current agent activation so that they can be confirmed later
					 */
					if (destroyBatches) {
						batchesToConfirm.addAll(stageBatches);						
					}
					stagesToConfirm.add(stageId);

					// show that the stage and batches were properly handled
					if (!destroyBatches) {
						List<BatchId> inventoryBatches = materialsDataView.getInventoryBatches(testMaterialsProducerId);
						for (BatchId batchId : stageBatches) {
							assertTrue(inventoryBatches.contains(batchId));
						}
					}					

					// generate the expected observations
					if (destroyBatches) {
						for (BatchId batchId : stageBatches) {
							expectedObservations.add(new MultiKey(c.getTime(), batchId));
						}
					} else {
						for (BatchId batchId : stageBatches) {
							expectedObservations.add(new MultiKey(c.getTime(), batchId, stageId));
						}
					}

					expectedObservations.add(new MultiKey(c.getTime(), stageId));

				}
			}));
			
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				for(StageId stageId : stagesToConfirm) {
					assertFalse(materialsDataView.stageExists(stageId));
				}			
				for(BatchId batchId : batchesToConfirm) {
					assertFalse(materialsDataView.batchExists(batchId));
				}			
			}));
			
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			c.resolveEvent(new StageCreationEvent());
			StageId stageId = materialsDataView.getLastIssuedStageId().get();

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageRemovalRequestEvent(null, false)));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageRemovalRequestEvent(new StageId(1000000), false)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

			// if stage is offered
			c.resolveEvent(new StageOfferEvent(stageId, true));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageRemovalRequestEvent(stageId, false)));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
			c.resolveEvent(new StageOfferEvent(stageId, false));
		}));

		// precondition tests
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {
			// get the stage created in the previous precondition test
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			List<StageId> stages = materialsDataView.getStages(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			assertFalse(stages.isEmpty());
			StageId stageId = stages.get(0);

			// if the requesting agent is not the owning materials producer id
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageRemovalRequestEvent(stageId, false)));
			assertEquals(MaterialsError.MATERIALS_OWNERSHIP, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(886447125697525680L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageOfferEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have a agent observe stage creations
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			c.subscribe(StageOfferChangeObservationEvent.getEventLabelByAll(c), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getStageId(), e.isPreviousOfferState(), e.isCurrentOfferState()));
			});
		}));

		// have the producers create a few stages
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				for (int i = 0; i < 5; i++) {
					c.resolveEvent(new StageCreationEvent());
				}
			}));
		}

		// have the producers make a few random offer state changes
		for (int i = 0; i < 10; i++) {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
					StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
					RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
					MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
					List<StageId> stages = materialsDataView.getStages(testMaterialsProducerId);
					StageId stageId = stages.get(randomGenerator.nextInt(stages.size()));
					boolean isOffered = materialsDataView.isStageOffered(stageId);
					c.resolveEvent(new StageOfferEvent(stageId, !isOffered));
					// show that the offer state changed
					boolean newOfferState = materialsDataView.isStageOffered(stageId);
					assertNotEquals(isOffered, newOfferState);

					// generate the expected observation
					expectedObservations.add(new MultiKey(c.getTime(), stageId, isOffered, !isOffered));
				}));
			}
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {

			// if the stage id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageOfferEvent(null, true)));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if the stage id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageOfferEvent(new StageId(10000000), true)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

		}));

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			List<StageId> stages = materialsDataView.getStages(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			assertFalse(stages.isEmpty());
			StageId stageId = stages.get(0);

			// if the requesting agent is not the owning materials producer
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageOfferEvent(stageId, true)));
			assertEquals(MaterialsError.MATERIALS_OWNERSHIP, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(916177724112971509L, actionPlugin);
	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageToBatchConversionEvent() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {

			c.subscribe(BatchImminentRemovalObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getBatchId(), "removal"));
			});

			c.subscribe(BatchCreationObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getBatchId(), "creation"));
			});

			c.subscribe(StageImminentRemovalObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getStageId()));
			});

		}));

		// have the producers generate batches via stage conversion
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			List<StageId> stagesToConfirm = new ArrayList<>();
			List<BatchId> batchesToConfirm = new ArrayList<>();
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();

					MaterialId materialId;
					double amount;
					int batchCount = randomGenerator.nextInt(3);
					for (int j = 0; j < batchCount; j++) {
						materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
						amount = randomGenerator.nextDouble() + 0.01;
						c.resolveEvent(new BatchCreationEvent(materialId, amount));
						BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
					}
					materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
					amount = randomGenerator.nextDouble() + 0.01;
					List<BatchId> stageBatches = materialsDataView.getStageBatches(stageId);
					c.resolveEvent(new StageToBatchConversionEvent(stageId, materialId, amount));

					// record the stages and batches that should be removed, but
					// only after the current agent activation
					stagesToConfirm.add(stageId);
					batchesToConfirm.addAll(stageBatches);

					// show that the stage was properly converted
					BatchId producedBatchId = materialsDataView.getLastIssuedBatchId().get();
					assertEquals(materialId, materialsDataView.getBatchMaterial(producedBatchId));
					assertEquals(amount, materialsDataView.getBatchAmount(producedBatchId));

					// generate the expected observations
					for (BatchId batchId : stageBatches) {
						expectedObservations.add(new MultiKey(c.getTime(), batchId, "creation"));
						expectedObservations.add(new MultiKey(c.getTime(), batchId, "removal"));
					}
					expectedObservations.add(new MultiKey(c.getTime(), producedBatchId, "creation"));
					expectedObservations.add(new MultiKey(c.getTime(), stageId));
				}
			}));

			// show that the stages and batches used to generate the new batches
			// were in fact removed
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				for (StageId stageId : stagesToConfirm) {
					assertFalse(materialsDataView.stageExists(stageId));
				}
				for (BatchId batchId : batchesToConfirm) {
					assertFalse(materialsDataView.batchExists(batchId));
				}
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			c.resolveEvent(new StageCreationEvent());
			StageId stageId = materialsDataView.getLastIssuedStageId().get();

			MaterialId materialId = TestMaterialId.MATERIAL_1;
			double amount = 12.5;

			// if the material id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageToBatchConversionEvent(stageId, null, amount)));
			assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

			// if the material id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageToBatchConversionEvent(stageId, TestMaterialId.getUnknownMaterialId(), amount)));
			assertEquals(MaterialsError.UNKNOWN_MATERIAL_ID, contractException.getErrorType());

			// if the stage id is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageToBatchConversionEvent(null, materialId, amount)));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());

			// if stage id is unknown
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageToBatchConversionEvent(new StageId(10000000), materialId, amount)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());

			// if the stage is offered
			c.resolveEvent(new StageOfferEvent(stageId, true));
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageToBatchConversionEvent(stageId, materialId, amount)));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
			c.resolveEvent(new StageOfferEvent(stageId, false));

			// if the material amount is not finite
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageToBatchConversionEvent(stageId, materialId, Double.POSITIVE_INFINITY)));
			assertEquals(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, contractException.getErrorType());

			// if the material amount is negative
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageToBatchConversionEvent(stageId, materialId, -1.0)));
			assertEquals(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, contractException.getErrorType());

		}));
		// precondition tests
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {

			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			List<StageId> stages = materialsDataView.getStages(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			assertFalse(stages.isEmpty());
			StageId stageId = stages.get(0);
			MaterialId materialId = TestMaterialId.MATERIAL_1;
			double amount = 12.5;

			// if the requesting agent is not the owning materials producer
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new StageToBatchConversionEvent(stageId, materialId, amount)));
			assertEquals(MaterialsError.MATERIALS_OWNERSHIP, contractException.getErrorType());

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(855059044560726814L, actionPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { AgentContext.class })
	public void testStageToResourceConversionEvent() {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		double actionTime = 0;

		// create containers to hold observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// have an agent observe
		pluginBuilder.addAgent("observer");
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {

			c.subscribe(BatchImminentRemovalObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getBatchId()));
			});

			for (TestResourceId testResourceId : TestResourceId.values()) {
				c.subscribe(MaterialsProducerResourceChangeObservationEvent.getEventLabelByResource(c, testResourceId), (c2, e) -> {
					actualObservations.add(new MultiKey(c2.getTime(), e.getMaterialsProducerId(), e.getResourceId(), e.getPreviousResourceLevel(), e.getCurrentResourceLevel()));
				});
			}

			c.subscribe(StageImminentRemovalObservationEvent.getEventLabelByAll(), (c2, e) -> {
				actualObservations.add(new MultiKey(c2.getTime(), e.getStageId()));
			});

		}));

		// have the producers generate resources via stage conversion
		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {

			List<StageId> stagesToConfirm = new ArrayList<>();
			List<BatchId> batchesToConfirm = new ArrayList<>();

			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {			
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
				RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
				for (int i = 0; i < 50; i++) {
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();

					MaterialId materialId;
					double amount;
					int batchCount = randomGenerator.nextInt(3);
					for (int j = 0; j < batchCount; j++) {
						materialId = TestMaterialId.getRandomMaterialId(randomGenerator);
						amount = randomGenerator.nextDouble() + 0.01;
						c.resolveEvent(new BatchCreationEvent(materialId, amount));
						BatchId batchId = materialsDataView.getLastIssuedBatchId().get();
						c.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));
					}
					ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);

					long resourceAmount = randomGenerator.nextInt(100) + 1;
					long previousResourceLevel = materialsDataView.getMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId);
					long expectedResourceLevel = previousResourceLevel + resourceAmount;
					List<BatchId> stageBatches = materialsDataView.getStageBatches(stageId);
					c.resolveEvent(new StageToResourceConversionEvent(stageId, resourceId, resourceAmount));

					// record the stages and batches that should be removed, but
					// only after the current agent activation
					stagesToConfirm.add(stageId);
					batchesToConfirm.addAll(stageBatches);

					// show that the stage was properly converted
					long currentResourceLevel = materialsDataView.getMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId);
					assertEquals(expectedResourceLevel, currentResourceLevel);

					// generate the expected observations
					for (BatchId batchId : stageBatches) {
						expectedObservations.add(new MultiKey(c.getTime(), batchId));
					}
					expectedObservations.add(new MultiKey(c.getTime(), testMaterialsProducerId, resourceId, previousResourceLevel, currentResourceLevel));
					expectedObservations.add(new MultiKey(c.getTime(), stageId));
				}
			}));

			// show that the stages and batches used to generate the new batches
			// were in fact removed
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				for (StageId stageId : stagesToConfirm) {
					assertFalse(materialsDataView.stageExists(stageId));
				}
				for (BatchId batchId : batchesToConfirm) {
					assertFalse(materialsDataView.batchExists(batchId));
				}
			}));
		}

		// have the observer show that the correct observations were generated
		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(actionTime++, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		// precondition tests
		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(actionTime++, (c) -> {
			
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			c.resolveEvent(new StageCreationEvent());
			StageId stageId = materialsDataView.getLastIssuedStageId().get();
			
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			long amount = 15L;
			
			// if the resource id is null 
			ContractException contractException = assertThrows(ContractException.class,()->c.resolveEvent(new StageToResourceConversionEvent(stageId, null, amount)));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class,()->c.resolveEvent(new StageToResourceConversionEvent(stageId, TestResourceId.getUnknownResourceId(), amount)));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());


			// if the stage id is null
			contractException = assertThrows(ContractException.class,()->c.resolveEvent(new StageToResourceConversionEvent(null, resourceId, amount)));
			assertEquals(MaterialsError.NULL_STAGE_ID, contractException.getErrorType());


			// if the stage id is unknown
			contractException = assertThrows(ContractException.class,()->c.resolveEvent(new StageToResourceConversionEvent(new StageId(10000000), resourceId, amount)));
			assertEquals(MaterialsError.UNKNOWN_STAGE_ID, contractException.getErrorType());


			// if the stage is offered
			c.resolveEvent(new StageOfferEvent(stageId, true));
			contractException = assertThrows(ContractException.class,()->c.resolveEvent(new StageToResourceConversionEvent(stageId, resourceId, amount)));
			assertEquals(MaterialsError.OFFERED_STAGE_UNALTERABLE, contractException.getErrorType());
			c.resolveEvent(new StageOfferEvent(stageId, false));
			
			// if the the resource amount is negative
			contractException = assertThrows(ContractException.class,()->c.resolveEvent(new StageToResourceConversionEvent(stageId, resourceId, -1L)));
			assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());


			// if the resource amount would cause an overflow of the materials
			// producer's resource level			
			
			//first ensure that there is some small amount of resource stored on the producer
			c.resolveEvent(new StageCreationEvent());
			StageId stageId2 = materialsDataView.getLastIssuedStageId().get();
			c.resolveEvent(new StageToResourceConversionEvent(stageId2, resourceId, 10));
			
			contractException = assertThrows(ContractException.class,()->c.resolveEvent(new StageToResourceConversionEvent(stageId, resourceId, Long.MAX_VALUE)));
			assertEquals(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION, contractException.getErrorType());

		}));

		
		// precondition tests
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			List<StageId> stages = materialsDataView.getStages(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
			assertFalse(stages.isEmpty());
			StageId stageId = stages.get(0);
			
			ResourceId resourceId = TestResourceId.RESOURCE_1;
			long amount = 15L;
			
			// if the requesting agent is not the owning materials producer 
			ContractException contractException = assertThrows(ContractException.class,()->c.resolveEvent(new StageToResourceConversionEvent(stageId, resourceId, amount)));
			assertEquals(MaterialsError.MATERIALS_OWNERSHIP, contractException.getErrorType());
			
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(7708324840412313909L, actionPlugin);

	}
}
