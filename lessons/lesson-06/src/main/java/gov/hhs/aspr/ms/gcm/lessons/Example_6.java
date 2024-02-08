package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.lessons.plugins.family.FamilyDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelActor;
import gov.hhs.aspr.ms.gcm.lessons.plugins.people.PersonDataManager;
import gov.hhs.aspr.ms.gcm.lessons.plugins.vaccine.VaccinationDataManager;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.nucleus.SimplePluginId;
import gov.hhs.aspr.ms.gcm.nucleus.Simulation;

public final class Example_6 {

	private Example_6() {
	}

	/*
	 * start code_ref=plugin_dependencies_connecting_the_plugins|code_cap=The
	 * people, vaccine, family and model plugins are contributed to the simulation.
	 * On execution, the model plugin's single actor schedules the vaccination of
	 * each person as well as a few random removals of people from the simulation.
	 */
	public static void main(String[] args) {

		PluginId peoplePluginId = new SimplePluginId("people plugin");
		Plugin peoplePlugin = Plugin.builder()//
				.setPluginId(peoplePluginId)//
				.setInitializer(pluginContext -> {
					pluginContext.addDataManager(new PersonDataManager());
				})//
				.build();

		PluginId vaccinePluginId = new SimplePluginId("vaccine plugin");
		Plugin vaccinePlugin = Plugin.builder()//
				.setPluginId(vaccinePluginId)//
				.addPluginDependency(peoplePluginId)//
				.setInitializer(pluginContext -> {
					pluginContext.addDataManager(new VaccinationDataManager());
				})//
				.build();

		PluginId familyPluginId = new SimplePluginId("family plugin");
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
	/* end */

}
