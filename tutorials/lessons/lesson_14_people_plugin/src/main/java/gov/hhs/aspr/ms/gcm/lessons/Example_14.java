package gov.hhs.aspr.ms.gcm.lessons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelReportLabel;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.VaccinePlugin;
import gov.hhs.aspr.ms.gcm.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.nucleus.FunctionalDimension;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.NIOReportItemHandler;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import util.random.RandomGeneratorProvider;

public final class Example_14 {

	private Example_14() {
	}

	/* start code_ref= people_plugin_stochastics_dimension|code_cap=The stochastics dimension contains levels for each replication value. Note that the generation of the random seed values occurs outside of the lambda code.*/
	private static Dimension getStochasticsDimension(int replicationCount, long seed) {
		FunctionalDimension.Builder builder = FunctionalDimension.builder();//

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		List<Long> seedValues = new ArrayList<>();
		for (int i = 0; i < replicationCount; i++) {
			seedValues.add(randomGenerator.nextLong());
		}

		IntStream.range(0, seedValues.size()).forEach((i) -> {
			builder.addLevel((context) -> {
				StochasticsPluginData.Builder stochasticsPluginDataBuilder = context
						.getPluginDataBuilder(StochasticsPluginData.Builder.class);
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
	/* end */

	/* start code_ref= people_plugin_example_14_init|code_cap=The population trace and vaccination reports are associated with corresponding file names via the NIO report item handler.*/
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

		/* end */
		/* start code_ref= people_plugin_example_14_adding_plugins|code_cap=The various plugins are initialized with data and added to the experiment.*/

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
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setMainRNGState(wellState)
				.build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		Dimension stochasticsDimension = getStochasticsDimension(5, 8265427588292179209L);

		// create the vaccine and model plugins
		Plugin vaccinePlugin = VaccinePlugin.getVaccinePlugin();
		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		Experiment.builder()//
				.addPlugin(modelPlugin)//
				.addPlugin(peoplePlugin)//
				.addPlugin(stochasticsPlugin)//
				.addPlugin(vaccinePlugin)//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.addDimension(stochasticsDimension)//
				.build()//
				.execute();//
	}
	/* end */

}
