package plugins.components.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import nucleus.AgentId;
import nucleus.SimulationContext;
import nucleus.testsupport.MockSimulationContext;
import nucleus.testsupport.MockDataManagerContext;
import plugins.components.support.ComponentError;
import plugins.components.support.ComponentId;
import plugins.components.testsupport.SimpleComponentId;
import util.ContractException;
import util.Holder;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ComponentDataView.class)
public final class AT_ComponentDataView {

	@Test
	@UnitTestConstructor(args = { SimulationContext.class, ComponentDataManager.class })
	public void testConstructor() {
		MockDataManagerContext mockDataManagerContext = MockDataManagerContext.builder().build();		
		ComponentDataManager componentDataManager = new ComponentDataManager(mockDataManagerContext);
		ComponentDataView componentDataView = new ComponentDataView(MockSimulationContext.builder().build(),componentDataManager);
		assertNotNull(componentDataView);
	}
	
	
	@Test
	@UnitTestMethod(name = "getFocalComponentId", args = {})
	public void testGetFocalComponentId() {
		Holder<AgentId> currentAgentId = new Holder<>();

		MockDataManagerContext mockDataManagerContext = MockDataManagerContext//
																		.builder()//
																		.setGetCurrentAgentIdSupplier(() -> currentAgentId.get())//
																		.build();//

		/*
		 * Create a component manager and load it with agent/components
		 * associations
		 */
		ComponentDataManager componentDataManager = new ComponentDataManager(mockDataManagerContext);
		ComponentDataView componentDataView = new ComponentDataView(MockSimulationContext.builder().build(),componentDataManager);
		for (int i = 0; i < 10; i++) {
			componentDataManager.addComponentData(new AgentId(i), new SimpleComponentId(i));
		}

		// Set the current agent for the mock resolver context and show that the
		// focal component matches the current agent id
		for (int i = 0; i < 10; i++) {
			currentAgentId.set(new AgentId(i));
			assertEquals(new SimpleComponentId(i), componentDataView.getFocalComponentId());
		}
		// show that this returns null if there is no current agent or the
		// current agent was not mapped to a component id

		currentAgentId.set(new AgentId(11));
		assertNull(componentDataView.getFocalComponentId());

		currentAgentId.set(null);
		assertNull(componentDataView.getFocalComponentId());
	}

	@Test
	@UnitTestMethod(name = "getAgentId", args = { ComponentId.class })
	public void testGetAgentId() {
		MockDataManagerContext mockDataManagerContext = MockDataManagerContext.builder().build();

		// create a container for expected results
		Map<ComponentId, AgentId> expectedAssociations = new LinkedHashMap<>();

		/*
		 * Create a component manager
		 */
		ComponentDataManager componentDataManager = new ComponentDataManager(mockDataManagerContext);
		ComponentDataView componentDataView = new ComponentDataView(mockDataManagerContext,componentDataManager);
		/*
		 * Add several agent/component id associations
		 */

		for (int i = 0; i < 10; i++) {
			ComponentId componentId = new SimpleComponentId(i);
			AgentId agentId = new AgentId(i);
			expectedAssociations.put(componentId, agentId);
			componentDataManager.addComponentData(agentId, componentId);
		}

		// show that the agent associations are correct
		assertEquals(expectedAssociations.keySet(), componentDataManager.getComponentIds());
		for (ComponentId componentId : componentDataManager.getComponentIds()) {
			AgentId expectedAgentId = expectedAssociations.get(componentId);
			AgentId actualAgentId = componentDataView.getAgentId(componentId);
			assertEquals(expectedAgentId, actualAgentId);
		}

		// precondition tests

		// create a few argument values to support precondition tests
		ComponentId unknownComponentId = new SimpleComponentId(10);

		
		// if the component id is null
		ContractException contractException = assertThrows(ContractException.class, () -> componentDataView.getAgentId(null));
		assertEquals(ComponentError.NULL_AGENT_ID, contractException.getErrorType());

		// if the component id is null
		contractException = assertThrows(ContractException.class, () -> componentDataView.getAgentId(unknownComponentId));
		assertEquals(ComponentError.UNKNOWN_COMPONENT_ID, contractException.getErrorType());
	}
	
	@Test
	@UnitTestMethod(name = "containsComponentId", args = { ComponentId.class })
	public void testContainsComponentId() {
		MockDataManagerContext mockDataManagerContext = MockDataManagerContext.builder().build();

		// create a container for expected results
		Map<ComponentId, AgentId> expectedAssociations = new LinkedHashMap<>();

		/*
		 * Create a component manager
		 */
		ComponentDataManager componentDataManager = new ComponentDataManager(mockDataManagerContext);
		ComponentDataView componentDataView = new ComponentDataView(mockDataManagerContext, componentDataManager);
		/*
		 * Add several agent/component id associations
		 */

		for (int i = 0; i < 10; i++) {
			ComponentId componentId = new SimpleComponentId(i);
			AgentId agentId = new AgentId(i);
			expectedAssociations.put(componentId, agentId);
			componentDataManager.addComponentData(agentId, componentId);
		}

		// show that the component ids that should be contained are contained
		assertEquals(expectedAssociations.keySet(), componentDataManager.getComponentIds());
		for (ComponentId componentId : componentDataManager.getComponentIds()) {
			assertTrue(componentDataView.containsComponentId(componentId));
		}

		// show that component ids that should not be contain are not contained
		for (int i = 10; i < 20; i++) {
			ComponentId componentId = new SimpleComponentId(i);
			assertFalse(componentDataView.containsComponentId(componentId));
		}

		// no precondition tests

	}

}
