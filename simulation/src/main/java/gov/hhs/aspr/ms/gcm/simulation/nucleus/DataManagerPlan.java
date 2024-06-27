package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class DataManagerPlan extends Plan {
	// The data manager id is used by the simulation via package access
	DataManagerId dataManagerId;

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
	public DataManagerPlan(double time, boolean active, long arrivalId, Consumer<DataManagerContext> consumer) {
		super(time, active, arrivalId, Planner.DATA_MANAGER);
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
	public DataManagerPlan(double time, boolean active, Consumer<DataManagerContext> consumer) {
		super(time, active, -1L, Planner.DATA_MANAGER);
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
	public DataManagerPlan(double time, Consumer<DataManagerContext> consumer) {
		super(time, true, -1L, Planner.DATA_MANAGER);
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}

		this.consumer = consumer;
	}

	protected void execute(DataManagerContext context) {
		this.consumer.accept(context);
	}
}
