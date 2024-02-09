package gov.hhs.aspr.ms.gcm.nucleus;

public abstract class Plan {
	private double time;
	private boolean active = true;
	long arrivalId = -1;

	protected Plan(double time) {
		this.time = time;
	}

	protected Plan(double time, boolean active) {
		this.time = time;
		this.active = active;
	}

	public final double getTime() {
		return time;
	}

	public final boolean isActive() {
		return active;
	}

	public final void setActive(boolean active) {
		this.active = active;
	}

	public final long getArrivalId() {
		return arrivalId;
	}

}
