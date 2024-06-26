package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class ActorPlan extends Plan {

	// The actor id is used by the simulation via package access
	ActorId actorId;

	private final Consumer<ActorContext> consumer;

	/**
	 * Constructs the plan scheduled for the given time active status arrivalId and
	 * consumer
	 * 
	 * @throw {@link ContractException}
	 *        <ul>
	 *        <li>{@linkplain NucleusError#NULL_PLAN_CONSUMER} if the consumer is
	 *        null</li>
	 *        </ul>
	 * 
	 */
	public ActorPlan(double time, boolean active, long arrivalId, Consumer<ActorContext> consumer) {
		super(time, active, arrivalId, Planner.ACTOR);
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
		this.consumer = consumer;
	}

	/**
	 * Constructs the plan scheduled for the given time and consumer. The plan will
	 * be active.The arrival id is set to -1L indicating that this is a new,
	 * non-deserialized plan.
	 * 
	 * @throw {@link ContractException}
	 *        <ul>
	 *        <li>{@linkplain NucleusError#NULL_PLAN_CONSUMER} if the consumer is
	 *        null</li>
	 *        </ul>
	 * 
	 */
	public ActorPlan(double time, Consumer<ActorContext> consumer) {
		super(time, true, -1L, Planner.ACTOR);
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
		this.consumer = consumer;
	}

	/**
	 * Constructs the plan scheduled for the given time, active status and consumer.
	 * The arrival id is set to -1L indicating that this is a new, non-deserialized
	 * plan.
	 * 
	 * @throw {@link ContractException}
	 *        <ul>
	 *        <li>{@linkplain NucleusError#NULL_PLAN_CONSUMER} if the consumer is
	 *        null</li>
	 *        </ul>
	 * 
	 */
	public ActorPlan(double time, boolean active, Consumer<ActorContext> consumer) {
		super(time, active, -1L, Planner.ACTOR);
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
		this.consumer = consumer;
	}

	protected void execute(ActorContext context) {
		this.consumer.accept(context);
	}
}
