package nucleus.testsupport.actionplugin;

import java.util.List;

import nucleus.DataManager;
import nucleus.DataManagerContext;

public class ActionDataManager extends DataManager {
	private final Object alias;
	public ActionDataManager(Object alias) {
		this.alias = alias;
	}
	
	@Override
	protected void init(DataManagerContext dataManagerContext) {
		ActionPluginDataManager actionPluginDataManager = dataManagerContext.getDataManager(ActionPluginDataManager.class).get();
		actionPluginDataManager.setDataManagerAlias(dataManagerContext.getDataManagerId(),alias);
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
