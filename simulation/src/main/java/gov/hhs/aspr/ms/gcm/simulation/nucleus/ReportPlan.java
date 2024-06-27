package gov.hhs.aspr.ms.gcm.simulation.nucleus;

public abstract class ReportPlan extends Plan {
	ReportId reportId;
	

	/**
	 * Constructs the plan scheduled for the given time and arrivalId.
	 * Report plans are always passive.
	 * 
	 */
	public ReportPlan(double time, long arrivalId) {
		super(time, false, arrivalId, Planner.REPORT);		
	}

	/**
	 * Constructs the plan scheduled for the given time. Report plans
	 * are always passive.The arrival id is set to -1L indicating that this is a
	 * new, non-deserialized plan.
	 */
	public ReportPlan(double time) {
		super(time, false, -1L, Planner.REPORT);
	}

	protected abstract void execute(ReportContext context);
}
