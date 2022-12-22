package lesson;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportId;
import lesson.plugins.model.Region;
import lesson.plugins.model.RegionProperty;
import lesson.plugins.vaccine.VaccinePlugin;
import lesson.plugins.vaccine.VaccineReport;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.actors.RegionPropertyReport;
import plugins.regions.actors.RegionTransferReport;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class Example_15 {

	private Example_15() {
	}

	private List<PersonId> initialPeople = new ArrayList<>();
	private List<Region> initialRegions = new ArrayList<>();
	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524055747550937602L);

	

	private Plugin getReportsPlugin() {
		ReportsPluginData reportsPluginData = //
				ReportsPluginData	.builder()//
									.addReport(() -> {
										return new RegionPropertyReport(
												ModelReportId.REGION_PROPERTY_REPORT)//
												::init;
									})//
									.addReport(() -> {
										return new RegionTransferReport(
												ModelReportId.REGION_TRANSFER_REPORT,//
												ReportPeriod.END_OF_SIMULATION)//
												::init;
									})//
									.addReport(() -> {
										return new VaccineReport(
												ModelReportId.VACCINATION,//
												ReportPeriod.END_OF_SIMULATION,//
												6)::init;
									})//
									.build();

		return ReportsPlugin.getReportsPlugin(reportsPluginData);
	}

	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler	.builder()//
									.addReport(ModelReportId.REGION_PROPERTY_REPORT, //
											Paths.get("C:\\temp\\gcm\\region_property_report.xls"))//
									.addReport(ModelReportId.REGION_TRANSFER_REPORT, //
											Paths.get("C:\\temp\\gcm\\region_transfer_report.xls"))//
									.addReport(ModelReportId.VACCINATION, //
											Paths.get("C:\\temp\\gcm\\vaccine_report.xls"))//
									.build();
	}

	private Plugin getPeoplePlugin() {
		PeoplePluginData.Builder peoplePluginDataBuilder = PeoplePluginData.builder();
		for (PersonId personId : initialPeople) {
			peoplePluginDataBuilder.addPersonId(personId);
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
			regionsPluginDataBuilder.setPersonRegion(personId, region);
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
		return RegionsPlugin.getRegionsPlugin(regionsPluginData);
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
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//
				.setSeed(randomGenerator.nextLong()).build();
		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}
	
	private Dimension getStochasticsDimension(int replicationCount, long seed) {
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

	private void execute() {
		/*
		 * Create person ids and region ids that are shared across the plugins
		 */
		initializePeopleAndRegions();

		/*
		 * Create the reports
		 */
		Plugin reportsPlugin = getReportsPlugin();
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
					.addPlugin(reportsPlugin)//
					.addExperimentContextConsumer(nioReportItemHandler)//
					.addDimension(stochasticsDimension)//
					.build()//
					.execute();//

	}

	public static void main(String[] args) {
		new Example_15().execute();
	}

}
