package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class ConsumerDataManagerPlan extends DataManagerPlan {
	
	private final Consumer<DataManagerContext> consumer;

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
	public ConsumerDataManagerPlan(double time, boolean active, long arrivalId, Consumer<DataManagerContext> consumer) {
		super(time, active, arrivalId);
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
	public ConsumerDataManagerPlan(double time, boolean active, Consumer<DataManagerContext> consumer) {
		super(time, active);
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
	public ConsumerDataManagerPlan(double time, Consumer<DataManagerContext> consumer) {
		super(time);
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}

		this.consumer = consumer;
	}

	@Override
	protected void execute(DataManagerContext context) {
		this.consumer.accept(context);
	}
}
