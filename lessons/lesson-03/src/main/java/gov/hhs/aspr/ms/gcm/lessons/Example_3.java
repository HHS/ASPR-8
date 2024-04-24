package gov.hhs.aspr.ms.gcm.lessons;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimplePluginId;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Simulation;

public final class Example_3 {

	private Example_3() {
	}

	/**
	 * Introducing the addition of a plugin and an actor
	 */
	public static void main(String[] args) {
		/* start code_ref=actors_actor_context_using_lambdas|code_cap=A single actor writes output to the console during its initialization.*/
		PluginId pluginId = new SimplePluginId("example plugin");

		Plugin plugin = Plugin.builder()//
				.setPluginId(pluginId)//
				.setInitializer(pluginContext -> {
					System.out.println("plugin being initialized -- we will add one actor");
					pluginContext.addActor(actorContext -> {
						System.out.println("actor being initialized");
						System.out.println("my id = " + actorContext.getActorId());
						System.out.println("time = " + actorContext.getTime());
					});
				})//
				.build();

		Simulation.builder()//
				.addPlugin(plugin)//
				.build()//
				.execute();
		/* end */
	}

	/* start code_ref=actors_plugin_initializer|code_cap=A single actor is being added to the simulation at initialization.*/
	public static void pluginInit(PluginContext pluginContext) {
		System.out.println("plugin being initialized -- we will add one actor");
		pluginContext.addActor(Example_3::actorInit);
	}
	/* end */

	/* start code_ref=actors_actor_init|code_cap=The actor prints out some identifying information when it initializes.*/
	public static void actorInit(ActorContext actorContext) {
		System.out.println("actor being initialized");
		System.out.println("my id = " + actorContext.getActorId());
		System.out.println("time = " + actorContext.getTime());
	}
	/* end */

	public static void altMain(String[] args) {
		/* start code_ref=actors_intro_to_plugin_context|code_cap=An initializer uses a plugin context to execute the initialization logic at the beginning of each simulation.*/
		PluginId pluginId = new SimplePluginId("example plugin");

		Plugin plugin = Plugin.builder()//
				.setPluginId(pluginId)//
				.setInitializer(Example_3::pluginInit)//
				.build();

		Simulation.builder()//
				.addPlugin(plugin)//
				.build()//
				.execute();
		/* end */

	}
}
