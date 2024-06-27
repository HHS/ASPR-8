package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.util.function.Consumer;

import gov.hhs.aspr.ms.util.errors.ContractException;

public class ConsumerReportPlan extends ReportPlan {
	
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
	public ConsumerReportPlan(double time, long arrivalId, Consumer<ReportContext> consumer) {
		super(time, arrivalId);
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
	public ConsumerReportPlan(double time, Consumer<ReportContext> consumer) {
		super(time);
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_PLAN_CONSUMER);
		}
		this.consumer = consumer;
	}

	@Override
	protected void execute(ReportContext context) {		
		this.consumer.accept(context);
	}
}
