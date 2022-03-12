package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nucleus.DataManager;
import nucleus.DataManagerContext;

/**
 * A data manager used by test actors and test data managers to retrieve plans
 * by alias ids.
 * 
 * @author Shawn Hatch
 *
 */

public class TestPlanDataManager extends DataManager {

	private final Map<Object, List<TestActorPlan>> actorActionPlanMap = new LinkedHashMap<>();

	private final Map<Object, List<TestDataManagerPlan>> dataManagerActionPlanMap = new LinkedHashMap<>();

	/**
	 * Constructs this data manager from the given test plugin data
	 */
	public TestPlanDataManager(TestPluginData testPluginData) {

		for (Object alias : testPluginData.getTestActorAliases()) {
			List<TestActorPlan> testActorPlans = testPluginData.getTestActorPlans(alias);
			actorActionPlanMap.put(alias, testActorPlans);
		}

		for (Object alias : testPluginData.getTestDataManagerAliases()) {
			List<TestDataManagerPlan> testDataManagerPlans = testPluginData.getTestDataManagerPlans(alias);
			dataManagerActionPlanMap.put(alias, testDataManagerPlans);
		}
	}

	/**
	 * Initializes this data manager by subscribing to simulation close. On
	 * close it releases a single TestScenarioReport that indicates success if
	 * there was at least one plan and all plans were executed.
	 */
	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		dataManagerContext.subscribeToSimulationClose(this::sendActionCompletionReport);
	}

	/**
	 * Returns all plans associated with the given actor alias
	 */
	public List<TestActorPlan> getTestActorPlans(Object alias) {
		List<TestActorPlan> result = new ArrayList<>();
		List<TestActorPlan> list = actorActionPlanMap.get(alias);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

	/**
	 * Returns all plans associated with the given data manager alias
	 */
	public List<TestDataManagerPlan> getTestDataManagerPlans(Object alias) {
		List<TestDataManagerPlan> result = new ArrayList<>();

		List<TestDataManagerPlan> list = dataManagerActionPlanMap.get(alias);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

	/**
	 * Return true if and only if all actions that are stored in this action
	 * data view have been executed. Indicates that all injected behaviors in a
	 * unit test were actually executed. RETURNS FALSE IF THERE WERE NO ACTIONS
	 * STORED.
	 */

	private void sendActionCompletionReport(DataManagerContext context) {
		context.releaseOutput(new TestScenarioReport(allActionsExecuted()));
	}

	private boolean allActionsExecuted() {
		int planCount = 0;
		for (Object alias : actorActionPlanMap.keySet()) {
			for (TestActorPlan testActorPlan : actorActionPlanMap.get(alias)) {
				planCount++;
				if (!testActorPlan.executed()) {
					return false;
				}
			}
		}

		for (Object alias : dataManagerActionPlanMap.keySet()) {
			for (TestDataManagerPlan testDataManagerPlan : dataManagerActionPlanMap.get(alias)) {
				planCount++;
				if (!testDataManagerPlan.executed()) {
					return false;
				}
			}
		}

		return planCount > 0;

	}

}
