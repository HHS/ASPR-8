package lesson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.actors.antigenproducer.AntigenProducerPluginData;
import lesson.plugins.model.actors.contactmanager.ContactManagerPluginData;
import lesson.plugins.model.actors.vaccinator.VaccinatorPluginData;
import lesson.plugins.model.actors.vaccineproducer.VaccineProducerPluginData;
import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.GroupType;
import lesson.plugins.model.support.Material;
import lesson.plugins.model.support.MaterialsProducer;
import lesson.plugins.model.support.ModelReportLabel;
import lesson.plugins.model.support.PersonProperty;
import lesson.plugins.model.support.Region;
import lesson.plugins.model.support.Resource;
import nucleus.Experiment;
import nucleus.Plugin;
import nucleus.SimulationState;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginData.Builder;
import plugins.groups.GroupsPlugin;
import plugins.groups.GroupsPluginData;
import plugins.materials.MaterialsPlugin;
import plugins.materials.MaterialsPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.support.ResourceId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.WellState;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class PlanTestDriver {
	private int iterationCount = 0;
	private final static boolean executeFull = false;

	public static void main(final String[] args) throws IOException {
		Path baseOutputDirectory = Paths.get(args[0]);
		PlanTestDriver planTestDriver = new PlanTestDriver(baseOutputDirectory);

		if (executeFull) {
			planTestDriver.executeFull();
		} else {
			planTestDriver.executeByParts();
		}

	}

	private void clearDirectory(File file) {
		if (!file.isDirectory()) {
			throw new RuntimeException("not a directory");
		}
		for (File f : file.listFiles()) {
			if (f.isDirectory()) {
				clearDirectory(f);
			}
			f.delete();
		}
	}

	private List<Plugin> getStartingPlugins() {
		List<Plugin> plugins = new ArrayList<>();
		plugins.add(getMaterialsPlugin());//
		plugins.add(getResourcesPlugin());//
		plugins.add(getGlobalPropertiesPlugin());//
		plugins.add(getPersonPropertiesPlugin());//
		plugins.add(getStochasticsPlugin());//
		plugins.add(getRegionsPlugin());//
		plugins.add(getPeoplePlugin());//
		plugins.add(getGroupsPlugin());//
		plugins.add(getModelPlugin());//
		return plugins;
	}

	private List<Plugin> getPlugins(StateCollector stateCollector) {
		List<Plugin> plugins = new ArrayList<>();
		GlobalPropertiesPluginData globalPropertiesPluginData = //
				stateCollector.get(0, GlobalPropertiesPluginData.class).get();
		Plugin globalPropertiesPlugin = GlobalPropertiesPlugin	.builder()//
																.setGlobalPropertiesPluginData(globalPropertiesPluginData)//
																.getGlobalPropertiesPlugin();
		plugins.add(globalPropertiesPlugin);

		StochasticsPluginData stochasticsPluginData = stateCollector.get(0, StochasticsPluginData.class).get();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		plugins.add(stochasticsPlugin);

		PeoplePluginData peoplePluginData = stateCollector.get(0, PeoplePluginData.class).get();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		plugins.add(peoplePlugin);

		RegionsPluginData regionsPluginData = stateCollector.get(0, RegionsPluginData.class).get();
		Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();
		plugins.add(regionsPlugin);

		GroupsPluginData groupsPluginData = stateCollector.get(0, GroupsPluginData.class).get();
		Plugin groupsPlugin = GroupsPlugin.builder().setGroupsPluginData(groupsPluginData).getGroupsPlugin();
		plugins.add(groupsPlugin);

		ResourcesPluginData resourcesPluginData = stateCollector.get(0, ResourcesPluginData.class).get();
		Plugin resourcesPlugin = ResourcesPlugin.builder().setResourcesPluginData(resourcesPluginData).getResourcesPlugin();
		plugins.add(resourcesPlugin);

		MaterialsPluginData materialsPluginData = stateCollector.get(0, MaterialsPluginData.class).get();
		Plugin materialsPlugin = MaterialsPlugin.builder().setMaterialsPluginData(materialsPluginData).getMaterialsPlugin();
		plugins.add(materialsPlugin);

		PersonPropertiesPluginData personPropertiesPluginData = null;
		Optional<PersonPropertiesPluginData> optional1 = stateCollector.get(0, PersonPropertiesPluginData.class);
		if (optional1.isPresent()) {
			personPropertiesPluginData = optional1.get();
		}
		PersonPropertyReportPluginData personPropertyReportPluginData = null;
		Optional<PersonPropertyReportPluginData> optional2 = stateCollector.get(0, PersonPropertyReportPluginData.class);
		if (optional2.isPresent()) {
			personPropertyReportPluginData = optional2.get();
		}
		Plugin personPropertyPlugin = PersonPropertiesPlugin.builder()//
															.setPersonPropertiesPluginData(personPropertiesPluginData)//
															.setPersonPropertyReportPluginData(personPropertyReportPluginData).getPersonPropertyPlugin();
		plugins.add(personPropertyPlugin);

		AntigenProducerPluginData antigenProducerPluginData = null;
		Optional<AntigenProducerPluginData> optional3 = stateCollector.get(0, AntigenProducerPluginData.class);
		if(optional3.isPresent()) {
			antigenProducerPluginData = optional3.get();
		}
		
		VaccineProducerPluginData vaccineProducerPluginData = null;
		Optional<VaccineProducerPluginData> optional4 = stateCollector.get(0, VaccineProducerPluginData.class);
		if(optional4.isPresent()) {
			vaccineProducerPluginData = optional4.get();
		}
		
		
		VaccinatorPluginData vaccinatorPluginData = null;
		Optional<VaccinatorPluginData> optional5 = stateCollector.get(0, VaccinatorPluginData.class);
		if(optional5.isPresent()) {
			vaccinatorPluginData = optional5.get();
		}
		
		ContactManagerPluginData contactManagerPluginData = null;
		Optional<ContactManagerPluginData> optional6 = stateCollector.get(0, ContactManagerPluginData.class);
		if(optional6.isPresent()) {
			contactManagerPluginData = optional6.get();
		}

		Plugin modelPlugin = ModelPlugin.builder()//
										.setAntigenProducerPluginData(antigenProducerPluginData)//
										.setVaccineProducerPluginData(vaccineProducerPluginData)//
										.setVaccinatorPluginData(vaccinatorPluginData)//
										.setContactManagerPluginData(contactManagerPluginData)//
										.getModelPlugin();
		plugins.add(modelPlugin);

		return plugins;
	}

	private void executeFull() throws IOException {
		clearDirectory(baseOutputDirectory.toFile());

		SimulationState simulationState = SimulationState.builder().build();
		List<Plugin> plugins = getStartingPlugins();

		Path outputDirectory = baseOutputDirectory.resolve("sub" + iterationCount++);
		if (!Files.exists(outputDirectory)) {
			Files.createDirectory(outputDirectory);
		}
		Experiment.Builder builder = Experiment.builder();//
		for (Plugin plugin : plugins) {
			builder.addPlugin(plugin);
		}

		Experiment experiment = builder	.setSimulationState(simulationState)//
										.addExperimentContextConsumer(getNIOReportItemHandler(outputDirectory))//
										.build();//

		experiment.execute();//

	}

	private void executeByParts() throws IOException {
		clearDirectory(baseOutputDirectory.toFile());

		SimulationState simulationState = SimulationState.builder().build();
		List<Plugin> plugins = getStartingPlugins();

		for (int i = 0; i < 45; i++) {						
			StateCollector stateCollector = executeSim(simulationState, plugins);
			plugins = getPlugins(stateCollector);
			simulationState = stateCollector.get(0, SimulationState.class).get();
		}		
	}

	private final RandomGenerator randomGenerator = 
			//RandomGeneratorProvider.getRandomGenerator(9032703880551658180L);
	        RandomGeneratorProvider.getRandomGenerator(1713830743266777795L);
	
	private final Path baseOutputDirectory;

	private PlanTestDriver(Path baseOutputDirectory) {
		this.baseOutputDirectory = baseOutputDirectory;
	}

	private StateCollector executeSim(SimulationState simulationState, List<Plugin> plugins) throws IOException {
		
		Path outputDirectory = baseOutputDirectory.resolve("sub" + iterationCount++);
		if (!Files.exists(outputDirectory)) {
			Files.createDirectory(outputDirectory);
		}
		Experiment.Builder builder = Experiment.builder();//
		for (Plugin plugin : plugins) {
			builder.addPlugin(plugin);
		}
		StateCollector stateCollector = new StateCollector();

		Experiment experiment = builder	.setSimulationState(simulationState)//
										.addExperimentContextConsumer(getNIOReportItemHandler(outputDirectory))//
										.addExperimentContextConsumer(stateCollector)//
										.setRecordState(true)//
										.setSimulationHaltTime(simulationState.getStartTime() + 10)//
										.build();//

		experiment.execute();//

		return stateCollector;

	}

	private Plugin getGlobalPropertiesPlugin() {
		final Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setPropertyValueMutability(false)//
																	.setDefaultValue(0.0)//
																	.build();

		builder.defineGlobalProperty(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_HOME_SIZE, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_SCHOOL_SIZE, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_WORK_SIZE, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.CHILD_POPULATION_PROPORTION, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.SENIOR_POPULATION_PROPORTION, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.R0, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.COMMUNITY_CONTACT_RATE, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.INFECTION_THRESHOLD, propertyDefinition,0);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setPropertyValueMutability(false)//
												.build();
		builder.defineGlobalProperty(GlobalProperty.INITIAL_INFECTIONS, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.MIN_INFECTIOUS_PERIOD, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.MAX_INFECTIOUS_PERIOD, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition,0);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Boolean.class)//
												.setDefaultValue(false)//
												.setPropertyValueMutability(true)//
												.build();
		builder.defineGlobalProperty(GlobalProperty.MANUFACTURE_VACCINE, propertyDefinition,0);

		builder.setGlobalPropertyValue(GlobalProperty.POPULATION_SIZE, 10_000,0);
		builder.setGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, 1.0,0);
		builder.setGlobalPropertyValue(GlobalProperty.INITIAL_INFECTIONS, 1,0);
		builder.setGlobalPropertyValue(GlobalProperty.MIN_INFECTIOUS_PERIOD, 7,0);
		builder.setGlobalPropertyValue(GlobalProperty.MAX_INFECTIOUS_PERIOD, 14,0);
		builder.setGlobalPropertyValue(GlobalProperty.R0, 2.0,0);
		builder.setGlobalPropertyValue(GlobalProperty.CHILD_POPULATION_PROPORTION, 0.235,0);
		builder.setGlobalPropertyValue(GlobalProperty.SENIOR_POPULATION_PROPORTION, 0.169,0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_HOME_SIZE, 2.5,0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_SCHOOL_SIZE, 250.0,0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_WORK_SIZE, 30.0,0);
		builder.setGlobalPropertyValue(GlobalProperty.INFECTION_THRESHOLD, 0.0,0);
		builder.setGlobalPropertyValue(GlobalProperty.COMMUNITY_CONTACT_RATE, 0.0,0);

		final GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.builder().setGlobalPropertiesPluginData(globalPropertiesPluginData).getGlobalPropertiesPlugin();

	}

	private Plugin getModelPlugin() {
		final AntigenProducerPluginData.Builder antigenProducerPluginDataBuilder = AntigenProducerPluginData.builder();
		antigenProducerPluginDataBuilder.setMaterialsProducerId(MaterialsProducer.ANTIGEN_PRODUCER);
		AntigenProducerPluginData antigenProducerPluginData = antigenProducerPluginDataBuilder.build();

		VaccineProducerPluginData.Builder vaccineProducerPluginDataBuilder = VaccineProducerPluginData.builder();
		vaccineProducerPluginDataBuilder.setMaterialsProducerId(MaterialsProducer.VACCINE_PRODUCER);
		VaccineProducerPluginData vaccineProducerPluginData = vaccineProducerPluginDataBuilder.build();

		VaccinatorPluginData.Builder vaccinatorPluginDataBuilder = VaccinatorPluginData.builder();
		VaccinatorPluginData vaccinatorPluginData = vaccinatorPluginDataBuilder.build();

		ContactManagerPluginData.Builder contactManagerPluginDataBuilder = ContactManagerPluginData.builder();
		ContactManagerPluginData contactManagerPluginData = contactManagerPluginDataBuilder.build();

		return ModelPlugin	.builder()//
							.setAntigenProducerPluginData(antigenProducerPluginData)//
							.setVaccineProducerPluginData(vaccineProducerPluginData)//
							.setVaccinatorPluginData(vaccinatorPluginData)//
							.setContactManagerPluginData(contactManagerPluginData)//
							.getModelPlugin();
	}

	private Plugin getGroupsPlugin() {
		final GroupsPluginData.Builder builder = GroupsPluginData.builder();
		for (final GroupType groupType : GroupType.values()) {
			builder.addGroupTypeId(groupType);
		}
		final GroupsPluginData groupsPluginData = builder.build();
		return GroupsPlugin.builder().setGroupsPluginData(groupsPluginData).getGroupsPlugin();
	}

	private Plugin getMaterialsPlugin() {
		final MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		for (final MaterialsProducer materialsProducer : MaterialsProducer.values()) {
			builder.addMaterialsProducerId(materialsProducer);
		}
		for (final Material material : Material.values()) {
			builder.addMaterial(material);
		}
		final MaterialsPluginData materialsPluginData = builder.build();
		return MaterialsPlugin.builder().setMaterialsPluginData(materialsPluginData).getMaterialsPlugin();
	}

	private Plugin getPeoplePlugin() {
		final PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	private Plugin getPersonPropertiesPlugin() {

		final PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Boolean.class)//
																	.setDefaultValue(false)//
																	.build();

		builder.definePersonProperty(PersonProperty.VACCINATED, propertyDefinition,0,false);//
		builder.definePersonProperty(PersonProperty.VACCINE_SCHEDULED, propertyDefinition,0,false);//

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.build();//
		builder.definePersonProperty(PersonProperty.AGE, propertyDefinition,0,false);//

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(DiseaseState.class)//
												.setDefaultValue(DiseaseState.SUSCEPTIBLE)//
												.build();

		builder.definePersonProperty(PersonProperty.DISEASE_STATE, propertyDefinition,0,false);//

		final PersonPropertiesPluginData personPropertiesPluginData = builder.build();

//		PersonPropertyReportPluginData personPropertyReportPluginData = //
//				PersonPropertyReportPluginData	.builder()//
//												.setReportLabel(ModelReportLabel.PERSON_PROPERTY_REPORT)//
//												.setReportPeriod(ReportPeriod.DAILY)//
//												.includePersonProperty(PersonProperty.VACCINATED)//
//												.includePersonProperty(PersonProperty.VACCINE_SCHEDULED)//
//												.build();

		return PersonPropertiesPlugin	.builder()//
										.setPersonPropertiesPluginData(personPropertiesPluginData)//
										// .setPersonPropertyReportPluginData(personPropertyReportPluginData)//
										.getPersonPropertyPlugin();

	}

	private Plugin getRegionsPlugin() {
		final RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();

		for (int i = 0; i < 1; i++) {
			regionsPluginDataBuilder.addRegion(new Region(i));
		}
		final RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		return RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();
	}

	private NIOReportItemHandler getNIOReportItemHandler(Path outputDirectory) {
		return NIOReportItemHandler	.builder()//
									.addReport(ModelReportLabel.DISEASE_STATE_REPORT, outputDirectory.resolve("disease_state_report.csv"))//
									.addReport(ModelReportLabel.PERSON_PROPERTY_REPORT, outputDirectory.resolve("person_property_report.csv"))//
									.addReport(ModelReportLabel.VACCINE_REPORT, outputDirectory.resolve("vaccine_report.csv"))//
									.addReport(ModelReportLabel.VACCINE_PRODUCTION_REPORT, outputDirectory.resolve("vaccine_production_report.csv"))//
									.setDelimiter(",")//
									.build();
	}

	private Plugin getResourcesPlugin() {
		final ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (final ResourceId resourcId : Resource.values()) {
			builder.addResource(resourcId,0.0);
		}
		final ResourcesPluginData resourcesPluginData = builder.build();
		return ResourcesPlugin.builder().setResourcesPluginData(resourcesPluginData).getResourcesPlugin();
	}

	private Plugin getStochasticsPlugin() {
		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		final StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//
																					.setMainRNGState(wellState)//
																					.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

}
