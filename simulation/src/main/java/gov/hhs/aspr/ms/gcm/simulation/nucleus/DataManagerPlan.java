package gov.hhs.aspr.ms.gcm.simulation.nucleus;

public abstract class DataManagerPlan extends Plan {
	// The data manager id is used by the simulation via package access
	DataManagerId dataManagerId;

	

	/**
	 * Constructs the plan scheduled for the given time active status and arrivalId.
	 * 
	
	 */
	public DataManagerPlan(double time, boolean active, long arrivalId) {
		super(time, active, arrivalId, Planner.DATA_MANAGER);
		
	}

	/**
	 * Constructs the plan scheduled for the given time and active status.
	 * The arrival id is set to -1L indicating that this is a new, non-deserialized
	 * plan.
	 
	 * 
	 */
	public DataManagerPlan(double time, boolean active) {
		super(time, active, -1L, Planner.DATA_MANAGER);
		
	}

	/**
	 * Constructs the plan scheduled for the given time. The plan will
	 * be active.The arrival id is set to -1L indicating that this is a new,
	 * non-deserialized plan.
	 * 
	
	 * 
	 */
	public DataManagerPlan(double time) {
		super(time, true, -1L, Planner.DATA_MANAGER);
		
	}
	
	/**
	 * Executes the data manager logic associated with the plan.
	 */
	protected abstract void execute(DataManagerContext context);
	
}
