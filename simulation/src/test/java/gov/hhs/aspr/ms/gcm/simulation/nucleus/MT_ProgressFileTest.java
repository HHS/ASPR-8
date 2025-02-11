package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MT_ProgressFileTest {
	public static void main(String[] args) {

		Path experimentProgressPath = Paths.get(args[0]);
		FunctionalDimensionData dimensionData1 = FunctionalDimensionData.builder()//
										.addMetaDatum("alpha")//
										.addMetaDatum("beta")//
										.addValue("Level_0", (t) -> {
											List<String> result = new ArrayList<>();
											result.add("45");
											result.add("fred");
											return result;
										})//
										.addValue("Level_1", (t) -> {
											List<String> result = new ArrayList<>();
											result.add("66");
											result.add("betty");
											return result;
										})//

										.build();
		Dimension dimension1 = new FunctionalDimension(dimensionData1);

		FunctionalDimensionData dimensionData2 = FunctionalDimensionData.builder()//
										.addMetaDatum("comet")//
										.addMetaDatum("cupid")//
										.addMetaDatum("vixen")//
										.addValue("Level_0", (t) -> {
											List<String> result = new ArrayList<>();
											result.add("1");
											result.add("2");
											result.add("3");
											return result;
										})//
										.addValue("Level_1", (t) -> {
											List<String> result = new ArrayList<>();
											result.add("4");
											result.add("5");
											result.add("6");
											return result;
										})//
										.addValue("Level_2", (t) -> {
											List<String> result = new ArrayList<>();
											result.add("7");
											result.add("8");
											result.add("9");
											return result;
										})//

										.build();
		Dimension dimension2 = new FunctionalDimension(dimensionData2);

		ExperimentParameterData experimentParameterData = ExperimentParameterData	.builder()//
																					.setContinueFromProgressLog(true)//
																					.setExperimentProgressLog(experimentProgressPath)//
																					.build();//

		Experiment//
					.builder()//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.setExperimentParameterData(experimentParameterData)//
					.build()//
					.execute();//

	}
}
