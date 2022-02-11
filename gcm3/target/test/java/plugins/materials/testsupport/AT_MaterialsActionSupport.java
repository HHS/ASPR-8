package plugins.materials.testsupport;

import util.annotations.UnitTest;

@UnitTest(target = MaterialsActionSupport.class)
public class AT_MaterialsActionSupport {

//	/**
//	 * Creates an action plugin with an agent that will execute the given
//	 * consumer at time 0. The action plugin and the remaining arguments are
//	 * passed to an invocation of the testConsumers() method.
//	 */
//	public static void testConsumer(long seed, Consumer<AgentContext> consumer) {
//		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
//		pluginBuilder.addAgent("agent");
//		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
//		testConsumers(seed, pluginBuilder.build());
//	}
//
//	/**
//	 * Executes a simulation instance that supports materials plugin testing.
//	 * 
//	 * The initial population is added in the initial data.
//	 * 
//	 * Materials, Materials Producers and their associated properties are added.
//	 * No batches or stages are created. Materials producer resource levels are
//	 * zero.
//	 * 
//	 * Resources and their property definitions and initial values are added. No
//	 * resources are allocated to regions or people.
//	 * 
//	 * The seed is used to produce randomized initial group types and group
//	 * memberships.
//	 * 
//	 * The action plugin is integrated into the simulation run and must contain
//	 * at least one action plan. This helps to ensure that a test that does not
//	 * run completely does not lead to a false positive test evaluation.
//	 * 
//	 * @throws ContractException
//	 *             <li>{@linkplain ActionError#ACTION_EXECUTION_FAILURE} if not
//	 *             all action plans execute or if there are no action plans
//	 *             contained in the action plugin</li>
//	 */
//	public static void testConsumers(long seed, ActionPlugin actionPlugin, Consumer<Object> outputConsumer, Consumer<ReportContext> report) {
//		_testConsumers(seed, actionPlugin, outputConsumer, report);
//	}
//
//	public static void testConsumers(long seed, ActionPlugin actionPlugin) {
//		_testConsumers(seed, actionPlugin, null, null);
//	}
//
//	private static void _testConsumers(long seed, ActionPlugin actionPlugin, Consumer<Object> outputConsumer, Consumer<ReportContext> report) {
//
//		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(seed);
//
//		EngineBuilder engineBuilder = Engine.builder();
//
//		MaterialsInitialData.Builder materialsBuilder = MaterialsInitialData.builder();
//
//		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
//			materialsBuilder.addMaterial(testMaterialId);
//		}
//
//		for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
//			materialsBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new ActionAgent(testMaterialsProducerId)::init);
//		}
//
//		for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
//			materialsBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, testMaterialsProducerPropertyId.getPropertyDefinition());
//		}
//
//		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
//			Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
//			for (TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
//				materialsBuilder.defineBatchProperty(testMaterialId, testBatchPropertyId, testBatchPropertyId.getPropertyDefinition());
//			}
//		}
//
//		engineBuilder.addPlugin(MaterialsPlugin.PLUGIN_ID, new MaterialsPlugin(materialsBuilder.build())::init);
//
//		// add the resources plugin
//		ResourceInitialData.Builder resourcesBuilder = ResourceInitialData.builder();
//
//		for (TestResourceId testResourceId : TestResourceId.values()) {
//			resourcesBuilder.addResource(testResourceId);
//			resourcesBuilder.setResourceTimeTracking(testResourceId, testResourceId.getTimeTrackingPolicy());
//		}
//
//		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
//			TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
//			PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
//			Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
//			resourcesBuilder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
//			resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
//		}
//
//		engineBuilder.addPlugin(ResourcesPlugin.PLUGIN_ID, new ResourcesPlugin(resourcesBuilder.build())::init);
//
//		// add the partitions plugin
//		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);
//
//		// add the people plugin
//
//		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
//		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);
//
//		// add the properties plugin
//		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
//
//		// add the compartments plugin
//		CompartmentInitialData.Builder compartmentsBuilder = CompartmentInitialData.builder();
//		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
//			compartmentsBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> new ActionAgent(testCompartmentId)::init);
//		}
//
//		engineBuilder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentsBuilder.build())::init);
//
//		// add the regions plugin
//		RegionInitialData.Builder regionsBuilder = RegionInitialData.builder();
//		for (TestRegionId testRegionId : TestRegionId.values()) {
//			regionsBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
//		}
//
//		engineBuilder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionsBuilder.build())::init);
//
//		// add the report plugin
//
//		ReportsInitialData.Builder reportsBuilder = ReportsInitialData.builder();
//		if (report != null) {
//			reportsBuilder.addReport(new SimpleReportId("report"), () -> report);
//		}
//
//		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(reportsBuilder.build())::init);
//
//		// add the component plugin
//		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
//
//		// add the stochastics plugin
//		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);
//
//		// add the action plugin
//		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);
//
//		// set the output consumer
//		engineBuilder.setOutputConsumer(outputConsumer);
//
//		// build and execute the engine
//		engineBuilder.build().execute();
//
//		// show that all actions were executed
//		if (!actionPlugin.allActionsExecuted()) {
//			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
//		}
//	}

}
