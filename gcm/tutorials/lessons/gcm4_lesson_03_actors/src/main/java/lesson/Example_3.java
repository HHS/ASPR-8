package lesson;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.SimplePluginId;
import nucleus.Simulation;

public final class Example_3 {

	private Example_3() {
	}

	/**
	 * Introducing the addition of a plugin and an actor
	 */
	public static void main(String[] args) {
		/* start code_ref=actors_actor_context_using_lambdas */
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

	/* start code_ref=actors_plugin_initializer */
	public static void pluginInit(PluginContext pluginContext) {
		System.out.println("plugin being initialized -- we will add one actor");
		pluginContext.addActor(Example_3::actorInit);
	}
	/* end */

	/* start code_ref=actors_actor_init */
	public static void actorInit(ActorContext actorContext) {
		System.out.println("actor being initialized");
		System.out.println("my id = " + actorContext.getActorId());
		System.out.println("time = " + actorContext.getTime());
	}
	/* end */

	public static void altMain(String[] args) {
		/* start code_ref=actors_intro_to_plugin_context */
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
