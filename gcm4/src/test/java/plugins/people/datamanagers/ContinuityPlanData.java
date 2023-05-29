package plugins.people.datamanagers;

import nucleus.PlanData;

public final class ContinuityPlanData implements PlanData {
	private final int id;

	public ContinuityPlanData(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

}