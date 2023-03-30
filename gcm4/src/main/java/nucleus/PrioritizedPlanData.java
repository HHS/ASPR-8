package nucleus;

public final class PrioritizedPlanData {
	private final PlanData planData;
	private final long priority;
	
	public PrioritizedPlanData(PlanData planData, long priority) {
		this.planData = planData;
		this.priority = priority;
	}

	@SuppressWarnings("unchecked")
	public <T extends PlanData> T getPlanData() {
		return (T)planData;
	}

	public long getPriority() {
		return priority;
	}
}
