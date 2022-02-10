package plugins.components.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.AgentId;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.Event;
import nucleus.DataManagerContext;
import nucleus.ResolverId;
import nucleus.SimpleResolverId;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import nucleus.testsupport.actionplugin.DataManagerActionPlan;
import plugins.components.ComponentPlugin;
import plugins.components.datacontainers.ComponentDataView;
import plugins.components.events.ComponentConstructionEvent;
import plugins.components.support.ComponentError;
import plugins.components.support.ComponentId;
import plugins.components.testsupport.SimpleComponentId;
import util.ContractException;
import util.Holder;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = ComponentResolver.class)
public final class AT_ComponentResolver {
	/**
	 * Shows that the component data view is published with the correct initial
	 * state. Other tests will demonstrate that the data view is maintained.
	 */
	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testComponentDataViewInitialization() {

		Builder builder = Simulation.builder();

		// add the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		/*
		 * Create an agent to show that the component data view exists and is
		 * initialized
		 */

		pluginBuilder.addAgent("agent");

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			// show that the compartment location data view exists
			Optional<ComponentDataView> optional = c.getDataView(ComponentDataView.class);
			assertTrue(optional.isPresent());

			ComponentDataView componentDataView = optional.get();

			// show that there is no focal component
			assertNull(componentDataView.getFocalComponentId());

		}));

		// build action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		builder.addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	/*
	 * Duplicates the ComponentConstructionEvent.
	 * 
	 * We must test the ComponentConstructionEvent via a resolver since agents
	 * are not allowed to resolve this type of event. Since resolvers do not
	 * have immediate event resolution, we have an agent resolve this custom
	 * event that will in turn be converted into a ComponentConstructionEvent by
	 * a custom resolver. This will force the contract exceptions to be
	 * generated in the assertion statements of the agent.
	 *
	 */
	private static class CustomEvent implements Event {

		private final ComponentId componentId;

		private final Consumer<AgentContext> consumer;

		public CustomEvent(ComponentId componentId, Consumer<AgentContext> consumer) {
			this.componentId = componentId;
			this.consumer = consumer;
		}

		public ComponentId getComponentId() {
			return componentId;
		}

		public Consumer<AgentContext> getConsumer() {
			return consumer;
		}

	}

	/**
	 * Shows ComponentConstructionEvent events are handled properly. The agent
	 * is created with the consumer as its initial behavior and the agent is
	 * associated with the component id.
	 */
	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testComponentConstructionEvent() {

		Builder builder = Simulation.builder();

		// add the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		/*
		 * Create a resolver to generate and test a ComponentConstructionEvent
		 */
		ResolverId resolverId = new SimpleResolverId("resolver");
		pluginBuilder.addResolver(resolverId);

		// add a container to indicate the action of the agent's initial
		// behavior
		Holder<AgentId> expectedAgentId = new Holder<>();
		assertNull(expectedAgentId.get());

		// create the contents of the event
		Consumer<AgentContext> consumer = (c2) -> {
			expectedAgentId.set(c2.getCurrentAgentId());
		};

		ComponentId expectedComponentId = new SimpleComponentId("expected id");

		/*
		 * Have the resolver resolve a ComponentConstructionEvent.
		 * 
		 */
		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(0, (c) -> {
			c.resolveEvent(new ComponentConstructionEvent(expectedComponentId, consumer));

		}));

		// Have the resolver show that the agent was 1)created, 2) is properly
		// associated and 3) executed the proper initial behavior

		pluginBuilder.addResolverActionPlan(resolverId, new DataManagerActionPlan(1, (c) -> {

			// show that the agent's initial behavior was executed
			AgentId agentId = expectedAgentId.get();
			assertNotNull(agentId);

			// show the agent was created
			assertTrue(c.agentExists(agentId));

			// show that the agentid is associated with the expectedComponentId
			ComponentDataView componentDataView = c.getDataView(ComponentDataView.class).get();
			AgentId actualAgentId = componentDataView.getAgentId(expectedComponentId);
			assertNotNull(actualAgentId);
			assertEquals(agentId, actualAgentId);

		}));

		// precondition tests

		// add an agent for precondition tests
		pluginBuilder.addAgent("precondition agent");

		// have the precondition agent show that it cannot add a component
		pluginBuilder.addAgentActionPlan("precondition agent", new AgentActionPlan(2, (c) -> {
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new ComponentConstructionEvent(new SimpleComponentId("failing component"), (c2) -> {
			})));
			assertEquals(ComponentError.RESOLVER_EXCLUSIVE_EVENT, contractException.getErrorType());

		}));

		/*
		 * Add a custom resolver that handles the custom event by resolving the
		 * corresponding ComponentConstructionEvent. Resolvers do not have
		 * immediate event resolution, so it is difficult to show that the
		 * correct exceptions are thrown. We leave that to the agent that
		 * generates the custom events.
		 */
		ResolverId customResolverId = new SimpleResolverId("custom resolver");
		pluginBuilder.addResolver(customResolverId);
		pluginBuilder.addResolverActionPlan(customResolverId, new DataManagerActionPlan(0, (c2) -> {
			c2.subscribeToEventExecutionPhase(CustomEvent.class, (c3, customEvent) -> {
				ComponentConstructionEvent componentConstructionEvent = new ComponentConstructionEvent(customEvent.getComponentId(), customEvent.getConsumer());
				c3.resolveEvent(componentConstructionEvent);
			});
		}));

		// The remaining precondition tests are executed by the precondition
		// agent using the custom event as a proxy for the
		// ComponentConstructionEvent
		pluginBuilder.addAgentActionPlan("precondition agent", new AgentActionPlan(3, (c) -> {
			ComponentId componentId = new SimpleComponentId("failing component");
			Consumer<AgentContext> actionConsumer = (c2) -> {
			};

			// if the event's agent context consumer is null
			CustomEvent event1 = new CustomEvent(componentId, null);
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(event1));
			assertEquals(ComponentError.NULL_AGENT_INITIAL_BEHAVIOR_CONSUMER, contractException.getErrorType());

			// if the event's component id is null
			CustomEvent event2 = new CustomEvent(null, actionConsumer);
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(event2));
			assertEquals(ComponentError.NULL_AGENT_ID, contractException.getErrorType());

			// if the component id was previously assigned to an agent
			ComponentId existingComponentId = new SimpleComponentId("existing component");
			CustomEvent event3 = new CustomEvent(existingComponentId, actionConsumer);
			c.resolveEvent(event3);

			CustomEvent event4 = new CustomEvent(existingComponentId, actionConsumer);
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(event4));
			assertEquals(ComponentError.COMPONENT_ID_ALREADY_EXISTS, contractException.getErrorType());
		}));

		// build action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		builder.addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

	}
}
