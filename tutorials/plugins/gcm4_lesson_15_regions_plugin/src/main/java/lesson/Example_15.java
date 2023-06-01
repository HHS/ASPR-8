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
import lesson.plugins.model.Region;
import lesson.plugins.model.RegionProperty;
import lesson.plugins.vaccine.VaccinePlugin;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.FunctionalDimension;
import nucleus.Plugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.reports.RegionPropertyReportPluginData;
import plugins.regions.reports.RegionTransferReportPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.WellState;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class Example_15 {

	private final Path outputDirectory;

	private Example_15(Path outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	private List<PersonId> initialPeople = new ArrayList<>();
	private List<Region> initialRegions = new ArrayList<>();
	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524055747550937602L);


	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler	.builder()//
									.addReport(ModelReportLabel.REGION_PROPERTY_REPORT, //
											outputDirectory.resolve("region_property_report.xls"))//
									.addReport(ModelReportLabel.REGION_TRANSFER_REPORT, //
											outputDirectory.resolve("region_transfer_report.xls"))//
									.addReport(ModelReportLabel.VACCINATION, //
											outputDirectory.resolve("vaccine_report.xls"))//
									.build();
	}

	private Plugin getPeoplePlugin() {
		PeoplePluginData.Builder peoplePluginDataBuilder = PeoplePluginData.builder();
		for (PersonId personId : initialPeople) {
			peoplePluginDataBuilder.addPersonRange(new PersonRange(personId.getValue(),personId.getValue()));
		}
		PeoplePluginData peoplePluginData = peoplePluginDataBuilder.build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	private Plugin getRegionsPlugin() {
		// create the region plugin with an initial five regions, each region
		// having 200 people
		RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();
		for (Region region : initialRegions) {
			regionsPluginDataBuilder.addRegion(region);
		}

		for (PersonId personId : initialPeople) {
			Region region = initialRegions.get(randomGenerator.nextInt(initialRegions.size()));
			regionsPluginDataBuilder.addPerson(personId, region);
		}

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setPropertyValueMutability(false)//
																	.build();
		regionsPluginDataBuilder.defineRegionProperty(RegionProperty.LAT, propertyDefinition);
		regionsPluginDataBuilder.defineRegionProperty(RegionProperty.LON, propertyDefinition);

		for (Region region : initialRegions) {
			regionsPluginDataBuilder.setRegionPropertyValue(region, RegionProperty.LAT, randomGenerator.nextDouble() + 45.0);
			regionsPluginDataBuilder.setRegionPropertyValue(region, RegionProperty.LON, randomGenerator.nextDouble() + 128.0);
		}

		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
			
		
		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//
				.setReportLabel(ModelReportLabel.REGION_PROPERTY_REPORT)//
				.build();
		
		RegionTransferReportPluginData regionTransferReportPluginData = RegionTransferReportPluginData	.builder()//
		.setReportLabel(ModelReportLabel.REGION_TRANSFER_REPORT)//
		.setReportPeriod(ReportPeriod.END_OF_SIMULATION)//
		.build();//
		
		
		return RegionsPlugin.builder()//
				.setRegionsPluginData(regionsPluginData)//
				.setRegionPropertyReportPluginData(regionPropertyReportPluginData)//
				.setRegionTransferReportPluginData(regionTransferReportPluginData)//
				.getRegionsPlugin();
	}

	private void initializePeopleAndRegions() {
		for (int i = 0; i < 1000; i++) {
			initialPeople.add(new PersonId(i));
		}
		for (int i = 0; i < 5; i++) {
			initialRegions.add(new Region(i));
		}
	}

	private Plugin getStochasticsPlugin() {
		
		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//
																			.setMainRNGState(wellState).build();
		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	private Dimension getStochasticsDimension(int replicationCount, long seed) {
		FunctionalDimension.Builder builder = FunctionalDimension.builder();//

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		List<Long> seedValues = new ArrayList<>();
		for (int i = 0; i < replicationCount; i++) {
			seedValues.add(randomGenerator.nextLong());
		}

		IntStream.range(0, seedValues.size()).forEach((i) -> {
			builder.addLevel((context) -> {
				StochasticsPluginData.Builder stochasticsPluginDataBuilder = context.get(StochasticsPluginData.Builder.class);
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

	private void execute() {
		/*
		 * Create person ids and region ids that are shared across the plugins
		 */
		initializePeopleAndRegions();

		/*
		 * Create the reports
		 */
		
		NIOReportItemHandler nioReportItemHandler = getNIOReportItemHandler();

		/*
		 * Create the people plugin filled with 1000 people
		 */
		Plugin peoplePlugin = getPeoplePlugin();

		/*
		 * Create the region plugin 5 regions, each having a lat and lon and
		 * assign the people to random regions.
		 * 
		 */
		Plugin regionsPlugin = getRegionsPlugin();

		/*
		 * create the stochastics plugin and build a dimension with 5 seed
		 * values
		 */
		Plugin stochasticsPlugin = getStochasticsPlugin();
		Dimension stochasticsDimension = getStochasticsDimension(5, randomGenerator.nextLong());

		/*
		 * Create the vaccine and model plugins
		 */
		Plugin vaccinePlugin = VaccinePlugin.getVaccinePlugin();

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		/*
		 * Assemble and execute the experiment
		 */
		Experiment	.builder()//
					.addPlugin(modelPlugin)//
					.addPlugin(regionsPlugin)//
					.addPlugin(peoplePlugin)//
					.addPlugin(stochasticsPlugin)//
					.addPlugin(vaccinePlugin)//					
					.addExperimentContextConsumer(nioReportItemHandler)//
					.addDimension(stochasticsDimension)//
					.build()//
					.execute();//

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

		new Example_15(outputDirectory).execute();
	}

}
