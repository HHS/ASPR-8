package lesson.plugins.disease;

import gov.hhs.aspr.ms.gcm.nucleus.DataManager;
import gov.hhs.aspr.ms.gcm.nucleus.DataManagerContext;

public final class DiseaseDataManager extends DataManager {

	private double r0 = 7;

	private double asymptomaticDays;

	private double symptomaticDays;

	private final DiseasePluginData diseasePluginData;

	public DiseaseDataManager(DiseasePluginData diseasePluginData) {
		this.diseasePluginData = diseasePluginData;
	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);

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
	}

	public void setAsymptomaticDays(double asymptomaticDays) {
		this.asymptomaticDays = asymptomaticDays;
	}

	public void setSymptomaticDays(double symptomaticDays) {
		this.symptomaticDays = symptomaticDays;
	}

}
