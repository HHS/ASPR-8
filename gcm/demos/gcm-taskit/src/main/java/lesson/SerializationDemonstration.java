package lesson;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.gcm.translation.protobuf.nucleus.NucleusTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.GlobalPropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.PersonPropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.stochastics.StochasticsTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportLabel;
import lesson.plugins.model.PersonProperty;
import lesson.plugins.model.Region;
import lesson.translatorSpecs.GlobalPropertyTranslatorSpec;
import lesson.translatorSpecs.PersonPropertyTranslatorSpec;
import lesson.translatorSpecs.RegionTranslatorSpec;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.ExperimentContext;
import nucleus.ExperimentParameterData;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.SimulationState;
import nucleus.SimulationStateCollector;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import plugins.globalproperties.support.GlobalPropertyDimension;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.people.PeoplePlugin;
import plugins.people.datamanagers.PeoplePluginData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.datamanagers.RegionsPluginData;
import plugins.reports.ReportsPlugin;
import plugins.reports.support.NIOReportItemHandler;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datamanagers.StochasticsPluginData;
import plugins.stochastics.support.WellState;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;
import util.time.Stopwatch;

public final class SerializationDemonstration {

	private final String personPropertiesFileName = "personPropertiesOutput.json";
	private final String globalPropertiesFileName = "globalPropertiesOutput.json";
	private final String regionsFileName = "regionsOutput.json";
	private final String peopleFileName = "peopleOutput.json";
	private final String stochasticsFileName = "stochasticsOutput.json";
	private final String simStateFileName = "simStateOutput.json";

	private final Path outputDirectory;
	private TranslationController writingTranslationController;
	private TranslationController readingTranslationController;

	private SerializationDemonstration(Path outputDirectory) {
		this.outputDirectory = outputDirectory;

		this.readingTranslationController = TranslationController
				.builder()
				.addTranslator(
						PersonPropertiesTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(
						GlobalPropertiesTranslator.getTranslator())
				.addTranslator(ReportsTranslator.getTranslator())
				.setTranslationEngineBuilder(ProtobufTranslationEngine.builder()
						.addTranslationSpec(new PersonPropertyTranslatorSpec())
						.addTranslationSpec(new GlobalPropertyTranslatorSpec())
						.addTranslationSpec(new RegionTranslatorSpec()))
				.addInputFilePath(Paths.get("./src/main/resources/peopleInput.json"), PeoplePluginDataInput.class)
				.addInputFilePath(Paths.get("./src/main/resources/regionsInput.json"), RegionsPluginDataInput.class)
				.addInputFilePath(Paths.get("./src/main/resources/globalPropertiesInput.json"),
						GlobalPropertiesPluginDataInput.class)
				.build();
	}

	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524055747550937602L);

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
		return RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();
	}

	private Plugin getPersonPropertiesPlugin() {
		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.setDefaultValue(0)//
				.build();
		builder.definePersonProperty(PersonProperty.EDUCATION_ATTEMPTS,
				propertyDefinition, 0.0, false);
		// builder.definePersonProperty(PersonProperty.VACCINE_ATTEMPTS,
		// propertyDefinition);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Boolean.class)//
				.build();
		builder.definePersonProperty(PersonProperty.REFUSES_VACCINE,
				propertyDefinition, 0.0, false);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Boolean.class)//
				.setDefaultValue(false)//
				.build();
		builder.definePersonProperty(PersonProperty.VACCINATED, propertyDefinition, 0.0, false);

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();
		return PersonPropertiesPlugin.builder().setPersonPropertiesPluginData(personPropertiesPluginData)
				.getPersonPropertyPlugin();
	}

	private Plugin getStochasticsPlugin() {
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//

				.setMainRNGState(WellState.builder().setSeed(randomGenerator.nextLong()).build())//
				.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	private Dimension getGlobalPropertyDimension(GlobalPropertyId globalPropertyId, String header, double[] values) {
		GlobalPropertyDimension.Builder dimensionBuilder = GlobalPropertyDimension.builder();//

		dimensionBuilder.setGlobalPropertyId(globalPropertyId)
				.setAssignmentTime(0.0);

		for (Double val : values) {
			dimensionBuilder.addValue(val);
		}
		return dimensionBuilder.build();
	}

	private Dimension getVaccineRefusalProbabilityDimension() {
		double[] values = new double[] { 0.0, 0.25, 0.5, 0.75, 1.0 };
		return getGlobalPropertyDimension(GlobalProperty.VACCINE_REFUSAL_PROBABILITY,
				"intial_refusal_probability",
				values);
	}

	private Dimension getImmunityStartTimeDimension() {
		double[] values = new double[] { 120.0, 180.0 };
		return getGlobalPropertyDimension(GlobalProperty.IMMUNITY_START_TIME,
				"immunity_start_time", values);
	}

	private Dimension getImmunityProbabilityDimension() {
		double[] values = new double[] { 0.0, 0.1, 0.2, 0.5 };
		return getGlobalPropertyDimension(GlobalProperty.IMMUNITY_PROBABILITY,
				"immunity_probabilty", values);
	}

	private Dimension getVaccineAttemptIntervalDimension() {
		double[] values = new double[] { 30.0, 45.0, 60.0 };

		return getGlobalPropertyDimension(GlobalProperty.VACCINE_ATTEMPT_INTERVAL,
				"vaccine_atttempt_interval", values);
	}

	private Dimension getEducationAttemptIntervalDimension() {
		double[] values = new double[] { 30.0, 60.0, 180.0 };
		return getGlobalPropertyDimension(GlobalProperty.EDUCATION_ATTEMPT_INTERVAL,
				"education_attempt_interval",
				values);
	}

	private Dimension getEducationSuccessRatedimension() {
		double[] values = new double[] { 0.0, 0.1, 0.2 };
		return getGlobalPropertyDimension(GlobalProperty.EDUCATION_SUCCESS_RATE,
				"education_success_rate", values);
	}

	private Plugin getGlobalPropertiesPlugin() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Double.class)//
				.setDefaultValue(0.0)//
				.setPropertyValueMutability(false)//
				.build();

		builder.defineGlobalProperty(GlobalProperty.IMMUNITY_START_TIME,
				propertyDefinition, 0.0);
		builder.defineGlobalProperty(GlobalProperty.VACCINE_ATTEMPT_INTERVAL,
				propertyDefinition, 0.0);
		builder.defineGlobalProperty(GlobalProperty.EDUCATION_ATTEMPT_INTERVAL,
				propertyDefinition, 0.0);
		builder.defineGlobalProperty(GlobalProperty.EDUCATION_SUCCESS_RATE,
				propertyDefinition, 0.0);
		builder.defineGlobalProperty(GlobalProperty.VACCINE_REFUSAL_PROBABILITY,
				propertyDefinition, 0.0);
		builder.defineGlobalProperty(GlobalProperty.IMMUNITY_PROBABILITY,
				propertyDefinition, 0.0);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Double.class)//
				.setDefaultValue(365.0)//
				.setPropertyValueMutability(false)//
				.build();
		builder.defineGlobalProperty(GlobalProperty.SIMULATION_DURATION,
				propertyDefinition, 0.0);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.setDefaultValue(1000)//
				.setPropertyValueMutability(false)//
				.build();

		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE,
				propertyDefinition, 0.0);

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.builder().setGlobalPropertiesPluginData(globalPropertiesPluginData)
				.getGlobalPropertiesPlugin();
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

		Stopwatch stopwatch = new Stopwatch();

		stopwatch.start();

		this.readingTranslationController.readInput();

		stopwatch.stop();
		System.out.println(stopwatch.getElapsedMilliSeconds());

		List<PluginData> pluginDatas = this.readingTranslationController.getObjects(PluginData.class);

		for (PluginData pluginData : pluginDatas) {
			if (pluginData instanceof PeoplePluginData) {
				peoplePlugin = PeoplePlugin.getPeoplePlugin((PeoplePluginData) pluginData);
				continue;
			}
			if (pluginData instanceof PersonPropertiesPluginData) {
				personPropertiesPlugin = PersonPropertiesPlugin.builder()
						.setPersonPropertiesPluginData((PersonPropertiesPluginData) pluginData)
						.getPersonPropertyPlugin();
				continue;
			}

			if (pluginData instanceof RegionsPluginData) {
				regionsPlugin = RegionsPlugin.builder().setRegionsPluginData((RegionsPluginData) pluginData)
						.getRegionsPlugin();
				continue;
			}

			if (pluginData instanceof GlobalPropertiesPluginData) {
				globalPropertiesPlugin = GlobalPropertiesPlugin.builder()
						.setGlobalPropertiesPluginData((GlobalPropertiesPluginData) pluginData)
						.getGlobalPropertiesPlugin();
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
				.addPlugin(ReportsPlugin.getReportsPlugin())

				.addDimension(getImmunityStartTimeDimension())//
				.addDimension(getImmunityProbabilityDimension())//
				.addDimension(getVaccineAttemptIntervalDimension())//
				.addDimension(getEducationAttemptIntervalDimension())//
				.addDimension(getEducationSuccessRatedimension())//
				.addDimension(getVaccineRefusalProbabilityDimension())//
				.addExperimentContextConsumer(nioReportItemHandler)//
				.setExperimentParameterData(ExperimentParameterData.builder()
						.setRecordState(true)
						.setSimulationHaltTime(10.0)
						.setThreadCount(8)
						.build())
				.addExperimentContextConsumer(new SimulationStateCollector(this::handleSimulationStateCollection,
						this::handleExperiementOpen))
				.build()//
				.execute();//

	}

	private void handleExperiementOpen(ExperimentContext experimentContext) {

		TranslationController.Builder translationControllerBuilder = TranslationController.builder();

		Translator personPropertiesTranslator = PersonPropertiesTranslator.getTranslator();
		Translator globalPropertiesTranslator = GlobalPropertiesTranslator.getTranslator();
		Translator regionsTranslator = RegionsTranslator.getTranslator();
		Translator peopleTranslator = PeopleTranslator.getTranslator();
		Translator stochasticsTranslator = StochasticsTranslator.getTranslator();
		Translator nucleusTranslator = NucleusTranslator.getTranslator();

		for (int i = 0; i < experimentContext.getScenarioCount(); i++) {
			File outputDir = this.outputDirectory.resolve("scenario" + i).toFile();
			outputDir.mkdir();

			Path personPropertiesPath = Paths.get(outputDir.getAbsolutePath()).resolve(personPropertiesFileName);
			Path globalPropertiesPath = Paths.get(outputDir.getAbsolutePath()).resolve(globalPropertiesFileName);
			Path regionsPath = Paths.get(outputDir.getAbsolutePath()).resolve(regionsFileName);
			Path peoplePath = Paths.get(outputDir.getAbsolutePath()).resolve(peopleFileName);
			Path stochasticsPath = Paths.get(outputDir.getAbsolutePath()).resolve(stochasticsFileName);
			Path simStatepath = Paths.get(outputDir.getAbsolutePath()).resolve(simStateFileName);

			translationControllerBuilder
					.addOutputFilePath(personPropertiesPath, PersonPropertiesPluginData.class, i)
					.addOutputFilePath(globalPropertiesPath, GlobalPropertiesPluginData.class, i)
					.addOutputFilePath(regionsPath, RegionsPluginData.class, i)
					.addOutputFilePath(peoplePath, PeoplePluginData.class, i)
					.addOutputFilePath(stochasticsPath, StochasticsPluginData.class, i)
					.addOutputFilePath(simStatepath, SimulationState.class,
							i);

		}

		translationControllerBuilder
				.addTranslator(personPropertiesTranslator)
				.addTranslator(globalPropertiesTranslator)
				.addTranslator(regionsTranslator)
				.addTranslator(peopleTranslator)
				.addTranslator(stochasticsTranslator)
				.addTranslator(nucleusTranslator)
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ReportsTranslator.getTranslator())
				.setTranslationEngineBuilder(ProtobufTranslationEngine.builder()
						.addTranslationSpec(new PersonPropertyTranslatorSpec())
						.addTranslationSpec(new GlobalPropertyTranslatorSpec())
						.addTranslationSpec(new RegionTranslatorSpec()));

		this.writingTranslationController = translationControllerBuilder.build();
	}

	private void handleSimulationStateCollection(Integer scenarioId, List<Object> output) {

		System.out.println(scenarioId);

		for (Object object : output) {
			if (object instanceof PluginData) {
				writingTranslationController.writeOutput((PluginData) object, scenarioId);
			}
			/*
			 * if (object instanceof SimulationTime) {
			 * writingTranslationController.writeObjectOutput(object, scenarioId);
			 * }
			 */
		}

	}

	public static void main(String[] args) {
		Path outputDirectory = Paths.get(args[0]);

		new SerializationDemonstration(outputDirectory).execute();
	}

}
