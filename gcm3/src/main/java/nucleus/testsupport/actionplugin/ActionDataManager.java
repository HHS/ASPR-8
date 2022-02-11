package nucleus.testsupport.actionplugin;

import java.util.List;

import nucleus.DataManager;
import nucleus.DataManagerContext;

public class ActionDataManager extends DataManager {

	@Override
	protected void init(DataManagerContext dataManagerContext) {
		ActionPluginDataManager actionPluginDataManager = dataManagerContext.getDataManager(ActionPluginDataManager.class).get();

		List<DataManagerActionPlan> dataManagerActionPlans = actionPluginDataManager.getDataManagerActionPlans(this.getClass());
		for (final DataManagerActionPlan dataManagerActionPlan : dataManagerActionPlans) {
			if (dataManagerActionPlan.getKey() != null) {
				dataManagerContext.addKeyedPlan(dataManagerActionPlan::executeAction, dataManagerActionPlan.getScheduledTime(), dataManagerActionPlan.getKey());
			} else {
				dataManagerContext.addPlan(dataManagerActionPlan::executeAction, dataManagerActionPlan.getScheduledTime());
			}
		}
	}

}
