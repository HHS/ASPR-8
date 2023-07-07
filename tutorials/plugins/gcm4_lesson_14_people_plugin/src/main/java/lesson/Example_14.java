package lesson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportLabel;
import lesson.plugins.vaccine.VaccinePlugin;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.FunctionalDimension;
import nucleus.Plugin;
import plugins.people.PeoplePlugin;
import plugins.people.datamanagers.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.reports.support.NIOReportItemHandler;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datamanagers.StochasticsPluginData;
import plugins.stochastics.support.WellState;
import util.random.RandomGeneratorProvider;

public final class Example_14 {

	private Example_14() {
	}

	private static Dimension getStochasticsDimension(int replicationCount, long seed) {
		FunctionalDimension.Builder builder = FunctionalDimension.builder();//

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		List<Long> seedValues = new ArrayList<>();
		for (int i = 0; i < replicationCount; i++) {
			seedValues.add(randomGenerator.nextLong());
		}

		IntStream.range(0, seedValues.size()).forEach((i) -> {
			builder.addLevel((context) -> {
				StochasticsPluginData.Builder stochasticsPluginDataBuilder = 
						context.getPluginDataBuilder(StochasticsPluginData.Builder.class);
				long seedValue = seedValues.get(i);
				WellState wellState = WellState.builder().setSeed(seedValue).build();
				stochasticsPluginDataBuilder.setMainRNGState(wellState);

				ArrayList<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				result.add(Long.toString(seedValue) + "L");

				return result;
			});//
		});

		builder.addMetaDatum("seed index");//
		builder.addMetaDatum("seed value");//

		return builder.build();
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			throw new RuntimeException("One output directory argument is required");
		}
		Path outputDirectory = Paths.get(args[0]);
		if (!Files.exists(outputDirectory)) {
			Files.createDirectory(outputDirectory);
		} else {
			if (!Files.isDirectory(outputDirectory)) {
				throw new IOException("Provided path is not a directory");
			}
		}

		// reports
		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
									.addReport(ModelReportLabel.POPULATION_TRACE, //
											outputDirectory.resolve("population_trace_report.xls"))//
									.addReport(ModelReportLabel.VACCINATION, //
											outputDirectory.resolve("vaccination_report.xls"))//
									.build();

		// create the people plugin with an initial population of ten people,
		// numbered 1, 3, 5,...,19
		PeoplePluginData.Builder peoplePluginDataBuilder = PeoplePluginData.builder();
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i * 2 + 1);
			peoplePluginDataBuilder.addPersonRange(new PersonRange(personId.getValue(), personId.getValue()));
		}
		PeoplePluginData peoplePluginData = peoplePluginDataBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		// create the stochastics plugin and build a dimension with 5 seed
		// values
		WellState wellState = WellState.builder().setSeed(463390897335624435L).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setMainRNGState(wellState).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		Dimension stochasticsDimension = getStochasticsDimension(5, 8265427588292179209L);

		// create the vaccine and model plugins
		Plugin vaccinePlugin = VaccinePlugin.getVaccinePlugin();
		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		Experiment	.builder()//
					.addPlugin(modelPlugin)//
					.addPlugin(peoplePlugin)//
					.addPlugin(stochasticsPlugin)//
					.addPlugin(vaccinePlugin)//
					.addExperimentContextConsumer(nioReportItemHandler)//
					.addDimension(stochasticsDimension)//
					.build()//
					.execute();//
	}

}
