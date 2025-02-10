package gov.hhs.aspr.ms.gcm.lessons;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.lessons.plugins.disease.DiseasePlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.disease.DiseasePluginData;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.policy.PolicyPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.policy.PolicyPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.FunctionalDimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.FunctionalDimensionData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

/* start code_ref=experiments_example_steamlined_dimension|code_cap=Example 9 C improves on the creation of the R0 dimension.*/
public final class Example_9_C {

	private Example_9_C() {
	}

	private static DiseasePluginData getDiseasePluginData() {
		return DiseasePluginData.builder()//
				.setR0(1.5)//
				.setAsymptomaticDays(4.0)//
				.setSymptomaticDays(12.0)//
				.build();
	}

	private static PolicyPluginData getPolicyPluginData() {
		return PolicyPluginData.builder()//
				.setDistributeVaccineLocally(true)//
				.setSchoolClosingInfectionRate(0.05)//
				.build();
	}

	private static Dimension getDimension() {
		FunctionalDimensionData.Builder builder = FunctionalDimensionData.builder();//

		List<Double> r0Values = new ArrayList<>();
		r0Values.add(0.5);
		r0Values.add(0.75);
		r0Values.add(1.0);
		r0Values.add(1.5);
		r0Values.add(2.0);
		r0Values.add(2.5);

		for (int i = 0; i < r0Values.size(); i++) {
			Double r0 = r0Values.get(i);
			builder.addValue("Level_" + i, (context) -> {
				DiseasePluginData.Builder pluginDataBuilder = context
						.getPluginDataBuilder(DiseasePluginData.Builder.class);
				pluginDataBuilder.setR0(r0);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(r0));
				return result;
			});//
		}
		builder.addMetaDatum("r0");//

		FunctionalDimensionData dimensionData =  builder.build();
		return new FunctionalDimension(dimensionData);
	}
	/* end */

	/* start code_ref=experiments_example_9_C|code_cap=Execution of the experiment is cleaner.*/
	public static void main(String[] args) {

		DiseasePluginData diseasePluginData = getDiseasePluginData();
		Plugin diseasePlugin = DiseasePlugin.getDiseasePlugin(diseasePluginData);

		PolicyPluginData policyPluginData = getPolicyPluginData();
		Plugin policyPlugin = PolicyPlugin.getPolicyPlugin(policyPluginData);

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		Dimension dimension = getDimension();

		Experiment.builder()//
				.addPlugin(diseasePlugin)//
				.addPlugin(modelPlugin)//
				.addPlugin(policyPlugin)//
				.addDimension(dimension)//
				.build()//
				.execute();
	}

	/* end */
}
