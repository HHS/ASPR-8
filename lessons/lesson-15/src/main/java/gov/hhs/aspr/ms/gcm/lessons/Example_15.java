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
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.Region;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.RegionProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.VaccinePlugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.FunctionalDimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.FunctionalDimensionData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.reports.RegionPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.reports.RegionTransferReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.NIOReportItemHandler;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public final class Example_15 {

	private final Path outputDirectory;

	private Example_15(Path outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	private List<PersonId> initialPeople = new ArrayList<>();
	private List<Region> initialRegions = new ArrayList<>();
	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524055747550937602L);

	/* start code_ref= regions_plugin_example_15_nio|code_cap= The region property, region transfer and vaccination reports are mapped to distinct file names. */
	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler.builder()//
				.addReport(ModelReportLabel.REGION_PROPERTY_REPORT, //
						outputDirectory.resolve("region_property_report.xls"))//
				.addReport(ModelReportLabel.REGION_TRANSFER_REPORT, //
						outputDirectory.resolve("region_transfer_report.xls"))//
				.addReport(ModelReportLabel.VACCINATION, //
						outputDirectory.resolve("vaccine_report.xls"))//
				.build();
	}
	/* end */

	/* start code_ref= regions_plugin_example_getting_people_plugin|code_cap=The people plugin is initialized with the starting populaiton. */
	private Plugin getPeoplePlugin() {
		PeoplePluginData.Builder peoplePluginDataBuilder = PeoplePluginData.builder();
		for (PersonId personId : initialPeople) {
			peoplePluginDataBuilder.addPersonRange(new PersonRange(personId.getValue(), personId.getValue()));
		}
		PeoplePluginData peoplePluginData = peoplePluginDataBuilder.build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}
	/* end */

	/* start code_ref= regions_plugin_example_getting_regions_plugin|code_cap=The regions plugin is initialized with the starting regions and people, with each person assigned to a randomly selected region. The two region-based reports are also initialized and added to the region plugin's data.*/
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

		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Double.class)//
				.setPropertyValueMutability(false)//
				.build();
		regionsPluginDataBuilder.defineRegionProperty(RegionProperty.LAT, propertyDefinition);
		regionsPluginDataBuilder.defineRegionProperty(RegionProperty.LON, propertyDefinition);

		for (Region region : initialRegions) {
			regionsPluginDataBuilder.setRegionPropertyValue(region, RegionProperty.LAT,
					randomGenerator.nextDouble() + 45.0);
			regionsPluginDataBuilder.setRegionPropertyValue(region, RegionProperty.LON,
					randomGenerator.nextDouble() + 128.0);
		}

		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();

		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//
						.setReportLabel(ModelReportLabel.REGION_PROPERTY_REPORT)//
						.build();

		RegionTransferReportPluginData regionTransferReportPluginData = RegionTransferReportPluginData.builder()//
				.setReportLabel(ModelReportLabel.REGION_TRANSFER_REPORT)//
				.setReportPeriod(ReportPeriod.END_OF_SIMULATION)//
				.build();//

		return RegionsPlugin.builder()//
				.setRegionsPluginData(regionsPluginData)//
				.setRegionPropertyReportPluginData(regionPropertyReportPluginData)//
				.setRegionTransferReportPluginData(regionTransferReportPluginData)//
				.getRegionsPlugin();
	}
	/* end */

	/* start code_ref= regions_plugin_example_15_intialize_people_regions|code_cap=Lists of initial people and regions are created and will be used to initialize the various plugins. */
	private void initializePeopleAndRegions() {
		for (int i = 0; i < 1000; i++) {
			initialPeople.add(new PersonId(i));
		}
		for (int i = 0; i < 5; i++) {
			initialRegions.add(new Region(i));
		}
	}
	/* end */

	/* start code_ref= regions_plugin_example_15_stochastics|code_cap=The stochastics plugin is initialized with a random seed value.  A dimension is added to add new seeds to the resulting scenarios. */
	private Plugin getStochasticsPlugin() {

		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//
				.setMainRNGState(wellState).build();
		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	private Dimension getStochasticsDimension(int replicationCount, long seed) {
		FunctionalDimensionData.Builder builder = FunctionalDimensionData.builder();//

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		List<Long> seedValues = new ArrayList<>();
		for (int i = 0; i < replicationCount; i++) {
			seedValues.add(randomGenerator.nextLong());
		}

		IntStream.range(0, seedValues.size()).forEach((i) -> {
			builder.addValue("Level_" + i, (context) -> {
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

		FunctionalDimensionData functionalDimensionData = builder.build();
		return new FunctionalDimension(functionalDimensionData);
	}
	/* end */

	/* start code_ref= regions_plugin_example_15_execute|code_cap= The various plugins are gathered from their initial data.*/
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
		 * Create the region plugin 5 regions, each having a lat and lon and assign the
		 * people to random regions.
		 * 
		 */
		Plugin regionsPlugin = getRegionsPlugin();

		/*
		 * create the stochastics plugin and build a dimension with 5 seed values
		 */
		Plugin stochasticsPlugin = getStochasticsPlugin();
		Dimension stochasticsDimension = getStochasticsDimension(5, randomGenerator.nextLong());

		/*
		 * Create the vaccine and model plugins
		 */
		Plugin vaccinePlugin = VaccinePlugin.getVaccinePlugin();

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		/* end */

		/*
		 * Assemble and execute the experiment
		 */
		/* start code_ref= regions_plugin_example_15_executing_experiment |code_cap=The experiment is run with five scenarios, each using distinct random seed values.*/
		Experiment.builder()//
				.addPlugin(modelPlugin)//
				.addPlugin(regionsPlugin)//
				.addPlugin(peoplePlugin)//
				.addPlugin(stochasticsPlugin)//
				.addPlugin(vaccinePlugin)//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.addDimension(stochasticsDimension)//
				.build()//
				.execute();//
		/* end */
	}

	/* start code_ref= regions_plugin_example_15_main|code_cap=Executing example 15 with an output directory. */
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
	/* end */
}
