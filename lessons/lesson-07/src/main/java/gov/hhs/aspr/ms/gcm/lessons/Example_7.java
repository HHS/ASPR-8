package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.lessons.plugins.family.FamilyDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelActor;
import gov.hhs.aspr.ms.gcm.lessons.plugins.people.PersonDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.VaccinationDataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimplePluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Simulation;

public final class Example_7 {

	private Example_7() {
	}

	/**
	 * Emphasize the lack of order, and the plugin dependencies
	 */
	public static void main(String[] args) {

		PluginId peoplePluginId = new SimplePluginId("people plugin");
		PluginId familyPluginId = new SimplePluginId("family plugin");
		PluginId vaccinePluginId = new SimplePluginId("vaccine plugin");

		Plugin peoplePlugin = Plugin.builder()//
				.setPluginId(peoplePluginId)//
				.setInitializer(pluginContext -> {
					pluginContext.addDataManager(new PersonDataManager());
				})//
				.build();

		Plugin vaccinePlugin = Plugin.builder()//
				.setPluginId(vaccinePluginId)//
				.addPluginDependency(peoplePluginId)//
				.addPluginDependency(familyPluginId)//
				.setInitializer(pluginContext -> {
					pluginContext.addDataManager(new VaccinationDataManager());
				})//
				.build();

		Plugin familyPlugin = Plugin.builder()//
				.setPluginId(familyPluginId)//
				.addPluginDependency(peoplePluginId)//
				.setInitializer(pluginContext -> {
					pluginContext.addDataManager(new FamilyDataManager());
				})//
				.build();

		PluginId modelPluginId = new SimplePluginId("model plugin");
		Plugin modelPlugin = Plugin.builder()//
				.setPluginId(modelPluginId)//
				.setInitializer(pluginContext -> {
					pluginContext.addActor(new ModelActor()::init);

				})//
				.build();

		Simulation.builder()//
				.addPlugin(vaccinePlugin)//
				.addPlugin(familyPlugin)//
				.addPlugin(peoplePlugin)//
				.addPlugin(modelPlugin)//
				.build()//
				.execute();
	}
}
