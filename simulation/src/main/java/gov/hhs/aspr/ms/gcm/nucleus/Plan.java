package gov.hhs.aspr.ms.gcm.nucleus;

abstract class Plan {
	protected final double time;
	protected final Planner planner;
	protected boolean isActive = true;

	boolean canceled = false;
	long arrivalId = -1;

	Object key = new Object();

	protected Plan(double time, Planner planner) {
		this.time = time;
		this.planner = planner;
	}

	protected Plan(double time, boolean active, Planner planner) {
		this(time, planner);
		this.isActive = active;
	}

	protected Plan(double time, Planner planner, Object key) {
		this(time, planner);
		this.key = key;
	}

	protected Plan(double time, boolean active, Planner planner, Object key) {
		this(time, active, planner);
		this.key = key;
	}

	public final double getTime() {
		return time;
	}

	public final boolean isActive() {
		return isActive;
	}

	public final void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public final void cancelPlan() {
		this.canceled = true;
	}

	abstract void validate(double simTime);
}
