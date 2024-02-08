package lesson;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.reports.ReportsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.NIOReportItemHandler;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
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
import gov.hhs.aspr.ms.taskit.core.TranslationEngineType;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;
import gov.hhs.aspr.ms.util.resourcehelper.ResourceHelper;
import gov.hhs.aspr.ms.util.time.Stopwatch;
import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportLabel;
import lesson.plugins.model.PersonProperty;
import lesson.plugins.model.Region;

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

	// test
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
					.addTranslationEngine(getTranslationEngine())
					.addInputFilePath(peopleInputPath,
							PeoplePluginDataInput.class, TranslationEngineType.PROTOBUF)
					.addInputFilePath(regionsInputPath,
							RegionsPluginDataInput.class, TranslationEngineType.PROTOBUF)
					.addInputFilePath(globalPropsInputPath,
							GlobalPropertiesPluginDataInput.class, TranslationEngineType.PROTOBUF)
					.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}

	private ProtobufTranslationEngine getTranslationEngine() {
		return ProtobufTranslationEngine.builder()
				.addTranslator(PersonPropertiesTranslator.getTranslator())
				.addTranslator(GlobalPropertiesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addTranslator(StochasticsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(NucleusTranslator.getTranslator())
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
		System.out.println("Read input took: " + stopwatch.getElapsedMilliSeconds() + "ms");

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

		translationControllerBuilder.addTranslationEngine(this.getTranslationEngine());

		List<Integer> scenarioIds = new ArrayList<>();
		for (int i = 0; i < experimentContext.getScenarioCount(); i++) {
			scenarioIds.add(i);
		}

		scenarioIds.parallelStream().forEach(scenarioId -> {
			Path scenarioOutputDir = this.outputDirectory.resolve("scenario_" + scenarioId);
			ResourceHelper.makeOutputDir(scenarioOutputDir);

			translationControllerBuilder
					.addOutputFilePath(scenarioOutputDir.resolve(personPropertiesOutputFileName),
							PersonPropertiesPluginData.class, scenarioId, TranslationEngineType.PROTOBUF)
					.addOutputFilePath(scenarioOutputDir.resolve(globalPropertiesOutputFileName),
							GlobalPropertiesPluginData.class, scenarioId, TranslationEngineType.PROTOBUF)
					.addOutputFilePath(scenarioOutputDir.resolve(regionsOutputFileName), RegionsPluginData.class,
							scenarioId, TranslationEngineType.PROTOBUF)
					.addOutputFilePath(scenarioOutputDir.resolve(peopleOutputFileName), PeoplePluginData.class,
							scenarioId, TranslationEngineType.PROTOBUF)
					.addOutputFilePath(scenarioOutputDir.resolve(stochasticsOutputFileName),
							StochasticsPluginData.class, scenarioId, TranslationEngineType.PROTOBUF)
					.addOutputFilePath(scenarioOutputDir.resolve(simStateOutputFileName), SimulationState.class,
							scenarioId, TranslationEngineType.PROTOBUF);
		});

		this.writingTranslationController = translationControllerBuilder.build();
	}

	private void handleSimulationStateCollection(Integer scenarioId, List<Object> output) {

		for (Object object : output) {
			if (object instanceof PluginData) {
				writingTranslationController.writeOutput((PluginData) object, scenarioId);
			}

			if (object instanceof SimulationState) {
				writingTranslationController.writeOutput(object, scenarioId);
			}

		}

	}

	public static void main(String[] args) {
		Path outputDirectory = Paths.get(args[0]);

		new SerializationDemonstration(outputDirectory).execute();
	}

}
