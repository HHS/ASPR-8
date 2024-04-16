package gov.hhs.aspr.ms.gcm.lessons.plugins.policy;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DataManager;

public final class PolicyDataManager extends DataManager {
	private double schoolClosingInfectionRate;
	private boolean distributeVaccineLocally;

	public PolicyDataManager(PolicyPluginData policyPluginData) {

	}

	public double getSchoolClosingInfectionRate() {
		return schoolClosingInfectionRate;
	}

	public void setSchoolClosingInfectionRate(double schoolClosingInfectionRate) {
		this.schoolClosingInfectionRate = schoolClosingInfectionRate;
	}

	public boolean distributeVaccineLocally() {
		return distributeVaccineLocally;
	}

	public void setDistributeVaccineLocally(boolean distributeVaccineLocally) {
		this.distributeVaccineLocally = distributeVaccineLocally;
	}

}
