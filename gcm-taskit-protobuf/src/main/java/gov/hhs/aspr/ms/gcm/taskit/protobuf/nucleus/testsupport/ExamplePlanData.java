package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.testsupport;

import java.util.Objects;

import gov.hhs.aspr.ms.gcm.nucleus.PlanData;

/**
 * Example implementation of {@link PlanData}
 */
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