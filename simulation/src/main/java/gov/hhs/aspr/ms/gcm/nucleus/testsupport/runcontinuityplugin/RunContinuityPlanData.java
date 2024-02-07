package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import gov.hhs.aspr.ms.gcm.nucleus.PlanData;

public final class RunContinuityPlanData implements PlanData {

	private final int id;

	public RunContinuityPlanData(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}