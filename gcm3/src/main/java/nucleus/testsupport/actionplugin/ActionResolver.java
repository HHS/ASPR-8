package nucleus.testsupport.actionplugin;

import java.util.Set;

import nucleus.ResolverContext;
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
public final class ActionResolver {

	public void init(ResolverContext resolverContext) {
		ActionDataView actionDataView = resolverContext.getDataView(ActionDataView.class).get();

		// retrieve the action plans from the action data view and schedule them
		// with the context
		ResolverId resolverId = resolverContext.getCurrentResolverId();
		Set<ResolverActionPlan> resolverActionPlans = actionDataView.getResolverActionPlans(resolverId);
		for (final ResolverActionPlan resolverActionPlan : resolverActionPlans) {
			resolverContext.addPlan(resolverActionPlan::executeAction, resolverActionPlan.getScheduledTime());
		}
	}

}
