package lesson;

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
import nucleus.Plugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.reports.support.NIOReportItemHandler;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import util.random.RandomGeneratorProvider;

public final class Example_14 {

	private Example_14() {
	}

	private static Dimension getStochasticsDimension(int replicationCount, long seed) {
		Dimension.Builder builder = Dimension.builder();//

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		List<Long> seedValues = new ArrayList<>();
		for (int i = 0; i < replicationCount; i++) {
			seedValues.add(randomGenerator.nextLong());
		}

		IntStream.range(0, seedValues.size()).forEach((i) -> {
			builder.addLevel((context) -> {
				StochasticsPluginData.Builder stochasticsPluginDataBuilder = 
						context.get(StochasticsPluginData.Builder.class);
				long seedValue = seedValues.get(i);
				stochasticsPluginDataBuilder.setSeed(seedValue);

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

	public static void main(String[] args) {

		// reports
		

		NIOReportItemHandler nioReportItemHandler = //
				NIOReportItemHandler.builder()//
									.addReport(ModelReportLabel.POPULATION_TRACE, //
											Paths.get("C:\\temp\\gcm\\population_trace_report.xls"))//
									.addReport(ModelReportLabel.VACCINATION, //
											Paths.get("C:\\temp\\gcm\\vaccination_report.xls"))//
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
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(463390897335624435L).build();
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
