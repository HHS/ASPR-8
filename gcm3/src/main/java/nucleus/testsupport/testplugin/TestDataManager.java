package nucleus.testsupport.testplugin;

import java.util.List;

import nucleus.DataManager;
import nucleus.DataManagerContext;

public class TestDataManager extends DataManager {
	private Object alias;
	
	void setAlias(Object alias){
		this.alias = alias;
	}
	
	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		TestPlanDataManager testPlanDataManager = dataManagerContext.getDataManager(TestPlanDataManager.class).get();
		testPlanDataManager.setDataManagerAlias(dataManagerContext.getDataManagerId(),alias);
		List<TestDataManagerPlan> testDataManagerPlans = testPlanDataManager.getTestDataManagerPlans(alias);
		for (final TestDataManagerPlan testDataManagerPlan : testDataManagerPlans) {
			if (testDataManagerPlan.getKey() != null) {
				dataManagerContext.addKeyedPlan(testDataManagerPlan::executeAction, testDataManagerPlan.getScheduledTime(), testDataManagerPlan.getKey());
			} else {
				dataManagerContext.addPlan(testDataManagerPlan::executeAction, testDataManagerPlan.getScheduledTime());
			}
		}
	}

}
