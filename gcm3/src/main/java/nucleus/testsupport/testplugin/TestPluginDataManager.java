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

public class TestPluginDataManager extends DataManager {

	private Map<ActorId, Object> actorAliasMap = new LinkedHashMap<>();

	private Map<DataManagerId, Object> dataManagerAliasMap = new LinkedHashMap<>();

	private final Map<Object, List<TestActorPlan>> actorActionPlanMap = new LinkedHashMap<>();

	private final Map<Class<? extends TestDataManager>, List<TestDataManagerPlan>> dataManagerActionPlanMap = new LinkedHashMap<>();

	public TestPluginDataManager(TestPluginData testPluginData) {

		for (Object alias : testPluginData.getActorsRequiringPlanning()) {
			List<TestActorPlan> newActorActionPlans = new ArrayList<>();
			actorActionPlanMap.put(alias, newActorActionPlans);
			List<TestActorPlan> testActorPlans = testPluginData.getTestActorPlans(alias);
			for (TestActorPlan testActorPlan : testActorPlans) {
				newActorActionPlans.add(new TestActorPlan(testActorPlan));
			}
		}

		for (Object alias : testPluginData.getTestDataManagerAliases()) {
			Class<? extends TestDataManager> c = testPluginData.getTestDataManagerType(alias).get();
			List<TestDataManagerPlan> newDataManagerActionPlans = new ArrayList<>();
			dataManagerActionPlanMap.put(c, newDataManagerActionPlans);
			for (TestDataManagerPlan testDataManagerPlan : testPluginData.getTestDataManagerPlans(alias)) {
				newDataManagerActionPlans.add(new TestDataManagerPlan(testDataManagerPlan));
			}
		}
	}

	@Override
	protected void init(DataManagerContext dataManagerContext) {
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

	public List<TestDataManagerPlan> getDataManagerActionPlans(Class<? extends TestDataManager> actionDataManagerClass) {
		List<TestDataManagerPlan> result = new ArrayList<>();

		List<TestDataManagerPlan> list = dataManagerActionPlanMap.get(actionDataManagerClass);
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

		for (Class<? extends TestDataManager> c : dataManagerActionPlanMap.keySet()) {
			for (TestDataManagerPlan testDataManagerPlan : dataManagerActionPlanMap.get(c)) {
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
