package nucleus.testsupport.testplugin;

import java.util.List;

import nucleus.DataManager;
import nucleus.DataManagerContext;

/**
 * Test Support data manager implementation designed to execute test-defined
 * behaviors from within the data manager. The data manager schedules the
 * TestDataManagerPlans that were stored in the TestPluginData that were
 * associated with its alias.
 * 
 * Alias identification exists for the convenience of the test implementor so
 * that tests can name data managers.
 * 
 *
 */

public class TestDataManager extends DataManager {
	private Object alias;

	/*
	 * Package level access for setting the alias of the data manager. This is
	 * done immediately after construction, before any integration into the
	 * experiment or simulation.
	 */
	void setAlias(Object alias) {
		this.alias = alias;
	}

	/**
	 * Associates its ActorId. Schedules the ActorActionPlans that were stored
	 * in the ActionDataView that were associated with its alias.
	 */
	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		TestPlanDataManager testPlanDataManager = dataManagerContext.getDataManager(TestPlanDataManager.class);

		List<TestDataManagerPlan> testDataManagerPlans = testPlanDataManager.getTestDataManagerPlans(alias);
		for (final TestDataManagerPlan testDataManagerPlan : testDataManagerPlans) {
			dataManagerContext.addPlan(testDataManagerPlan::executeAction, testDataManagerPlan.getScheduledTime());
		}
	}
}
