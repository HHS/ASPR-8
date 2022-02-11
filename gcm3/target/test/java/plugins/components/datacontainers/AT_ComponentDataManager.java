package plugins.components.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.AgentId;
import nucleus.DataManagerContext;
import nucleus.testsupport.MockDataManagerContext;
import plugins.components.support.ComponentId;
import plugins.components.testsupport.SimpleComponentId;
import plugins.components.testsupport.TestComponentId;
import util.Holder;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ComponentDataManager.class)
public final class AT_ComponentDataManager {

	

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
		for (int i = 0; i < 10; i++) {
			componentDataManager.addComponentData(new AgentId(i), new SimpleComponentId(i));
		}

		// Set the current agent for the mock resolver context and show that the
		// focal component matches the current agent id
		for (int i = 0; i < 10; i++) {
			currentAgentId.set(new AgentId(i));
			assertEquals(new SimpleComponentId(i), componentDataManager.getFocalComponentId());
		}
		// show that this returns null if there is no current agent or the
		// current agent was not mapped to a component id

		currentAgentId.set(new AgentId(11));
		assertNull(componentDataManager.getFocalComponentId());

		currentAgentId.set(null);
		assertNull(componentDataManager.getFocalComponentId());

	}

	@Test
	@UnitTestMethod(name = "getComponentIds", args = {})
	public void testGetComponentIds() {

		MockDataManagerContext mockDataManagerContext = MockDataManagerContext.builder().build();

		/*
		 * Create a component manager
		 */
		Set<ComponentId> expectedComponentIds = new LinkedHashSet<>();
		ComponentDataManager componentDataManager = new ComponentDataManager(mockDataManagerContext);

		// show that the component manager initially has no component ids
		assertTrue(componentDataManager.getComponentIds().isEmpty());

		/*
		 * Add several agent/component id associations
		 */
		for (int i = 0; i < 10; i++) {
			SimpleComponentId componentId = new SimpleComponentId(i);
			expectedComponentIds.add(componentId);
			componentDataManager.addComponentData(new AgentId(i), componentId);
		}

		// show that the component ids are the ones added
		Set<ComponentId> actualComponentIds = componentDataManager.getComponentIds();
		assertEquals(expectedComponentIds, actualComponentIds);

	}

	@Test
	@UnitTestMethod(name = "getComponentIds", args = { Class.class })
	public void testGetComponentIds_ByComponentClass() {
		MockDataManagerContext mockDataManagerContext = MockDataManagerContext.builder().build();

		// create some containers for expected results
		Set<ComponentId> expectedSimpleComponentIds = new LinkedHashSet<>();
		Set<ComponentId> expectedTestComponentIds = new LinkedHashSet<>();
		Set<ComponentId> expectedComponentIds = new LinkedHashSet<>();

		/*
		 * Create a component manager
		 */
		ComponentDataManager componentDataManager = new ComponentDataManager(mockDataManagerContext);

		// show that the component manager initially has not component ids
		assertTrue(componentDataManager.getComponentIds().isEmpty());

		/*
		 * Add several agent/component id associations
		 */
		int index = 0;
		for (int i = 0; i < 10; i++) {
			SimpleComponentId componentId = new SimpleComponentId(index);
			expectedSimpleComponentIds.add(componentId);
			componentDataManager.addComponentData(new AgentId(index), componentId);
			index++;
		}
		for (TestComponentId testComponentId : TestComponentId.values()) {
			expectedTestComponentIds.add(testComponentId);
			componentDataManager.addComponentData(new AgentId(index), testComponentId);
			index++;
		}

		// show that the component ids are the ones added for SimpleComponentId
		Set<SimpleComponentId> actualSimpleComponentIds = componentDataManager.getComponentIds(SimpleComponentId.class);
		assertEquals(expectedSimpleComponentIds, actualSimpleComponentIds);

		// show that the component ids are the ones added for TestComponentId
		Set<TestComponentId> actualTestComponentIds = componentDataManager.getComponentIds(TestComponentId.class);
		assertEquals(expectedTestComponentIds, actualTestComponentIds);

		// show that the component ids are the ones added for ComponentId
		expectedComponentIds.addAll(expectedSimpleComponentIds);
		expectedComponentIds.addAll(expectedTestComponentIds);
		Set<ComponentId> actualComponentIds = componentDataManager.getComponentIds(ComponentId.class);
		assertEquals(expectedComponentIds, actualComponentIds);

		// precondition tests
		assertThrows(RuntimeException.class, () -> componentDataManager.getComponentIds(null));

	}

	@Test
	@UnitTestMethod(name = "addComponentData", args = { AgentId.class, ComponentId.class })
	public void testAddComponentData() {
		MockDataManagerContext mockDataManagerContext = MockDataManagerContext.builder().build();

		// create a container for expected results
		Map<ComponentId, AgentId> expectedAssociations = new LinkedHashMap<>();

		/*
		 * Create a component manager
		 */
		ComponentDataManager componentDataManager = new ComponentDataManager(mockDataManagerContext);
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
			AgentId actualAgentId = componentDataManager.getAgentId(componentId);
			assertEquals(expectedAgentId, actualAgentId);
		}

		// precondition tests

		// create a few argument values to support precondition tests
		AgentId previouslyAddedAgentId = new AgentId(9);
		ComponentId previouslyAddedComponentId = new SimpleComponentId(9);
		ComponentId componentId = new SimpleComponentId(10);
		AgentId agentId = new AgentId(10);

		// if the agent id is null
		assertThrows(RuntimeException.class, () -> componentDataManager.addComponentData(null, componentId));

		// if the component id is null
		assertThrows(RuntimeException.class, () -> componentDataManager.addComponentData(agentId, null));

		// if the agent id was previously associated with a component id
		assertThrows(RuntimeException.class, () -> componentDataManager.addComponentData(previouslyAddedAgentId, componentId));

		// if the component id was previously associated with an agent id
		assertThrows(RuntimeException.class, () -> componentDataManager.addComponentData(agentId, previouslyAddedComponentId));
	}

	@Test
	@UnitTestConstructor(args = { DataManagerContext.class })
	public void testConstructor() {
		MockDataManagerContext mockDataManagerContext = MockDataManagerContext.builder().build();
		ComponentDataManager componentDataManager = new ComponentDataManager(mockDataManagerContext);
		assertNotNull(componentDataManager);
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
			AgentId actualAgentId = componentDataManager.getAgentId(componentId);
			assertEquals(expectedAgentId, actualAgentId);
		}

		// precondition tests

		// create a few argument values to support precondition tests
		ComponentId unknownComponentId = new SimpleComponentId(10);

		// if the component id is null
		assertThrows(RuntimeException.class, () -> componentDataManager.getAgentId(null));

		// if the component id is null
		assertThrows(RuntimeException.class, () -> componentDataManager.getAgentId(unknownComponentId));

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
			assertTrue(componentDataManager.containsComponentId(componentId));
		}

		// show that component ids that should not be contain are not contained
		for (int i = 10; i < 20; i++) {
			ComponentId componentId = new SimpleComponentId(i);
			assertFalse(componentDataManager.containsComponentId(componentId));
		}

		// no precondition tests

	}
}
