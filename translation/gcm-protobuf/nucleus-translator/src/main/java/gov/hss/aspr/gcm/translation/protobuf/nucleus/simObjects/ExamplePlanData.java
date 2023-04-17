package gov.hss.aspr.gcm.translation.protobuf.nucleus.simObjects;

import nucleus.PlanData;

public final class ExamplePlanData implements PlanData {
	private final double planTime;

	public ExamplePlanData(double planTime) {
		super();
		this.planTime = planTime;
	}

	public double getPlanTime() {
		return planTime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExamplePlanData [planTime=");
		builder.append(planTime);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ExamplePlanData)) {
			return false;
		}
		ExamplePlanData other = (ExamplePlanData) obj;

		return getPlanTime() == other.getPlanTime();

	}

}