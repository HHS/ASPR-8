package gov.hhs.aspr.ms.gcm.simulation.nucleus;

public abstract class ActorPlan extends Plan {

	// The actor id is used by the simulation via package access
	ActorId actorId;

	

	/**
	 * Constructs the plan scheduled for the given time, active status and arrivalId.
	 *	
	 * 
	 */
	public ActorPlan(double time, boolean active, long arrivalId) {
		super(time, active, arrivalId, Planner.ACTOR);
	}

	/**
	 * Constructs the plan scheduled for the given time. The plan will
	 * be active.The arrival id is set to -1L indicating that this is a new,
	 * non-deserialized plan.
	 * 
	 * 
	 */
	public ActorPlan(double time) {
		super(time, true, -1L, Planner.ACTOR);
	}

	/**
	 * Constructs the plan scheduled for the given time and active status.
	 * The arrival id is set to -1L indicating that this is a new, non-deserialized
	 * plan.
	 *	
	 * 
	 */
	public ActorPlan(double time, boolean active) {
		super(time, active, -1L, Planner.ACTOR);
	}

	/**
	 * Executes the actor logic associated with the plan.
	 */
	protected abstract void execute(ActorContext context);
}
