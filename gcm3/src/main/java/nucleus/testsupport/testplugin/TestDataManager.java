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
	protected void init(DataManagerContext dataManagerContext) {
		TestPluginDataManager testPluginDataManager = dataManagerContext.getDataManager(TestPluginDataManager.class).get();
		testPluginDataManager.setDataManagerAlias(dataManagerContext.getDataManagerId(),alias);
		List<TestDataManagerPlan> testDataManagerPlans = testPluginDataManager.getDataManagerActionPlans(this.getClass());
		for (final TestDataManagerPlan testDataManagerPlan : testDataManagerPlans) {
			if (testDataManagerPlan.getKey() != null) {
				dataManagerContext.addKeyedPlan(testDataManagerPlan::executeAction, testDataManagerPlan.getScheduledTime(), testDataManagerPlan.getKey());
			} else {
				dataManagerContext.addPlan(testDataManagerPlan::executeAction, testDataManagerPlan.getScheduledTime());
			}
		}
	}

}
