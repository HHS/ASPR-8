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
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestPluginInitializer;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

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

	@Test
	@UnitTestMethod(target = Simulation.Builder.class, name = "addPluginInitializer", args = { PluginInitializer.class })
	public void testAddPluginInitializer() {
		/*
		 * Add three initializers to the sim
		 */

		PluginInitializer2 initializer1 = new PluginInitializer2();
		PluginInitializer2 initializer2 = new PluginInitializer2();
		PluginInitializer2 initializer3 = new PluginInitializer2();

		Simulation	.builder()//					
					.addPluginInitializer(initializer1)//
					.addPluginInitializer(initializer2)//
					.addPluginInitializer(initializer3)//
					.build()//
					.execute();
		
		

		/*
		 * Show that each initializer was executed and thus was indeed added
		 */
		assertTrue(initializer1.executed);
		assertTrue(initializer2.executed);
		assertTrue(initializer3.executed);
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

	private static class PluginInitializer1 implements PluginInitializer {

		private boolean executedAssertions;

		@Override
		public PluginId getPluginId() {
			return new SimplePluginId(PluginInitializer1.class);
		}

		@Override
		public void init(PluginContext pluginContext) {
			assertTrue(pluginContext.getPluginData(PluginData1.class).isPresent());
			assertTrue(pluginContext.getPluginData(PluginData2.class).isPresent());
			executedAssertions = true;
		}

	}

	private static class PluginInitializer2 implements PluginInitializer {
		
		private boolean executed;

		@Override
		public PluginId getPluginId() {
			return new SimplePluginId(PluginInitializer2.class);
		}
		@Override
		public void init(PluginContext pluginContext) {
			executed = true;
			
		}
		
	}
	
	@Test
	@UnitTestMethod(target = Simulation.Builder.class, name = "addPluginData", args = { PluginData.class })
	public void testAddPluginData() {

		/*
		 * Show that the plugin data items are retrievable by the given
		 * initializer which contains the necessary assertions
		 */

		PluginInitializer1 pluginInitializer1 = new PluginInitializer1();

		Simulation	.builder()//
					.addPluginData(new PluginData1())//
					.addPluginData(new PluginData2())//
					.addPluginInitializer(pluginInitializer1)//
					.build()//
					.execute();

		/*
		 * Examine the plugin initializer to show that the assertions that show
		 * that the two plugin data items were executed
		 */
		assertTrue(pluginInitializer1.executedAssertions);

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

		// run the simulation
		Simulation	.builder()//
					.addPluginInitializer(new TestPluginInitializer())//
					.addPluginData(testPluginData)//
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
					.addPluginInitializer(new TestPluginInitializer())//
					.addPluginData(testPluginData)//
					.setOutputConsumer(localOutputConsumer3)//
					.setOutputConsumer(null)//
					.build()//
					.execute();//

		// show that the first local output consumer did not receive any values
		assertTrue(localOutputConsumer3.receivedItems.isEmpty());

	}

}
