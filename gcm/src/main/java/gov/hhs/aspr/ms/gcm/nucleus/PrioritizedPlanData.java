package gov.hhs.aspr.ms.gcm.nucleus;

/**
 * Combines a PlanData with a priority value used to reconstruct the order of
 * plans from de-serialized plans from a previous simulation execution.
 */
public final class PrioritizedPlanData {
	private final PlanData planData;
	private final long priority;

	/**
	 * Constructs this PrioritizedPlanData from the give PlanData and priority value
	 */
	public PrioritizedPlanData(PlanData planData, long priority) {
		this.planData = planData;
		this.priority = priority;
	}

	/**
	 * Returns the plan data
	 */
	@SuppressWarnings("unchecked")
	public <T extends PlanData> T getPlanData() {
		return (T) planData;
	}

	/**
	 * Returns the priority
	 */
	public long getPriority() {
		return priority;
	}

	/**
	 * Returns the string representation of a prioritized plan data in the form
	 * PrioritizedPlanData [planData=X, priority=1234] where X= the relevant plan
	 * data's toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PrioritizedPlanData [planData=");
		builder.append(planData);
		builder.append(", priority=");
		builder.append(priority);
		builder.append("]");
		return builder.toString();
	}

}
