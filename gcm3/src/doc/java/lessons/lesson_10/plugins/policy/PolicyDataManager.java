package lessons.lesson_10.plugins.policy;

import nucleus.DataManager;
import nucleus.DataManagerContext;

public final class PolicyDataManager extends DataManager{
	private double schoolClosingInfectionRate;
	private boolean distributeVaccineLocally;
	
	private final PolicyPluginData policyPluginData;
	
	public PolicyDataManager(PolicyPluginData policyPluginData){
		this.policyPluginData = policyPluginData;
	}
	
	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		schoolClosingInfectionRate = policyPluginData.getSchoolClosingInfectionRate();
		distributeVaccineLocally = policyPluginData.distributeVaccineLocally();
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
