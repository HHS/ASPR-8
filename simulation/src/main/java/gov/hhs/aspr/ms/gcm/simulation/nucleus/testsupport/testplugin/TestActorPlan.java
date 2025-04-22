package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin;

import java.util.Objects;
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
		super(scheduledTime);
		if (consumer == null) {
			throw new ContractException(TestError.NULL_PLAN);
		}
		this.consumer = consumer;
	}

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(executed, getTime());
	}

	/**
	 * Two {@link TestActorPlan} instances are equal if and only if
	 * they return the same values for 1) executed() and 2) getTime(). 
	 * This limited sense of equality is present simply to provide some reasonable 
	 * evidence that the plugin data cloning method is working correctly for the 
	 * test plugin data.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
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
		super(testActorPlan.getTime());
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
			consumer.accept(actorContext);		
		} finally {
			executed = true;
		}
	}

}
