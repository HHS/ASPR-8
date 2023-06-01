package nucleus.testsupport.runcontinuityplugin;

import nucleus.PlanData;

public final class RunContinuityPlanData implements PlanData {
	private final int id;

	public RunContinuityPlanData(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

}