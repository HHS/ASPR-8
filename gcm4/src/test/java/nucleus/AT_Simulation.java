package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;
import util.wrappers.MutableInteger;

public class AT_Simulation {

	@Test
	@UnitTestMethod(target = Simulation.class, name = "execute", args = {})
	public void testExecute() {

		// run the simulation
		Simulation simulation = Simulation.builder().build();
		simulation.execute();

		// precondition test
		ContractException contractException = assertThrows(ContractException.class, () -> simulation.execute());
		assertEquals(NucleusError.REPEATED_EXECUTION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = Simulation.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(Simulation.builder());
	}

	@Test
	@UnitTestMethod(target = Simulation.Builder.class, name = "build", args = {}, tags = { UnitTag.INCOMPLETE })
	public void testbuild() {
		/*
		 * There is no test that reflects circular dependency
		 */

		assertNotNull(Simulation.builder().build());
	}

	private static class PluginData1 implements PluginData {
		@Override
		public PluginDataBuilder getCloneBuilder() {
			throw new UnsupportedOperationException();
		}

		@Override
		public PluginDataBuilder getEmptyBuilder() {
			throw new UnsupportedOperationException();
		}
	}

	private static class PluginData2 implements PluginData {
		@Override
		public PluginDataBuilder getCloneBuilder() {
			throw new UnsupportedOperationException();
		}

		@Override
		public PluginDataBuilder getEmptyBuilder() {
			throw new UnsupportedOperationException();
		}
	}

	@Test
	@UnitTestMethod(target = Simulation.Builder.class, name = "addPlugin", args = { Plugin.class })
	public void testAddPlugin() {

		/*
		 * Show that the plugin is added correctly by showing that its init
		 * method is invoked and that the plugin data are available from the
		 * plugin context.
		 */

		MutableBoolean pluginAssertionsExecuted = new MutableBoolean();

		Plugin plugin = Plugin	.builder()//
								.setPluginId(new SimplePluginId("plugin"))//
								.addPluginData(new PluginData1())//
								.addPluginData(new PluginData2())//
								.setInitializer((c) -> {
									assertNotNull(c.getPluginData(PluginData1.class));
									assertNotNull(c.getPluginData(PluginData2.class));
									pluginAssertionsExecuted.setValue(true);
								})//
								.build();//

		Simulation	.builder()//
					.addPlugin(plugin)//
					.build()//
					.execute();

		/*
		 * Show that the initializer containing the assertions was executed
		 */
		assertTrue(pluginAssertionsExecuted.getValue());

	}

	private static class LocalOutputConsumer implements Consumer<Object> {

		private final Set<Object> receivedItems = new LinkedHashSet<>();

		@Override
		public void accept(Object t) {
			receivedItems.add(t);
		}

	}

	@Test
	@UnitTestMethod(target = Simulation.Builder.class, name = "setOutputConsumer", args = { Consumer.class })
	public void testSetOutputConsumer() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Case 1 : there is a non-null output consumer added to the builder
		 */

		Set<Object> expectedValues = new LinkedHashSet<>();
		expectedValues.add("A");
		expectedValues.add(4.5);
		expectedValues.add(424.75F);
		expectedValues.add(12);
		expectedValues.add(122423533423423453L);
		expectedValues.add(false);

		// have the added test agent produce some output
		pluginBuilder.addTestActorPlan("Alpha", new TestActorPlan(1, (context) -> {
			for (Object value : expectedValues) {
				context.releaseOutput(value);
			}
		}));

		// create two output consumers to show that the builder will only use
		// the last one
		LocalOutputConsumer localOutputConsumer1 = new LocalOutputConsumer();
		LocalOutputConsumer localOutputConsumer2 = new LocalOutputConsumer();

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// run the simulation
		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.setOutputConsumer(localOutputConsumer1)//
					.setOutputConsumer(localOutputConsumer2)//
					.build()//
					.execute();//

		// show that the first local output consumer did not receive any values
		assertTrue(localOutputConsumer1.receivedItems.isEmpty());

		// show that the second local output consumer received the expected
		// values
		assertTrue(localOutputConsumer2.receivedItems.containsAll(expectedValues));

		/*
		 * Case 2 : there is null output consumer added to the builder
		 */

		// show that the setting of null for the output consumer will yield no
		// output

		LocalOutputConsumer localOutputConsumer3 = new LocalOutputConsumer();

		Simulation	.builder()//
					.addPlugin(testPlugin)//
					.setOutputConsumer(localOutputConsumer3)//
					.setOutputConsumer(null)//
					.build()//
					.execute();//

		// show that the first local output consumer did not receive any values
		assertTrue(localOutputConsumer3.receivedItems.isEmpty());

	}

	
	//support class for testSetProduceSimulationStateOnHalt() test method
	private static class AlphaPluginDataBuilder implements PluginDataBuilder {
		private AlphaPluginData alphaPluginData = new AlphaPluginData();

		@Override
		public AlphaPluginData build() {
			try {
				return alphaPluginData;
			} finally {
				alphaPluginData = new AlphaPluginData();
			}
		}

		public AlphaPluginDataBuilder setX(int x) {
			alphaPluginData.x = x;
			return this;
		}

	}
	//support class for testSetProduceSimulationStateOnHalt() test method
	private static class AlphaPluginData implements PluginData {
		private int x;

		@Override
		public PluginDataBuilder getCloneBuilder() {
			AlphaPluginDataBuilder result = new AlphaPluginDataBuilder();
			result.setX(x);
			return result;
		}

		@Override
		public PluginDataBuilder getEmptyBuilder() {
			AlphaPluginDataBuilder result = new AlphaPluginDataBuilder();
			return result;
		}

		public int getX() {
			return x;
		}
	}
	//support class for testSetProduceSimulationStateOnHalt() test method
	private static class AlphaDataManager extends DataManager {
		private int x;

		public AlphaDataManager(AlphaPluginData alphaPluginData) {
			x = alphaPluginData.getX();
		}

		@Override
		public void init(DataManagerContext dataManagerContext) {
			super.init(dataManagerContext);
			dataManagerContext.subscribeToSimulationClose((c) -> {
				c.releaseOutput(new AlphaPluginDataBuilder().setX(x).build());
			});
		}

		public void setX(int x) {

			this.x = x;
		}

	}
	//support for testSetProduceSimulationStateOnHalt() test method
	private static final PluginId ALPHA_PLUGIN_ID = new SimplePluginId("Alpha Plugin Id");
	
	//support for testSetProduceSimulationStateOnHalt() test method
	private static Plugin getAlphaPlugin(AlphaPluginData alphaPluginData) {
		return Plugin	.builder()//
						.setPluginId(ALPHA_PLUGIN_ID)//
						.addPluginData(alphaPluginData)//
						.setInitializer(c -> {
							AlphaPluginData pluginData = c.getPluginData(AlphaPluginData.class).get();
							c.addDataManager(new AlphaDataManager(pluginData));
						}).build();//
	}

	@Test
	@UnitTestMethod(target = Simulation.Builder.class, name = "setProduceSimulationStateOnHalt", args = { boolean.class })
	public void testSetProduceSimulationStateOnHalt() {

		AlphaPluginData alphaPluginData = new AlphaPluginDataBuilder().setX(10).build();
		Plugin alphaPlugin = getAlphaPlugin(alphaPluginData);

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		MutableInteger rollingXValue = new MutableInteger();

		// have actor set the value of x a few times
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			AlphaDataManager alphaDataManager = c.getDataManager(AlphaDataManager.class);
			rollingXValue.setValue(55);
			alphaDataManager.setX(rollingXValue.getValue());

		}));
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			AlphaDataManager alphaDataManager = c.getDataManager(AlphaDataManager.class);
			rollingXValue.setValue(12);
			alphaDataManager.setX(rollingXValue.getValue());
		}));
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			AlphaDataManager alphaDataManager = c.getDataManager(AlphaDataManager.class);
			rollingXValue.setValue(87);
			alphaDataManager.setX(rollingXValue.getValue());
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		LocalDate localDate = LocalDate.of(2023, 03, 11);
		SimulationTime startingSimulationTime = SimulationTime.builder().setBaseDate(localDate).build();

		// run the simulation
		Simulation	.builder()//
					.addPlugin(alphaPlugin)//
					.addPlugin(testPlugin)//
					.setSimulationTime(startingSimulationTime)//
					.setOutputConsumer(testOutputConsumer)//
					.setProduceSimulationStateOnHalt(true)//
					.setSimulationHaltTime(20)//
					.build()//
					.execute();//

		// show that the simulation time data is correct
		Map<SimulationTime, Integer> simulationTimeItems = testOutputConsumer.getOutputItems(SimulationTime.class);
		assertEquals(1, simulationTimeItems.size());
		SimulationTime simulationTime = simulationTimeItems.keySet().iterator().next();
		Integer count = simulationTimeItems.get(simulationTime);
		assertEquals(1, count);
		assertEquals(localDate, simulationTime.getBaseDate());
		assertEquals(3, simulationTime.getStartTime());

		// show that there are two plugins and that the AlphaPluginData contains
		// the last value of x
		Map<AlphaPluginData, Integer> pluginDataItems = testOutputConsumer.getOutputItems(AlphaPluginData.class);
		
		assertEquals(1, pluginDataItems.size());
		AlphaPluginData outputAlphaPluginData = pluginDataItems.keySet().iterator().next();
		assertEquals(rollingXValue.getValue(), outputAlphaPluginData.getX());

		// show that if we explicitly set the production to false that nothing
		// is produced
		testOutputConsumer = new TestOutputConsumer();
		Simulation	.builder()//
					.addPlugin(alphaPlugin)//
					.addPlugin(testPlugin)//
					.setSimulationTime(startingSimulationTime)//
					.setOutputConsumer(testOutputConsumer)//
					.setProduceSimulationStateOnHalt(false)//
					.build()//
					.execute();//

		assertTrue(testOutputConsumer.getOutputItems(SimulationTime.class).isEmpty());
		assertTrue(testOutputConsumer.getOutputItems(Plugin.class).isEmpty());

		// show that if we do not set the production to false that nothing
		// is produced
		testOutputConsumer = new TestOutputConsumer();
		Simulation	.builder()//
					.addPlugin(alphaPlugin)//
					.addPlugin(testPlugin)//
					.setSimulationTime(startingSimulationTime)//
					.setOutputConsumer(testOutputConsumer)//					
					.build()//
					.execute();//

		assertTrue(testOutputConsumer.getOutputItems(SimulationTime.class).isEmpty());
		assertTrue(testOutputConsumer.getOutputItems(Plugin.class).isEmpty());

	}

}
