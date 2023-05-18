package gov.hhs.aspr.gcm.translation.protobuf.nucleus.simObjects;

import java.util.Objects;

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
	public int hashCode() {
		return Objects.hash(planTime);
	}

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
		ExamplePlanData other = (ExamplePlanData) obj;
		return Double.doubleToLongBits(planTime) == Double.doubleToLongBits(other.planTime);
	}
	
}