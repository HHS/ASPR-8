package lessons.lesson_07;

import lessons.lesson_07.plugins.family.FamilyDataManager;
import lessons.lesson_07.plugins.model.ModelActor;
import lessons.lesson_07.plugins.people.PersonDataManager;
import lessons.lesson_07.plugins.vaccine.VaccinationDataManager;
import nucleus.Plugin;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.Simulation;

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

		
		Plugin vaccinePlugin = Plugin	.builder()//
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
		Plugin modelPlugin = Plugin	.builder()//
									.setPluginId(modelPluginId)//
									.setInitializer(pluginContext -> {
										pluginContext.addActor(new ModelActor()::init);

									})//
									.build();

		Simulation	.builder()//
					.addPlugin(vaccinePlugin)//
					.addPlugin(familyPlugin)//
					.addPlugin(peoplePlugin)//
					.addPlugin(modelPlugin)//
					.build()//
					.execute();
	}
}
