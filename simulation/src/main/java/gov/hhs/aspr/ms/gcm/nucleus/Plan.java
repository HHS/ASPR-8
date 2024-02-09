package gov.hhs.aspr.ms.gcm.nucleus;

public abstract class Plan {
	protected double time;
	protected boolean isActive = true;
	long arrivalId = -1;
	protected Object key;

	protected Plan(double time) {
		this.time = time;
	}

	protected Plan(double time, boolean active) {
		this.time = time;
		this.isActive = active;
	}

	public final double getTime() {
		return time;
	}

	public final boolean isActive() {
		return isActive;
	}

	public final void cancelPlan() {
		this.isActive = false;
	}

	public final long getArrivalId() {
		return arrivalId;
	}

	abstract Planner getPlanner();
}
