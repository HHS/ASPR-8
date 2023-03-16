package lesson;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.core.TranslatorController;
import gov.hhs.aspr.gcm.translation.plugins.globalproperties.GlobalPropertiesTranslator;
import gov.hhs.aspr.gcm.translation.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.plugins.personproperties.PersonPropertiesTranslator;
import gov.hhs.aspr.gcm.translation.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.plugins.stochastics.StochasticsTranslator;
import gov.hss.aspr.gcm.translation.nucleus.NucleusTranslator;
import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportLabel;
import lesson.plugins.model.PersonProperty;
import lesson.plugins.model.Region;
import lesson.plugins.model.reports.VaccineReport;
import lesson.translatorSpecs.GlobalPropertyTranslatorSpec;
import lesson.translatorSpecs.PersonPropertyTranslatorSpec;
import lesson.translatorSpecs.RegionTranslatorSpec;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.ExperimentContext;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.SimulationStateCollector;
import nucleus.SimulationTime;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginData.Builder;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.reports.PersonPropertyReport;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class SerializationDemonstration {

	private final String personPropertiesFileName = "personPropertiesOutput.json";
	private final String globalPropertiesFileName = "globalPropertiesOutput.json";
	private final String regionsFileName = "regionsOutput.json";
	private final String peopleFileName = "peopleOutput.json";
	private final String stochasticsFileName = "stochasticsOutput.json";
	private final String simTimeFileName = "simTimeOutput.json";

	private final Path outputDirectory;
	private TranslatorController writingTranslatorController;
	private TranslatorController readingTranslatorController;

	private SerializationDemonstration(Path outputDirectory) {
		this.outputDirectory = outputDirectory;

		this.readingTranslatorController = TranslatorController
				.builder()
				.addTranslator(
						PersonPropertiesTranslator.getTranslatorR("./src/main/resources/personPropertiesInput.json"))
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslatorR("./src/main/resources/peopleInput.json"))
				.addTranslator(RegionsTranslator.getTranslatorR("./src/main/resources/regionsInput.json"))
				.addTranslator(
						GlobalPropertiesTranslator.getTranslatorR("./src/main/resources/globalPropertiesInput.json"))
				.addTranslatorSpec(new PersonPropertyTranslatorSpec())
				.addTranslatorSpec(new GlobalPropertyTranslatorSpec())
				.addTranslatorSpec(new RegionTranslatorSpec())
				.build().init();
	}

	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524055747550937602L);

	private Plugin getReportsPlugin() {
		ReportsPluginData reportsPluginData = //
				ReportsPluginData.builder()//
						.addReport(() -> {
							PersonPropertyReportPluginData personPropertyReportPluginData = PersonPropertyReportPluginData
									.builder()//
									.setReportLabel(ModelReportLabel.PERSON_PROPERTY_REPORT)//
									.setReportPeriod(ReportPeriod.END_OF_SIMULATION)//
									.setDefaultInclusion(true)//
									.build();//
							return new PersonPropertyReport(personPropertyReportPluginData)::init;
						})//
						.addReport(() -> {
							return new VaccineReport(ModelReportLabel.VACCINATION)::init;
						})//
						.build();

		return ReportsPlugin.getReportsPlugin(reportsPluginData);
	}

	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler.builder()//
				.addReport(ModelReportLabel.PERSON_PROPERTY_REPORT, //
						outputDirectory.resolve("person_property_report.xls"))//
				.addReport(ModelReportLabel.VACCINATION, //
						outputDirectory.resolve("vaccination_report.xls"))//
				.build();
	}

	private Plugin getPeoplePlugin() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	private Plugin getRegionsPlugin() {
		RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();

		for (int i = 0; i < 5; i++) {
			regionsPluginDataBuilder.addRegion(new Region(i));
		}
		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		return RegionsPlugin.getRegionsPlugin(regionsPluginData);
	}

	private Plugin getPersonPropertiesPlugin() {
		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.setDefaultValue(0)//
				.build();
		builder.definePersonProperty(PersonProperty.EDUCATION_ATTEMPTS, propertyDefinition);
		builder.definePersonProperty(PersonProperty.VACCINE_ATTEMPTS, propertyDefinition);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Boolean.class)//
				.build();
		builder.definePersonProperty(PersonProperty.REFUSES_VACCINE, propertyDefinition);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Boolean.class)//
				.setDefaultValue(false)//
				.build();
		builder.definePersonProperty(PersonProperty.VACCINATED, propertyDefinition);

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();
		return PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);
	}

	private Plugin getStochasticsPlugin() {
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//

				.setSeed(randomGenerator.nextLong())//
				.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	private Dimension getGlobalPropertyDimension(GlobalPropertyId globalPropertyId, String header, double[] values) {
		Dimension.Builder dimensionBuilder = Dimension.builder();//
		IntStream.range(0, values.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context.get(GlobalPropertiesPluginData.Builder.class);
				double value = values[i];
				builder.setGlobalPropertyValue(globalPropertyId, value);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(value));
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum(header);//
		return dimensionBuilder.build();
	}

	private Dimension getVaccineRefusalProbabilityDimension() {
		double[] values = new double[] { 0.0, 0.25, 0.5, 0.75, 1.0 };
		return getGlobalPropertyDimension(GlobalProperty.VACCINE_REFUSAL_PROBABILITY, "intial_refusal_probability",
				values);
	}

	private Dimension getImmunityStartTimeDimension() {
		double[] values = new double[] { 120.0, 180.0 };
		return getGlobalPropertyDimension(GlobalProperty.IMMUNITY_START_TIME, "immunity_start_time", values);
	}

	private Dimension getImmunityProbabilityDimension() {
		double[] values = new double[] { 0.0, 0.1, 0.2, 0.5 };
		return getGlobalPropertyDimension(GlobalProperty.IMMUNITY_PROBABILITY, "immunity_probabilty", values);
	}

	private Dimension getVaccineAttemptIntervalDimension() {
		double[] values = new double[] { 30.0, 45.0, 60.0 };

		return getGlobalPropertyDimension(GlobalProperty.VACCINE_ATTEMPT_INTERVAL, "vaccine_atttempt_interval", values);
	}

	private Dimension getEducationAttemptIntervalDimension() {
		double[] values = new double[] { 30.0, 60.0, 180.0 };
		return getGlobalPropertyDimension(GlobalProperty.EDUCATION_ATTEMPT_INTERVAL, "education_attempt_interval",
				values);
	}

	private Dimension getEducationSuccessRatedimension() {
		double[] values = new double[] { 0.0, 0.1, 0.2 };
		return getGlobalPropertyDimension(GlobalProperty.EDUCATION_SUCCESS_RATE, "education_success_rate", values);
	}

	private Plugin getGlobalPropertiesPlugin() {
		Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Double.class)//
				.setDefaultValue(0.0)//
				.setPropertyValueMutability(false)//
				.build();

		builder.defineGlobalProperty(GlobalProperty.IMMUNITY_START_TIME, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.VACCINE_ATTEMPT_INTERVAL, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.EDUCATION_ATTEMPT_INTERVAL, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.EDUCATION_SUCCESS_RATE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.VACCINE_REFUSAL_PROBABILITY, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.IMMUNITY_PROBABILITY, propertyDefinition);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Double.class)//
				.setDefaultValue(365.0)//
				.setPropertyValueMutability(false)//
				.build();
		builder.defineGlobalProperty(GlobalProperty.SIMULATION_DURATION, propertyDefinition);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.setDefaultValue(1000)//
				.setPropertyValueMutability(false)//
				.build();

		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition);

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.getGlobalPropertiesPlugin(globalPropertiesPluginData);
	}

	private void execute() {
		/*
		 * Create the people plugin filled with 1000 people
		 */
		Plugin peoplePlugin = getPeoplePlugin();

		// Create the person properties plugin
		Plugin personPropertiesPlugin = getPersonPropertiesPlugin();

		/*
		 * Create the region plugin 5 regions, each having a lat and lon and
		 * assign the people to random regions.
		 * 
		 */
		Plugin regionsPlugin = getRegionsPlugin();

		/*
		 * Create the global properties plugin
		 */
		Plugin globalPropertiesPlugin = getGlobalPropertiesPlugin();

		/*
		 * create the stochastics plugin
		 */
		Plugin stochasticsPlugin = getStochasticsPlugin();

		List<PluginData> pluginDatas = this.readingTranslatorController.readInput().getPluginDatas();

		for (PluginData pluginData : pluginDatas) {
			if (pluginData instanceof PeoplePluginData) {
				peoplePlugin = PeoplePlugin.getPeoplePlugin((PeoplePluginData) pluginData);
				continue;
			}
			if (pluginData instanceof PersonPropertiesPluginData) {
				personPropertiesPlugin = PersonPropertiesPlugin
						.getPersonPropertyPlugin((PersonPropertiesPluginData) pluginData);
				continue;
			}

			if (pluginData instanceof RegionsPluginData) {
				regionsPlugin = RegionsPlugin.getRegionsPlugin((RegionsPluginData) pluginData);
				continue;
			}

			if (pluginData instanceof GlobalPropertiesPluginData) {
				globalPropertiesPlugin = GlobalPropertiesPlugin
						.getGlobalPropertiesPlugin((GlobalPropertiesPluginData) pluginData);
				continue;
			}

			if (pluginData instanceof StochasticsPluginData) {
				stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin((StochasticsPluginData) pluginData);
				continue;
			}

		}

		/*
		 * Create the reports
		 */
		Plugin reportsPlugin = getReportsPlugin();
		NIOReportItemHandler nioReportItemHandler = getNIOReportItemHandler();

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		/*
		 * Assemble and execute the experiment
		 */

		Experiment.builder()//

				.addPlugin(personPropertiesPlugin)//
				.addPlugin(globalPropertiesPlugin)//
				.addPlugin(modelPlugin)//
				.addPlugin(regionsPlugin)//
				.addPlugin(peoplePlugin)//
				.addPlugin(stochasticsPlugin)//
				.addPlugin(reportsPlugin)//

				.addDimension(getImmunityStartTimeDimension())//
				.addDimension(getImmunityProbabilityDimension())//
				// .addDimension(getVaccineAttemptIntervalDimension())//
				// .addDimension(getEducationAttemptIntervalDimension())//
				// .addDimension(getEducationSuccessRatedimension())//
				// .addDimension(getVaccineRefusalProbabilityDimension())//
				.addExperimentContextConsumer(nioReportItemHandler)//

				.setProduceSimulationStateOnHalt(true)//
				.addExperimentContextConsumer(new SimulationStateCollector(this::handleSimulationStateCollection,
						this::handleExperiementOpen))

				.setThreadCount(8)//
				.build()//
				.execute();//

	}

	private void handleExperiementOpen(ExperimentContext experimentContext) {

		TranslatorController.Builder translatorControllerBuilder = TranslatorController.builder();

		Translator.Builder personPropertiesTranslator = PersonPropertiesTranslator.builder();
		Translator.Builder globalPropertiesTranslator = GlobalPropertiesTranslator.builder();
		Translator.Builder regionsTranslator = RegionsTranslator.builder();
		Translator.Builder peopleTranslator = PeopleTranslator.builder();
		Translator.Builder stochasticsTranslator = StochasticsTranslator.builder();
		Translator.Builder nucleusTranslator = NucleusTranslator.builder();

		for (int i = 0; i < experimentContext.getScenarioCount(); i++) {
			File outputDir = this.outputDirectory.resolve("scenario" + i).toFile();
			outputDir.mkdir();

			Path personPropertiesPath = Paths.get(outputDir.getAbsolutePath()).resolve(personPropertiesFileName);
			Path globalPropertiesPath = Paths.get(outputDir.getAbsolutePath()).resolve(globalPropertiesFileName);
			Path regionsPath = Paths.get(outputDir.getAbsolutePath()).resolve(regionsFileName);
			Path peoplePath = Paths.get(outputDir.getAbsolutePath()).resolve(peopleFileName);
			Path stochasticsPath = Paths.get(outputDir.getAbsolutePath()).resolve(stochasticsFileName);
			Path simTimePath = Paths.get(outputDir.getAbsolutePath()).resolve(simTimeFileName);

			personPropertiesTranslator
					.addOutputFile(personPropertiesPath.toString(), PersonPropertiesPluginData.class, i);
			globalPropertiesTranslator
					.addOutputFile(globalPropertiesPath.toString(), GlobalPropertiesPluginData.class, i);
			regionsTranslator.addOutputFile(regionsPath.toString(), RegionsPluginData.class, i);
			peopleTranslator.addOutputFile(peoplePath.toString(), PeoplePluginData.class, i);
			stochasticsTranslator.addOutputFile(stochasticsPath.toString(), StochasticsPluginData.class, i);
			nucleusTranslator.addOutputFile(simTimePath.toString(), SimulationTime.class, i);

		}

		translatorControllerBuilder
				.addTranslator(personPropertiesTranslator.build())
				.addTranslator(globalPropertiesTranslator.build())
				.addTranslator(regionsTranslator.build())
				.addTranslator(peopleTranslator.build())
				.addTranslator(stochasticsTranslator.build())
				.addTranslator(nucleusTranslator.build())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslatorSpec(new PersonPropertyTranslatorSpec())
				.addTranslatorSpec(new GlobalPropertyTranslatorSpec())
				.addTranslatorSpec(new RegionTranslatorSpec());

		this.writingTranslatorController = translatorControllerBuilder.build();

		this.writingTranslatorController.init();
	}

	private void handleSimulationStateCollection(Integer scenarioId, List<Object> output) {

		System.out.println(scenarioId);

		for (Object object : output) {
			if (object instanceof PluginData) {
				writingTranslatorController.writePluginDataOutput((PluginData) object, scenarioId);
			}
			if (object instanceof SimulationTime) {
				writingTranslatorController.writeObjectOutput(object, scenarioId);
			}
		}

	}

	public static void main(String[] args) {
		Path outputDirectory = Paths.get(args[0]);

		new SerializationDemonstration(outputDirectory).execute();
	}

}
