package nucleus.testsupport.actionplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Simulation;
import nucleus.ReportContext;
import nucleus.ReportId;
import nucleus.SimpleReportId;
import util.MultiKey;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ActionReport.class)
public class AT_ActionReport {
	/**
	 * Show construction works
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		// nothing to test in the constructor
	}

	/**
	 * 
	 * Show that an action report executes its ReportActionPlans via its report
	 * id as expected.
	 */
	@Test
	@UnitTestMethod(name = "init", args = { ReportContext.class })
	public void testInit() {
		// create two reports
		ReportId reportId1 = new SimpleReportId("report id 1");
		ReportId reportId2 = new SimpleReportId("report id 2");

		// create containers for expected and actual observations
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		expectedObservations.add(new MultiKey(reportId1, 3.0));
		expectedObservations.add(new MultiKey(reportId2, 3.0));
		expectedObservations.add(new MultiKey(reportId1, 4.212));
		expectedObservations.add(new MultiKey(reportId1, 5.123));
		expectedObservations.add(new MultiKey(reportId2, 43.0));
		expectedObservations.add(new MultiKey(reportId1, 12.123));
		expectedObservations.add(new MultiKey(reportId1, 8.534));
		expectedObservations.add(new MultiKey(reportId2, 1.423));

		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// add the reports to the action plugin
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addReport(reportId1);
		pluginBuilder.addReport(reportId2);
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(100, (c) -> {}));

		/*
		 * Create ReportActionPlans from the expected observations. Each action
		 * plan will record a Multikey into the actual observations.
		 */
		for (MultiKey multiKey : expectedObservations) {
			ReportId expectedReportId = multiKey.getKey(0);
			Double expectedTime = multiKey.getKey(1);
			pluginBuilder.addReportActionPlan(expectedReportId, new ReportActionPlan(expectedTime, (c) -> {
				ReportId actaulReportId = c.getCurrentReportId();
				Double actualTime = c.getTime();
				actualObservations.add(new MultiKey(actaulReportId, actualTime));
			}));
		}

		// build the action plugin
		ActionPlugin actionPlugin = pluginBuilder.build();

		// build and execute the engine
		Simulation	.builder()//
				.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init)//
				.build()//
				.execute();//

		// show that all actions executed
		assertTrue(actionPlugin.allActionsExecuted());

		// show that the agents executed the expected actions
		assertEquals(expectedObservations, actualObservations);

	}
}
