package gov.hhs.aspr.ms.gcm.nucleus;

public abstract class Plan {
	protected final double time;
	protected boolean isActive = true;
	protected Planner planner;

	boolean canceled = false;
	long arrivalId = -1;
	
	final String key;

	protected Plan(double time, String key) {
		this.time = time;
		this.key = key;
	}

	protected Plan(double time, boolean active, String key) {
		this(time, key);
		this.isActive = active;
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
}
