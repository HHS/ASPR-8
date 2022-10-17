package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.wrappers.MutableBoolean;

/**
 * Test unit for Engine. See the various Context tests for test's of engine's
 * implementation of internal simulation behaviors.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Simulation.class)
public class AT_Simulation {

	@Test
	@UnitTestMethod(name = "execute", args = {})
	public void testExecute() {

		// run the simulation
		Simulation simulation = Simulation.builder().build();
		simulation.execute();

		// precondition test
		ContractException contractException = assertThrows(ContractException.class, () -> simulation.execute());
		assertEquals(NucleusError.REPEATED_EXECUTION, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(Simulation.builder());
	}

	@Test
	@UnitTestMethod(target = Simulation.Builder.class, name = "build", args = {})
	public void testbuild() {
		assertNotNull(Simulation.builder().build());
	}

	private static class PluginData1 implements PluginData {
		@Override
		public PluginDataBuilder getCloneBuilder() {
			throw new UnsupportedOperationException();
		}
	}

	private static class PluginData2 implements PluginData {
		@Override
		public PluginDataBuilder getCloneBuilder() {
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

}
