package nucleus.testsupport.testplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.AgentId;
import nucleus.ReportId;
import nucleus.ResolverId;
import nucleus.SimpleReportId;
import nucleus.SimpleResolverId;
import util.MultiKey;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ActionDataView.class)
public class AT_ActionDataView {

	/**
	 * Shows that an action data view can be created from an action data
	 * container.
	 */
	@Test
	@UnitTestConstructor(args = { ActionDataContainer.class })
	public void testConstructor() {
		// precondition tests

		// if the action data container is null
		assertThrows(RuntimeException.class, () -> new ActionDataView(null));
	}
	/**
	 * Shows that agent action plans can be retrieved by agent aliases
	 */
	@Test
	@UnitTestMethod(name = "getAgentActionPlans", args = { Object.class })
	public void testGetAgentActionPlans() {
		ActionDataContainer actionDataContainer = new ActionDataContainer();
		ActionDataView actionDataView = new ActionDataView(actionDataContainer);

		// create several agent action plans and add them to the data container
		Object alias1 = "alias1";
		Object alias2 = "alias2";
		Object alias3 = "alias3";

		Map<Object, Set<AgentActionPlan>> expectedActionPlans = new LinkedHashMap<>();
		expectedActionPlans.put(alias1, new LinkedHashSet<>());
		expectedActionPlans.put(alias2, new LinkedHashSet<>());
		expectedActionPlans.put(alias3, new LinkedHashSet<>());

		// add a duplicated action plan
		AgentActionPlan duplicateAgentActionPlan = new AgentActionPlan(0, (c) -> {
		});
		expectedActionPlans.get(alias1).add(duplicateAgentActionPlan);
		expectedActionPlans.get(alias1).add(duplicateAgentActionPlan);
		expectedActionPlans.get(alias1).add(new AgentActionPlan(1, (c) -> {
		}));
		expectedActionPlans.get(alias1).add(new AgentActionPlan(2, (c) -> {
		}));

		expectedActionPlans.get(alias2).add(new AgentActionPlan(3, (c) -> {
		}));
		expectedActionPlans.get(alias2).add(new AgentActionPlan(4, (c) -> {
		}));
		expectedActionPlans.get(alias2).add(new AgentActionPlan(5, (c) -> {
		}));

		expectedActionPlans.get(alias3).add(new AgentActionPlan(1, (c) -> {
		}));
		expectedActionPlans.get(alias3).add(new AgentActionPlan(6, (c) -> {
		}));
		expectedActionPlans.get(alias3).add(new AgentActionPlan(9, (c) -> {
		}));

		for (Object alias : expectedActionPlans.keySet()) {
			for (AgentActionPlan agentActionPlan : expectedActionPlans.get(alias)) {
				actionDataContainer.addAgentActionPlan(alias, agentActionPlan);
			}
		}

		// retrieve the agent action plans and show that they are the same as
		// the ones we added
		for (Object alias : expectedActionPlans.keySet()) {
			Set<AgentActionPlan> actualPlans = actionDataView.getAgentActionPlans(alias);
			Set<AgentActionPlan> expectedPlans = expectedActionPlans.get(alias);
			assertEquals(expectedPlans, actualPlans);

			// the order of the plans should be the same as well
			assertEquals(new ArrayList<>(expectedPlans), new ArrayList<>(actualPlans));

		}

		// precondition tests

		// if the alias is null
		assertThrows(RuntimeException.class, () -> actionDataView.getAgentActionPlans(null));

	}
	/**
	 * Shows that an agent's alias can be retrieved
	 */
	@Test
	@UnitTestMethod(name = "getAgentAliasId", args = { AgentId.class })
	public void testGetAgentAliasId() {
		ActionDataContainer actionDataContainer = new ActionDataContainer();
		ActionDataView actionDataView = new ActionDataView(actionDataContainer);
		
		Set<MultiKey> expectedPairs = new LinkedHashSet<>();
		expectedPairs.add(new MultiKey("alias 1", new AgentId(0)));
		expectedPairs.add(new MultiKey("alias 2", new AgentId(1)));
		expectedPairs.add(new MultiKey("alias 3", new AgentId(2)));
		expectedPairs.add(new MultiKey("alias 4", new AgentId(3)));
		expectedPairs.add(new MultiKey("alias 5", new AgentId(4)));
		expectedPairs.add(new MultiKey("alias 6", new AgentId(5)));

		for (MultiKey multiKey : expectedPairs) {
			Object alias = multiKey.getKey(0);
			AgentId agentId = multiKey.getKey(1);
			actionDataContainer.assignAgentIdToAlias(alias, agentId);
		}

		for (MultiKey multiKey : expectedPairs) {
			Object expectedAlias = multiKey.getKey(0);
			AgentId agentId = multiKey.getKey(1);
			Optional<Object> optional = actionDataView.getAgentAliasId(agentId);
			assertTrue(optional.isPresent());
			Object actualAlias = optional.get();
			assertEquals(expectedAlias, actualAlias);
		}

		// precondition tests

		// if the alias is null
		assertThrows(RuntimeException.class, () -> actionDataView.getAgentAliasId(null));
	}
	
	/**
	 * Shows that report action plans can be retrieved by report ids
	 */
	@Test
	@UnitTestMethod(name = "getReportActionPlans", args = { ReportId.class })
	public void testGetReportActionPlans() {
		ActionDataContainer actionDataContainer = new ActionDataContainer();
		ActionDataView actionDataView = new ActionDataView(actionDataContainer);
		
		// create several report action plans and add them to the data container
		ReportId reportId1 = new SimpleReportId("report id 1");
		ReportId reportId2 = new SimpleReportId("report id 2");
		ReportId reportId3 = new SimpleReportId("report id 3");

		Map<ReportId, Set<ReportActionPlan>> expectedActionPlans = new LinkedHashMap<>();
		expectedActionPlans.put(reportId1, new LinkedHashSet<>());
		expectedActionPlans.put(reportId2, new LinkedHashSet<>());
		expectedActionPlans.put(reportId3, new LinkedHashSet<>());

		// add a duplicated action plan
		ReportActionPlan duplicateReportActionPlan = new ReportActionPlan(0, (c) -> {
		});
		expectedActionPlans.get(reportId1).add(duplicateReportActionPlan);
		expectedActionPlans.get(reportId1).add(duplicateReportActionPlan);
		expectedActionPlans.get(reportId1).add(new ReportActionPlan(1, (c) -> {
		}));
		expectedActionPlans.get(reportId1).add(new ReportActionPlan(2, (c) -> {
		}));

		expectedActionPlans.get(reportId2).add(new ReportActionPlan(3, (c) -> {
		}));
		expectedActionPlans.get(reportId2).add(new ReportActionPlan(4, (c) -> {
		}));
		expectedActionPlans.get(reportId2).add(new ReportActionPlan(5, (c) -> {
		}));

		expectedActionPlans.get(reportId3).add(new ReportActionPlan(1, (c) -> {
		}));
		expectedActionPlans.get(reportId3).add(new ReportActionPlan(6, (c) -> {
		}));
		expectedActionPlans.get(reportId3).add(new ReportActionPlan(9, (c) -> {
		}));

		for (ReportId reportId : expectedActionPlans.keySet()) {
			for (ReportActionPlan reportActionPlan : expectedActionPlans.get(reportId)) {
				actionDataContainer.addReportActionPlan(reportId, reportActionPlan);
			}
		}

		// retrieve the report action plans and show that they are the same as
		// the ones we added
		for (ReportId reportId : expectedActionPlans.keySet()) {
			Set<ReportActionPlan> actualPlans = actionDataView.getReportActionPlans(reportId);
			Set<ReportActionPlan> expectedPlans = expectedActionPlans.get(reportId);
			assertEquals(expectedPlans, actualPlans);

			// the order of the plans should be the same as well
			assertEquals(new ArrayList<>(expectedPlans), new ArrayList<>(actualPlans));

		}

		// precondition tests

		// if the report id is null
		assertThrows(RuntimeException.class, () -> actionDataView.getReportActionPlans(null));
	}
	
	/**
	 * Shows that resolver action plans can be retrieved by resolver ids
	 */
	@Test
	@UnitTestMethod(name = "getResolverActionPlans", args = { ResolverId.class })
	public void testGetResolverActionPlans() {
		ActionDataContainer actionDataContainer = new ActionDataContainer();
		ActionDataView actionDataView = new ActionDataView(actionDataContainer);
		// create several resolver action plans and add them to the data
		// container
		ResolverId resolverId1 = new SimpleResolverId("resolver id 1");
		ResolverId resolverId2 = new SimpleResolverId("resolver id 2");
		ResolverId resolverId3 = new SimpleResolverId("resolver id 3");

		Map<ResolverId, Set<ResolverActionPlan>> expectedActionPlans = new LinkedHashMap<>();
		expectedActionPlans.put(resolverId1, new LinkedHashSet<>());
		expectedActionPlans.put(resolverId2, new LinkedHashSet<>());
		expectedActionPlans.put(resolverId3, new LinkedHashSet<>());

		// add a duplicated action plan
		ResolverActionPlan duplicateResolverActionPlan = new ResolverActionPlan(0, (c) -> {
		});
		expectedActionPlans.get(resolverId1).add(duplicateResolverActionPlan);
		expectedActionPlans.get(resolverId1).add(duplicateResolverActionPlan);
		expectedActionPlans.get(resolverId1).add(new ResolverActionPlan(1, (c) -> {
		}));
		expectedActionPlans.get(resolverId1).add(new ResolverActionPlan(2, (c) -> {
		}));

		expectedActionPlans.get(resolverId2).add(new ResolverActionPlan(3, (c) -> {
		}));
		expectedActionPlans.get(resolverId2).add(new ResolverActionPlan(4, (c) -> {
		}));
		expectedActionPlans.get(resolverId2).add(new ResolverActionPlan(5, (c) -> {
		}));

		expectedActionPlans.get(resolverId3).add(new ResolverActionPlan(1, (c) -> {
		}));
		expectedActionPlans.get(resolverId3).add(new ResolverActionPlan(6, (c) -> {
		}));
		expectedActionPlans.get(resolverId3).add(new ResolverActionPlan(9, (c) -> {
		}));

		for (ResolverId resolverId : expectedActionPlans.keySet()) {
			for (ResolverActionPlan resolverActionPlan : expectedActionPlans.get(resolverId)) {
				actionDataContainer.addResolverActionPlan(resolverId, resolverActionPlan);
			}
		}

		// retrieve the resolver action plans and show that they are the same as
		// the ones we added
		for (ResolverId resolverId : expectedActionPlans.keySet()) {
			Set<ResolverActionPlan> actualPlans = actionDataView.getResolverActionPlans(resolverId);
			Set<ResolverActionPlan> expectedPlans = expectedActionPlans.get(resolverId);
			assertEquals(expectedPlans, actualPlans);

			// the order of the plans should be the same as well
			assertEquals(new ArrayList<>(expectedPlans), new ArrayList<>(actualPlans));

		}

		// precondition tests

		// if the resolver id is null
		assertThrows(RuntimeException.class, () -> actionDataView.getResolverActionPlans(null));

	}

}
