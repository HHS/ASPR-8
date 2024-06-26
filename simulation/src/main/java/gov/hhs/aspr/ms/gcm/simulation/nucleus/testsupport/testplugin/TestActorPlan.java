package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorPlan;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Test Support class that describes an action for an actor as a scheduled plan
 * with an optional key.
 */
public class TestActorPlan extends ActorPlan {
	private boolean executed;
	private Consumer<ActorContext> consumer;

	/**
	 * Constructs an actor action plan. If assignKey is false, then this actor
	 * action plan will return an empty optional key.
	 * 
	 * @throws ContractException {@linkplain TestError#NULL_PLAN} if the plan is
	 *                           null
	 */
	public TestActorPlan(final double scheduledTime, Consumer<ActorContext> consumer) {
		super(scheduledTime, consumer);
		if (consumer == null) {
			throw new ContractException(TestError.NULL_PLAN);
		}
		this.consumer = consumer;
	}

	/**
	 * Boilerplate implementation of hashCode consistent with equals()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (executed ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(getTime());
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * TestActorPlans are equal if and only they return the same values for
	 * 1)executed() and 2)getScheduledTime()This limited sense of equality is
	 * present simply to provide some reasonable evidence that the plugin data
	 * cloning method is working correctly for the test plugin data.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TestActorPlan)) {
			return false;
		}
		TestActorPlan other = (TestActorPlan) obj;
		if (executed != other.executed) {
			return false;
		}

		if (Double.doubleToLongBits(getTime()) != Double.doubleToLongBits(other.getTime())) {
			return false;
		}
		return true;
	}

	/**
	 * Constructs a test actor plan from another test actor plan.
	 */
	public TestActorPlan(TestActorPlan testActorPlan) {
		super(testActorPlan.getTime(), testActorPlan.consumer);
		executed = testActorPlan.executed;
		consumer = testActorPlan.consumer;
	}

	/**
	 * Returns true if an only if this actor action plan was executed
	 */
	public boolean executed() {
		return executed;
	}

	@Override
	protected void execute(final ActorContext actorContext) {
		try {
			super.execute(actorContext);			
		} finally {
			executed = true;
		}
	}

}
