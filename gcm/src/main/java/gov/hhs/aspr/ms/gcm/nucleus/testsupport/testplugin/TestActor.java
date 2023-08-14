package gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin;

import java.util.List;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;

/**
 * Test Support actor implementation designed to execute test-defined behaviors
 * from within the actor. The actor first registers its ActorId with its alias
 * by registering it with the TestPlanDataManager. It then schedules the
 * ActorActionPlans that were stored in the TestPluginData that were associated
 * with its alias.
 * 
 * Alias identification exists for the convenience of the test implementor so
 * that tests can name actors and are not bound to the forced ordering pattern
 * implied by ActorId values.
 */
public final class TestActor {
	private final Object alias;

	/**
	 * Creates the test actor with its alias
	 */
	public TestActor(Object alias) {
		this.alias = alias;
	}

	/**
	 * Associates its ActorId. Schedules the ActorActionPlans that were stored
	 * in the ActionDataView that were associated with its alias.
	 */
	public void init(ActorContext actorContext) {
		TestPlanDataManager testPlanDataManager = actorContext.getDataManager(TestPlanDataManager.class);

		List<TestActorPlan> testActorPlans = testPlanDataManager.getTestActorPlans(alias);
		for (final TestActorPlan testActorPlan : testActorPlans) {
			actorContext.addPlan(testActorPlan::executeAction, testActorPlan.getScheduledTime());
		}
	}

}
