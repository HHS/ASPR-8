package nucleus.testsupport.testplugin;

import java.util.List;

import nucleus.ActorContext;

/**
 * Test Support actor implementation designed to execute test-defined behaviors
 * from within the actor. The actor first associates its ActorId with its alias
 * via an AliasAssignmentEvent. It then schedules the ActorActionPlans that were
 * stored in the ActionDataView that were associated with its alias.
 * 
 * Alias identification exists for the convenience of the test implementor so
 * that tests can name actors and are not bound to the forced ordering pattern
 * implied by ActorId values.
 * 
 * @author Shawn Hatch
 *
 */
public final class TestActor {
	private final Object alias;
	public TestActor(Object alias) {
		this.alias = alias;
	}
	
	/**
	 * Associates its ActorId with its alias via an AliasAssignmentEvent. Schedules the
	 * ActorActionPlans that were stored in the ActionDataView that were associated
	 * with its alias.
	 */
	public void init(ActorContext actorContext) {
		TestPlanDataManager testPlanDataManager = actorContext.getDataManager(TestPlanDataManager.class).get();
		testPlanDataManager.setActorAlias(actorContext.getActorId(),alias);
		List<TestActorPlan> testActorPlans = testPlanDataManager.getTestActorPlans(alias);
		for (final TestActorPlan testActorPlan : testActorPlans) {
			if (testActorPlan.getKey() != null) {
				actorContext.addKeyedPlan(testActorPlan::executeAction, testActorPlan.getScheduledTime(), testActorPlan.getKey());
			} else {
				actorContext.addPlan(testActorPlan::executeAction, testActorPlan.getScheduledTime());
			}
		}
	}

}
