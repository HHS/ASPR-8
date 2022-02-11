package nucleus.testsupport.actionplugin;

import java.util.Set;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.ResolverId;

/**
 * Test Support resolver implementation designed to execute test-defined behaviors
 * from within the resolver. It schedules the ActionPlans that were stored in the
 * ActionDataView that were associated with its resolver id.
 * 
 * 
 * @author Shawn Hatch
 *
 */
public final class ActionResolver extends DataManager{

	public void init(DataManagerContext dataManagerContext) {
		ActionDataView actionDataView = dataManagerContext.getDataView(ActionDataView.class).get();

		// retrieve the action plans from the action data view and schedule them
		// with the context
		ResolverId resolverId = dataManagerContext.getCurrentResolverId();
		Set<DataManagerActionPlan> dataManagerActionPlans = actionDataView.getResolverActionPlans(resolverId);
		for (final DataManagerActionPlan dataManagerActionPlan : dataManagerActionPlans) {
			dataManagerContext.addPlan(dataManagerActionPlan::executeAction, dataManagerActionPlan.getScheduledTime());
		}
	}

}
