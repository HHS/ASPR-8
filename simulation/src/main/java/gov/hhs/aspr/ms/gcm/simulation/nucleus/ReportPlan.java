package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class ReportPlan extends Plan {
	ReportId reportId;
	private final Consumer<ReportContext> consumer;

	/**
	 * Constructs the plan scheduled for the given time, arrivalId and consumer.
	 * Report plans are always passive.
	 * 
	 * @throw {@link ContractException}
	 *        <ul>
	 *        <li>{@linkplain NucleusError#NULL_PLAN_CONSUMER} if the consumer is
	 *        null</li>
	 *        </ul>
	 * 
	 */
	public ReportPlan(double time, long arrivalId, Consumer<ReportContext> consumer) {
		super(time, false, arrivalId, Planner.REPORT);
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
		this.consumer = consumer;
	}

	/**
	 * Constructs the plan scheduled for the given time and consumer. Report plans
	 * are always passive.The arrival id is set to -1L indicating that this is a
	 * new, non-deserialized plan.
	 * 
	 * @throw {@link ContractException}
	 *        <ul>
	 *        <li>{@linkplain NucleusError#NULL_PLAN_CONSUMER} if the consumer is
	 *        null</li>
	 *        </ul>
	 * 
	 */
	public ReportPlan(double time, Consumer<ReportContext> consumer) {
		super(time, false, -1L, Planner.REPORT);
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
		this.consumer = consumer;
	}

	protected void execute(ReportContext context) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
		this.consumer.accept(context);
	}
}
