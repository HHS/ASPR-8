package lesson;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.nucleus.ExperimentContext;
import gov.hhs.aspr.ms.gcm.nucleus.ExperimentParameterData;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.SimulationState;
import gov.hhs.aspr.ms.gcm.nucleus.SimulationStateCollector;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.GlobalPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyDimension;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.PersonPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.reports.ReportsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.NIOReportItemHandler;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.NucleusTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.GlobalPropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.globalproperties.data.input.GlobalPropertiesPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.data.input.PeoplePluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.PersonPropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.data.input.RegionsPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.stochastics.StochasticsTranslator;
import gov.hhs.aspr.ms.taskit.core.TranslationController;
import gov.hhs.aspr.ms.taskit.core.Translator;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportLabel;
import lesson.plugins.model.PersonProperty;
import lesson.plugins.model.Region;
import lesson.translatorSpecs.GlobalPropertyTranslatorSpec;
import lesson.translatorSpecs.PersonPropertyTranslatorSpec;
import lesson.translatorSpecs.RegionTranslatorSpec;
import util.random.RandomGeneratorProvider;
import util.time.Stopwatch;

public final class SerializationDemonstration {

	private final String personPropertiesOutputFileName = "personPropertiesOutput.json";
	private final String globalPropertiesOutputFileName = "globalPropertiesOutput.json";
	private final String regionsOutputFileName = "regionsOutput.json";
	private final String peopleOutputFileName = "peopleOutput.json";
	private final String stochasticsOutputFileName = "stochasticsOutput.json";
	private final String simStateOutputFileName = "simStateOutput.json";
	private final String peopleInputFileName = "/peopleInput.json";
	private final String regionsInputFileName = "/regionsInput.json";
	private final String globalPropertiesInputFileName = "/globalPropertiesInput.json";

	private final Path outputDirectory;
	private TranslationController writingTranslationController;
	private TranslationController readingTranslationController;

	private SerializationDemonstration(Path outputDirectory) {
		this.outputDirectory = outputDirectory;

		Path peopleInputPath;
		Path regionsInputPath;
		Path globalPropsInputPath;

		try {
			peopleInputPath = Paths.get(this.getClass().getResource(peopleInputFileName).toURI());
			regionsInputPath = Paths.get(this.getClass().getResource(regionsInputFileName).toURI());
			globalPropsInputPath = Paths.get(this.getClass().getResource(globalPropertiesInputFileName).toURI());

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
					// .addInputFilePath(peopleInputPath,
					// 		PeoplePluginDataInput.class)
					// .addInputFilePath(regionsInputPath,
					// 		RegionsPluginDataInput.class)
					// .addInputFilePath(globalPropsInputPath,
					// 		GlobalPropertiesPluginDataInput.class)
					.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

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
		builder.definePersonProperty(PersonProperty.VACCINE_ATTEMPTS,
		propertyDefinition, 0.0, false);

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

			Path personPropertiesPath = Paths.get(outputDir.getAbsolutePath()).resolve(personPropertiesOutputFileName);
			Path globalPropertiesPath = Paths.get(outputDir.getAbsolutePath()).resolve(globalPropertiesOutputFileName);
			Path regionsPath = Paths.get(outputDir.getAbsolutePath()).resolve(regionsOutputFileName);
			Path peoplePath = Paths.get(outputDir.getAbsolutePath()).resolve(peopleOutputFileName);
			Path stochasticsPath = Paths.get(outputDir.getAbsolutePath()).resolve(stochasticsOutputFileName);
			Path simStatepath = Paths.get(outputDir.getAbsolutePath()).resolve(simStateOutputFileName);

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
