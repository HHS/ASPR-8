package lesson.plugins.disease;

import nucleus.DataManager;
import nucleus.DataManagerContext;

public final class DiseaseDataManager extends DataManager {

	private double r0 = 7;

	private double asymptomaticDays;

	private double symptomaticDays;

	private final DiseasePluginData diseasePluginData;

	private DataManagerContext dataManagerContext;

	public DiseaseDataManager(DiseasePluginData diseasePluginData) {
		this.diseasePluginData = diseasePluginData;
	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		this.dataManagerContext = dataManagerContext;
		r0 = diseasePluginData.getR0();
		asymptomaticDays = diseasePluginData.getAsymptomaticDays();
		symptomaticDays = diseasePluginData.getSymptomaticDays();

	}

	public double getR0() {
		return r0;
	}

	public double getAsymptomaticDays() {
		return asymptomaticDays;
	}

	public double getSymptomaticDays() {
		return symptomaticDays;
	}

	public void setR0(double r0) {
		this.r0 = r0;
		dataManagerContext.releaseOutput("setting R0 to "+r0+" at time = "+dataManagerContext.getTime());
	}

	public void setAsymptomaticDays(double asymptomaticDays) {
		this.asymptomaticDays = asymptomaticDays;
	}

	public void setSymptomaticDays(double symptomaticDays) {
		this.symptomaticDays = symptomaticDays;
	}

}
