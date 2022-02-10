package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
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
		Builder builder = Simulation.builder();
		assertNotNull(builder);
	}

	private static class TestPlugin {
		public PluginId getPluginId() {
			return new SimplePluginId(this.getClass());
		}

		public void init(PluginContext pluginContext) {

		}
	}

	private static class PluginA extends TestPlugin {

	};

	private static class PluginB extends TestPlugin {

		@Override
		public void init(PluginContext pluginContext) {
			pluginContext.addPluginDependency(new PluginA().getPluginId());
		}

	};

	private static class PluginC extends TestPlugin {
		@Override
		public void init(PluginContext pluginContext) {
			pluginContext.addPluginDependency(new PluginA().getPluginId());
			pluginContext.addPluginDependency(new PluginD().getPluginId());
		}

	};

	private static class PluginD extends TestPlugin {

		@Override
		public void init(PluginContext pluginContext) {
			pluginContext.addPluginDependency(new PluginE().getPluginId());
		}

	};

	private static class PluginE extends TestPlugin {
		@Override
		public void init(PluginContext pluginContext) {
			pluginContext.addPluginDependency(new PluginC().getPluginId());
		}

	};

	private static class PluginF extends TestPlugin {
		@Override
		public void init(PluginContext pluginContext) {
			pluginContext.defineResolver(new SimpleResolverId("resolver"), (c) -> {
			});
		}

	};

	private static class PluginG extends TestPlugin {
		@Override
		public void init(PluginContext pluginContext) {
			pluginContext.defineResolver(new SimpleResolverId("resolver"), (c) -> {
			});
		}

	};

	@Test
	@UnitTestMethod(target = Simulation.Builder.class, name = "build", args = {})
	public void testbuild() {

		// precondition : there can be no duplicate plugin ids
		ContractException contractException = assertThrows(ContractException.class, () -> {//

			PluginA pluginA = new PluginA();
			PluginB pluginB = new PluginB();

			Simulation	.builder()//
					.addPlugin(pluginA.getPluginId(), pluginA::init)//
					.addPlugin(pluginA.getPluginId(), pluginB::init)//
					.build(); //
		});

		
		// precondition : there can be no duplicate resolver ids
		contractException = assertThrows(ContractException.class, () -> {//

			PluginF pluginF = new PluginF();
			PluginG pluginG = new PluginG();

			Simulation	.builder()//
					.addPlugin(pluginF.getPluginId(), pluginF::init)//
					.addPlugin(pluginG.getPluginId(), pluginG::init)//
					.build(); //
		});

		assertEquals(NucleusError.DUPLICATE_RESOLVER_ID, contractException.getErrorType());

		// precondition : all of plugin's dependencies must be present
		contractException = assertThrows(ContractException.class, () -> {//
			PluginB pluginB = new PluginB();
			Simulation	.builder()//
					.addPlugin(pluginB.getPluginId(), pluginB::init)//
					.build(); //
		});

		assertEquals(NucleusError.MISSING_PLUGIN, contractException.getErrorType());

		// precondition : circular dependency
		contractException = assertThrows(ContractException.class, () -> {//
			PluginA pluginA = new PluginA();
			PluginB pluginB = new PluginB();
			PluginC pluginC = new PluginC();
			PluginD pluginD = new PluginD();
			PluginE pluginE = new PluginE();

			Simulation	.builder()//
					.addPlugin(pluginA.getPluginId(), pluginA::init)//
					.addPlugin(pluginB.getPluginId(), pluginB::init)//
					.addPlugin(pluginC.getPluginId(), pluginC::init)//
					.addPlugin(pluginD.getPluginId(), pluginD::init)//
					.addPlugin(pluginE.getPluginId(), pluginE::init)//
					.build(); //
		});

		assertEquals(NucleusError.CIRCULAR_PLUGIN_DEPENDENCIES, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = Simulation.Builder.class, name = "addPlugin", args = { PluginId.class })
	public void testAddPlugin() {
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		// have the added test agent produce some output
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// precondition : the plugin id cannot be null
		ContractException contractException = assertThrows(ContractException.class, () -> Simulation.builder().addPlugin(null, actionPluginInitializer::init));
		assertEquals(NucleusError.NULL_PLUGIN_ID, contractException.getErrorType());

		// precondition : the plugin context consumer cannot be null
		contractException = assertThrows(ContractException.class, () -> Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, null));
		assertEquals(NucleusError.NULL_PLUGIN_CONTEXT_CONSUMER, contractException.getErrorType());

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

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		/*
		 * Case 1 : there is a non-null output consumer added to the builder
		 */

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		Set<Object> expectedValues = new LinkedHashSet<>();
		expectedValues.add("A");
		expectedValues.add(4.5);
		expectedValues.add(424.75F);
		expectedValues.add(12);
		expectedValues.add(122423533423423453L);
		expectedValues.add(false);

		// have the added test agent produce some output
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {
			for (Object value : expectedValues) {
				context.releaseOutput(value);
			}
		}));

		// create two output consumers to show that the builder will only use
		// the last one
		LocalOutputConsumer localOutputConsumer1 = new LocalOutputConsumer();
		LocalOutputConsumer localOutputConsumer2 = new LocalOutputConsumer();

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();

		// run the simulation
		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).setOutputConsumer(localOutputConsumer1).setOutputConsumer(localOutputConsumer2).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the first local output consumer did not receive any values
		assertTrue(localOutputConsumer1.receivedItems.isEmpty());

		// show that the second local output consumer received the expected
		// values
		assertEquals(expectedValues, localOutputConsumer2.receivedItems);

		/*
		 * Case 2 : there is null output consumer added to the builder
		 */

		// show that the setting of null for the output consumer will yield no
		// output

		// ensure that the test agent will be created
		pluginBuilder.addAgent("Alpha");

		// have the added test agent produce some output
		pluginBuilder.addAgentActionPlan("Alpha", new AgentActionPlan(1, (context) -> {
			for (Object value : expectedValues) {
				context.releaseOutput(value);
			}
		}));

		actionPluginInitializer = pluginBuilder.build();

		localOutputConsumer1 = new LocalOutputConsumer();

		Simulation.builder().addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init).setOutputConsumer(localOutputConsumer1).setOutputConsumer(null).build().execute();

		// show that the action plans got executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the first local output consumer did not receive any values
		assertTrue(localOutputConsumer1.receivedItems.isEmpty());

	}

}
