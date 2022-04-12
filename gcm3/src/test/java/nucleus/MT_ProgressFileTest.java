package nucleus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MT_ProgressFileTest {
	public static void main(String[] args) {

		Path experimentProgressPath = Paths.get(args[0]);
		Dimension dimension1 = Dimension.builder()//
										.addMetaDatum("alpha")//
										.addMetaDatum("beta")//
										.addPoint((t) -> {
											List<String> result = new ArrayList<>();
											result.add("45");
											result.add("fred");
											return result;
										})//
										.addPoint((t) -> {
											List<String> result = new ArrayList<>();
											result.add("66");
											result.add("betty");
											return result;
										})//

										.build();

		Dimension dimension2 = Dimension.builder()//
										.addMetaDatum("comet")//
										.addMetaDatum("cupid")//
										.addMetaDatum("vixen")//
										.addPoint((t) -> {
											List<String> result = new ArrayList<>();
											result.add("1");
											result.add("2");
											result.add("3");
											return result;
										})//
										.addPoint((t) -> {
											List<String> result = new ArrayList<>();
											result.add("4");
											result.add("5");
											result.add("6");
											return result;
										})//
										.addPoint((t) -> {
											List<String> result = new ArrayList<>();
											result.add("7");
											result.add("8");
											result.add("9");
											return result;
										})//

										.build();

		Experiment//
					.builder()//
					.addDimension(dimension1)//
					.addDimension(dimension2)//
					.setContinueFromProgressLog(true)//
					.setExperimentProgressLog(experimentProgressPath).build()//
					.execute();//

	}
}
