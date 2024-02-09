package gov.hhs.aspr.ms.gcm.nucleus;

abstract class Plan {
	protected final double time;
	protected final Planner planner;
	protected final boolean isActive;

	boolean canceled = false;
	long arrivalId = -1;

//	protected Plan(double time, boolean active, long arrivalId,Planner planner) {
	/*
	 * Do we throw an execption here if the arrival id is < -1? Or do we detect this
	 * in the sim? Or do we just tolerate it in the sim by changing the logic for
	 * resetting the arrival id?
	 */
//		this.time = time;
//		this.isActive = active;
//		this.arrivalId = arrivalId;
//		this.planner = planner;
//	}

	protected Plan(double time, Planner planner) {
		this.time = time;
		this.planner = planner;
		this.isActive = true;
	}

	protected Plan(double time, boolean active, Planner planner) {
		this.time = time;
		this.planner = planner;
		this.isActive = active;
	}

	public final double getTime() {
		return time;
	}

	public final boolean isActive() {
		return isActive;
	}

	public final void cancelPlan() {
		this.canceled = true;
	}

	abstract void validate(double simTime);
}
