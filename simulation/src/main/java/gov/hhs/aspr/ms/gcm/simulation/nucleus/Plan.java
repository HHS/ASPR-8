package gov.hhs.aspr.ms.gcm.simulation.nucleus;

/**
 * Base class for plans. It is purposely kept as a package access only class.
 */
abstract class Plan {
	
	//internal state of the plan is package access to make the sim slightly faster
	final double time;
	final Planner planner;
	final boolean isActive;
	boolean canceled = false;
	long arrivalId = -1;

	/**
	 * Package access constructor
	 */
	Plan(double time, boolean active, long arrivalId, Planner planner) {
		/*
		 * Do we throw an execption here if the arrival id is < -1? Or do we detect this
		 * in the sim? Or do we just tolerate it in the sim by changing the logic for
		 * resetting the arrival id?
		 */
		this.time = time;
		this.isActive = active;
		this.arrivalId = arrivalId;
		this.planner = planner;
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
	
	public final long getArrivalId() {
		return arrivalId;
	}
}
