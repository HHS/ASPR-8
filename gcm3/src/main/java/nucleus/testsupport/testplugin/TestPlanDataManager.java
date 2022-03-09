package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nucleus.ActorId;
import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.DataManagerId;

/**
 * A data manager used by test actors and test data managers to retrieve plans by alias ids.
 * 
 * @author Shawn Hatch
 *
 */

public class TestPlanDataManager extends DataManager {

	private Map<ActorId, Object> actorAliasMap = new LinkedHashMap<>();

	private Map<DataManagerId, Object> dataManagerAliasMap = new LinkedHashMap<>();

	private final Map<Object, List<TestActorPlan>> actorActionPlanMap = new LinkedHashMap<>();

	private final Map<Object, List<TestDataManagerPlan>> dataManagerActionPlanMap = new LinkedHashMap<>();

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

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		dataManagerContext.subscribeToSimulationClose(this::sendActionCompletionReport);
	}

	public List<TestActorPlan> getTestActorPlans(Object alias) {
		List<TestActorPlan> result = new ArrayList<>();
		List<TestActorPlan> list = actorActionPlanMap.get(alias);
		if (list != null) {
			result.addAll(list);
		}
		return result;
	}

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

	public Optional<Object> getActorAlias(ActorId actorId) {
		return Optional.ofNullable(actorAliasMap.get(actorId));
	}

	public void setActorAlias(ActorId actorId, Object alias) {
		if ((actorId != null) && (alias != null)) {
			actorAliasMap.put(actorId, alias);
		}
	}

	public Optional<Object> getDataManagerAlias(DataManagerId dataManagerId) {
		return Optional.ofNullable(dataManagerAliasMap.get(dataManagerId));
	}

	public void setDataManagerAlias(DataManagerId dataManagerId, Object alias) {
		if ((dataManagerId != null) & (alias != null)) {
			dataManagerAliasMap.put(dataManagerId, alias);
		}
	}

}
