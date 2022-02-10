package plugins.globals.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.AgentId;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.DataManagerContext;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.components.datacontainers.ComponentDataView;
import plugins.globals.GlobalPlugin;
import plugins.globals.datacontainers.GlobalDataView;
import plugins.globals.events.mutation.GlobalComponentConstructionEvent;
import plugins.globals.events.mutation.GlobalPropertyValueAssignmentEvent;
import plugins.globals.events.observation.GlobalPropertyChangeObservationEvent;
import plugins.globals.initialdata.GlobalInitialData;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalError;
import plugins.globals.support.GlobalPropertyId;
import plugins.globals.support.SimpleGlobalComponentId;
import plugins.globals.support.SimpleGlobalPropertyId;
import plugins.globals.testsupport.TestGlobalPropertyId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import util.ContractException;
import util.MultiKey;
import util.MutableBoolean;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GlobalPropertyResolver.class)
public final class AT_GlobalPropertyResolver {

	@Test
	@UnitTestConstructor(args = { GlobalInitialData.class })
	public void testConstructor() {

		assertNotNull(new GlobalPropertyResolver(GlobalInitialData.builder().build()));

		ContractException contractException = assertThrows(ContractException.class, () -> new GlobalPropertyResolver(null));
		assertEquals(GlobalError.NULL_GLOBAL_INITIAL_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testGlobalDataViewInitialization() {

		Builder builder = Simulation.builder();

		// add the global property definitions
		GlobalInitialData.Builder globalInitialDataBuilder = GlobalInitialData.builder();

		GlobalPropertyId globalPropertyId_1 = new SimpleGlobalPropertyId("id_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3).build();
		globalInitialDataBuilder.defineGlobalProperty(globalPropertyId_1, propertyDefinition);

		GlobalPropertyId globalPropertyId_2 = new SimpleGlobalPropertyId("id_2");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.78).build();
		globalInitialDataBuilder.defineGlobalProperty(globalPropertyId_2, propertyDefinition);

		GlobalPropertyId globalPropertyId_3 = new SimpleGlobalPropertyId("id_3");
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
		globalInitialDataBuilder.defineGlobalProperty(globalPropertyId_3, propertyDefinition);

		// set some of the properties to new values
		globalInitialDataBuilder.setGlobalPropertyValue(globalPropertyId_1, 17);
		globalInitialDataBuilder.setGlobalPropertyValue(globalPropertyId_2, 9.756);

		/*
		 * Add two global components. These components will use mutable booleans
		 * to indicate that they were successfully created.
		 */
		MutableBoolean globalComponent_1_Exists = new MutableBoolean();
		assertFalse(globalComponent_1_Exists.getValue());

		GlobalComponentId globalComponentId_1 = new SimpleGlobalComponentId("component_1");
		globalInitialDataBuilder.setGlobalComponentInitialBehaviorSupplier(globalComponentId_1, () -> (c2) -> globalComponent_1_Exists.setValue(true));

		MutableBoolean globalComponent_2_Exists = new MutableBoolean();
		assertFalse(globalComponent_2_Exists.getValue());

		GlobalComponentId globalComponentId_2 = new SimpleGlobalComponentId("component_2");
		globalInitialDataBuilder.setGlobalComponentInitialBehaviorSupplier(globalComponentId_2, () -> (c2) -> globalComponent_2_Exists.setValue(true));

		GlobalInitialData globalInitialData = globalInitialDataBuilder.build();
		builder.addPlugin(GlobalPlugin.PLUGIN_ID, new GlobalPlugin(globalInitialData)::init);

		// add the remaining plugins
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// create an agent and have it assign various global properties at
		// various times
		pluginBuilder.addAgent("agent");

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0.0, (c) -> {
			// show that the data view exists
			Optional<GlobalDataView> optional = c.getDataView(GlobalDataView.class);

			assertTrue(optional.isPresent());

			GlobalDataView globalDataView = optional.get();

			// show that the global property ids are present
			Set<GlobalPropertyId> expectedGlobalPropertyIds = globalInitialData.getGlobalPropertyIds();
			Set<GlobalPropertyId> actualGlobalPropertyIds = globalDataView.getGlobalPropertyIds();
			assertEquals(expectedGlobalPropertyIds, actualGlobalPropertyIds);

			// show that all the property definitions are present
			for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
				PropertyDefinition expectedGlobalPropertyDefinition = globalInitialData.getGlobalPropertyDefinition(globalPropertyId);
				PropertyDefinition actualGlobalPropertyDefinition = globalDataView.getGlobalPropertyDefinition(globalPropertyId);
				assertEquals(expectedGlobalPropertyDefinition, actualGlobalPropertyDefinition);
			}

			// show that the component ids are present
			Set<GlobalComponentId> expectedGlobalComponentIds = globalInitialData.getGlobalComponentIds();
			Set<GlobalComponentId> actualGlobalComponentIds = globalDataView.getGlobalComponentIds();
			assertEquals(expectedGlobalComponentIds, actualGlobalComponentIds);

			// show that the properties have the expected values and the
			// expected times
			for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
				Object expectedPropertyValue = globalInitialData.getGlobalPropertyValue(globalPropertyId);
				if (expectedPropertyValue == null) {
					expectedPropertyValue = globalInitialData.getGlobalPropertyDefinition(globalPropertyId).getDefaultValue().get();
				}
				Object actualPropertyValue = globalDataView.getGlobalPropertyValue(globalPropertyId);
				assertEquals(expectedPropertyValue, actualPropertyValue);
				assertEquals(0.0, globalDataView.getGlobalPropertyTime(globalPropertyId));
			}

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		builder.addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init);

		// build and execute the engine
		TestReportItemOutputConsumer actualOutputConsumer = new TestReportItemOutputConsumer();
		builder.setOutputConsumer(actualOutputConsumer);
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the two global components were created
		assertTrue(globalComponent_1_Exists.getValue());
		assertTrue(globalComponent_2_Exists.getValue());

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testGlobalPropertyChangeObservationEventLabelers() {
		Builder builder = Simulation.builder();

		// add the required plugins
		builder.addPlugin(GlobalPlugin.PLUGIN_ID, new GlobalPlugin(GlobalInitialData.builder().build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// create an agent to execute the test
		pluginBuilder.addAgent("agent");

		// Have the agent attempt to add the event labeler and show that a
		// contract exception is thrown, indicating that the labeler was
		// previously added by the resolver.
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			EventLabeler<GlobalPropertyChangeObservationEvent> eventLabeler = GlobalPropertyChangeObservationEvent.getEventLabeler();
			assertNotNull(eventLabeler);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabeler));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		}));

		// build action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		builder.addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testGlobalComponentConstructionEvent() {
		Builder builder = Simulation.builder();

		// add the required plugins
		builder.addPlugin(GlobalPlugin.PLUGIN_ID, new GlobalPlugin(GlobalInitialData.builder().build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// create an agent to execute the test
		pluginBuilder.addAgent("agent");

		MutableBoolean globalComponentExecutedInit = new MutableBoolean();
		GlobalComponentId globalComponentId = new SimpleGlobalComponentId("id");

		// Have the agent create the global component and then detect that agent
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			// the new global component will set the globalComponentExecutedInit
			// to true if it actually performs its initialization
			GlobalComponentConstructionEvent globalComponentConstructionEvent = //
					new GlobalComponentConstructionEvent(globalComponentId, //
							(c2) -> globalComponentExecutedInit.setValue(true));//

			c.resolveEvent(globalComponentConstructionEvent);

			ComponentDataView componentDataView = c.getDataView(ComponentDataView.class).get();
			AgentId agentId = componentDataView.getAgentId(globalComponentId);
			assertNotNull(agentId);
			assertTrue(c.agentExists(agentId));

			GlobalDataView globalDataView = c.getDataView(GlobalDataView.class).get();
			globalDataView.getGlobalComponentIds().contains(globalComponentId);

		}));

		// have the agent demonstrate the preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GlobalComponentConstructionEvent(null, (c2) -> {
			})));
			assertEquals(GlobalError.NULL_GLOBAL_COMPONENT_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GlobalComponentConstructionEvent(new SimpleGlobalComponentId("xxx"), null)));
			assertEquals(NucleusError.NULL_AGENT_CONTEXT_CONSUMER, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GlobalComponentConstructionEvent(globalComponentId, (c2) -> {
			})));
			assertEquals(GlobalError.DUPLICATE_GLOBAL_COMPONENT_ID, contractException.getErrorType());

		}));

		// build action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		builder.addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the added global component executed its initialization
		assertTrue(globalComponentExecutedInit.getValue());

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testGlobalPropertyValueAssignmentEvent() {
		Builder builder = Simulation.builder();

		// add the required plugins
		GlobalInitialData.Builder globalInitialDataBuilder = GlobalInitialData.builder();

		// Add a mutable global property
		GlobalPropertyId globalPropertyId_1 = new SimpleGlobalPropertyId("mutable property");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(23).build();
		globalInitialDataBuilder.defineGlobalProperty(globalPropertyId_1, propertyDefinition);

		// Add an immutable global property
		GlobalPropertyId globalPropertyId_2 = new SimpleGlobalPropertyId("immutable property");
		propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(100).setPropertyValueMutability(false).build();
		globalInitialDataBuilder.defineGlobalProperty(globalPropertyId_2, propertyDefinition);

		GlobalInitialData globalInitialData = globalInitialDataBuilder.build();

		builder.addPlugin(GlobalPlugin.PLUGIN_ID, new GlobalPlugin(globalInitialData)::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		// create an agent to execute the test
		pluginBuilder.addAgent("agent");

		// create an agent to observe
		pluginBuilder.addAgent("observer");

		// create some containers to hold the expected and actual observations
		List<MultiKey> expectedObservations = new ArrayList<>();
		List<MultiKey> actualObservations = new ArrayList<>();

		pluginBuilder.addAgentActionPlan("observer", new AgentActionPlan(0, (c) -> {
			EventLabel<GlobalPropertyChangeObservationEvent> eventLabel = GlobalPropertyChangeObservationEvent.getEventLabel(c, globalPropertyId_1);
			c.subscribe(eventLabel, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c2.getTime(), e.getGlobalPropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue());
				actualObservations.add(multiKey);
			});
		}));

		// Have the agent set the value of the global property 1 a few times
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			GlobalDataView globalDataView = c.getDataView(GlobalDataView.class).get();
			assertEquals(0.0, globalDataView.getGlobalPropertyTime(globalPropertyId_1));

			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_1, 55));
			assertEquals(new Integer(55), globalDataView.getGlobalPropertyValue(globalPropertyId_1));
			assertEquals(1.0, globalDataView.getGlobalPropertyTime(globalPropertyId_1));
			expectedObservations.add(new MultiKey(1.0, globalPropertyId_1, 23, 55));
			
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			GlobalDataView globalDataView = c.getDataView(GlobalDataView.class).get();
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_1, 200));
			assertEquals(new Integer(200), globalDataView.getGlobalPropertyValue(globalPropertyId_1));
			assertEquals(2.0, globalDataView.getGlobalPropertyTime(globalPropertyId_1));
			expectedObservations.add(new MultiKey(2.0, globalPropertyId_1, 55, 200));

		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			GlobalDataView globalDataView = c.getDataView(GlobalDataView.class).get();
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_1, 300));
			assertEquals(new Integer(300), globalDataView.getGlobalPropertyValue(globalPropertyId_1));
			assertEquals(3.0, globalDataView.getGlobalPropertyTime(globalPropertyId_1));
			expectedObservations.add(new MultiKey(3.0, globalPropertyId_1, 200, 300));
		}));

		// have the agent demonstrate the preconditions
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {

			// if the global property id is null
			ContractException contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GlobalPropertyValueAssignmentEvent(null, 15)));
			assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_ID, contractException.getErrorType());
			
			//if the global property id
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GlobalPropertyValueAssignmentEvent(TestGlobalPropertyId.getUnknownGlobalPropertyId(), 15)));
			assertEquals(GlobalError.UNKNOWN_GLOBAL_PROPERTY_ID, contractException.getErrorType());

			//if the property value is null
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_1, null)));
			assertEquals(GlobalError.NULL_GLOBAL_PROPERTY_VALUE, contractException.getErrorType());

			//if the global property definition indicates the property is not mutable
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_2, 55)));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());
			
			//if the property value is incompatible with the property definition
			contractException = assertThrows(ContractException.class, () -> c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_1, "bad value")));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		}));

		// build action plugin
		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		builder.addPlugin(ActionPluginInitializer.PLUGIN_ID, actionPluginInitializer::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPluginInitializer.allActionsExecuted());

		// show that the observations were correct		
		assertTrue(expectedObservations.size()>0);
		assertEquals(expectedObservations.size(), actualObservations.size());
		assertEquals(new LinkedHashSet<>(expectedObservations), new LinkedHashSet<>(actualObservations));

	}

}
