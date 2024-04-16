package gov.hhs.aspr.ms.gcm.lessons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.DiseaseState;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.ModelReportLabel;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.PersonProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.Region;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.VaccinatorType;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.GlobalPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.PartitionsPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.datamanagers.PartitionsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.PersonPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.PersonPropertiesPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesPluginData.Builder;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.NIOReportItemHandler;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.support.WellState;

public final class Example_20 {

	private final Path outputDirectory;

	public static void main(final String[] args) throws IOException {
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

		new Example_20(outputDirectory).execute();
	}

	private Example_20(Path outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	/*
	 * start code_ref=partitions_plugin_partiions_init|code_cap=The partitions
	 * plugin is simple, but requires that it has dependencies on those plugins that
	 * will be used to calculate partition filters and labelers.
	 */
	private Plugin getPartitionsPlugin() {
		PartitionsPluginData partitionsPluginData = PartitionsPluginData.builder()//
				.setRunContinuitySupport(false).build();

		return PartitionsPlugin.builder()//
				.setPartitionsPluginData(partitionsPluginData)//
				.addPluginDependency(PersonPropertiesPluginId.PLUGIN_ID)//
				.getPartitionsPlugin();
	}
	/* end */

	private Plugin getPeoplePlugin() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	private Plugin getStochasticPlugin() {

		WellState mainRNGState = WellState.builder().setSeed(346456567565677L).build();

		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//
				.setMainRNGState(mainRNGState)//
				.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	/*
	 * start code_ref=partitions_plugin_person_properties|code_cap=The four person
	 * properties are defined.
	 */
	private Plugin getPersonPropertiesPlugin() {
		Builder builder = PersonPropertiesPluginData.builder();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setPropertyValueMutability(false).setType(Integer.class).build();
		builder.definePersonProperty(PersonProperty.AGE, propertyDefinition, 0.0, false);

		propertyDefinition = PropertyDefinition.builder()//
				.setPropertyValueMutability(true).setType(Boolean.class)//
				.setDefaultValue(false).build();
		builder.definePersonProperty(PersonProperty.WAITING_FOR_NEXT_DOSE, propertyDefinition, 0.0, false);

		propertyDefinition = PropertyDefinition.builder()//
				.setPropertyValueMutability(true)//
				.setType(DiseaseState.class).setDefaultValue(DiseaseState.SUSCEPTIBLE).build();
		builder.definePersonProperty(PersonProperty.DISEASE_STATE, propertyDefinition, 0.0, false);

		propertyDefinition = PropertyDefinition.builder()//
				.setPropertyValueMutability(true)//
				.setType(Integer.class)//
				.setDefaultValue(0)//
				.build();
		builder.definePersonProperty(PersonProperty.VACCINATION_COUNT, propertyDefinition, 0.0, false);

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();
		return PersonPropertiesPlugin.builder()//
				.setPersonPropertiesPluginData(personPropertiesPluginData)//
				.getPersonPropertyPlugin();
	}
	/* end */

	private Plugin getRegionsPlugin() {
		RegionsPluginData.Builder builder = RegionsPluginData.builder();
		for (Region region : Region.values()) {
			builder.addRegion(region);
		}
		RegionsPluginData regionsPluginData = builder.build();
		return RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();
	}

	/*
	 * start code_ref=partitions_plugin_global_properties|code_cap= The global
	 * properties are all fixed values.
	 */
	private Plugin getGlobalPropertiesPlugin() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false)
				.setDefaultValue(10_000).setType(Integer.class).build();
		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false).setDefaultValue(10)
				.setType(Integer.class).build();
		builder.defineGlobalProperty(GlobalProperty.INITIAL_INFECTION_COUNT, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false)
				.setDefaultValue(VaccinatorType.PARTITION).setType(VaccinatorType.class).build();
		builder.defineGlobalProperty(GlobalProperty.VACCINATOR_TYPE, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false).setDefaultValue(3.0)
				.setType(Double.class).build();
		builder.defineGlobalProperty(GlobalProperty.MINIMUM_INFECTIOUS_PERIOD, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false).setDefaultValue(12.0)
				.setType(Double.class).build();
		builder.defineGlobalProperty(GlobalProperty.MAXIMUM_INFECTIOUS_PERIOD, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false).setDefaultValue(2.0)
				.setType(Double.class).build();
		builder.defineGlobalProperty(GlobalProperty.INFECTIOUS_CONTACT_RATE, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false).setDefaultValue(0.15)
				.setType(Double.class).build();
		builder.defineGlobalProperty(GlobalProperty.TRANSMISSION_PROBABILTY, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false).setDefaultValue(100)
				.setType(Integer.class).build();
		builder.defineGlobalProperty(GlobalProperty.VACCINATIONS_PER_DAY, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder().setPropertyValueMutability(false).setDefaultValue(30.0)
				.setType(Double.class).build();
		builder.defineGlobalProperty(GlobalProperty.INTER_VACCINATION_DELAY_TIME, propertyDefinition, 0);

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();
		return GlobalPropertiesPlugin.builder().setGlobalPropertiesPluginData(globalPropertiesPluginData)
				.getGlobalPropertiesPlugin();
	}

	/* end */
	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler.builder()//
				.addReport(ModelReportLabel.DISEASE_STATE_REPORT, outputDirectory.resolve("disease_state_report.xls"))//
				.build();
	}

	/*
	 * start code_ref=partitions_plugin_example_20_execute|code_cap=The various
	 * plugins are gathered from their initial data and the experiment is executed.
	 */
	private void execute() {
		Experiment.builder()//
				.addPlugin(getGlobalPropertiesPlugin())//
				.addPlugin(getPersonPropertiesPlugin())//
				.addPlugin(getRegionsPlugin())//
				.addPlugin(getStochasticPlugin())//
				.addPlugin(getPeoplePlugin())//
				.addPlugin(getPartitionsPlugin())//
				.addPlugin(ModelPlugin.getModelPlugin())//
				.addExperimentContextConsumer(getNIOReportItemHandler())//
				.build()//
				.execute();
	}
	/* end */

}
