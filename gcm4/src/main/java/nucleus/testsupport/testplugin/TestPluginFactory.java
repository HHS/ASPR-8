package nucleus.testsupport.testplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import util.errors.ContractException;

/**
 * A static test support class for the {@linkplain TestPlugin}. Provides
 * convenience
 * methods for obtaining standarized PluginData for the listed Plugin.
 * 
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be
 * utilized with
 * </p>
 * 
 * <li>{@link TestSimulation#executeSimulation}
 */
public final class TestPluginFactory {
	private TestPluginFactory() {
	}

	private static class Data {
		private TestPluginData testPluginData;

		private Data(TestPluginData testPluginData) {
			this.testPluginData = testPluginData;
		}
	}

	/**
	 * Factory class that facilitates the building of {@linkplain PluginData}
	 * with the various setter methods.
	 */
	public static class Factory {
		private Data data;

		private Factory(Data data) {
			this.data = data;
		}

		/**
		 * Returns a list of plugins containing a TestPlugin built from the contributed
		 * PluginDatas
		 * 
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link TestPluginFactory#factory}
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}
	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link TestPlugin} by generating:
	 * <ul>
	 * <li>{@link TestPluginData}
	 * </ul>
	 * <li>via the
	 * {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption
	 *                           {@linkplain NucleusError#NULL_PLUGIN_DATA}
	 *                           if testPluginData is null
	 */
	public static Factory factory(TestPluginData testPluginData) {
		if (testPluginData == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
		}
		return new Factory(new Data(testPluginData));
	}

	/**
	 * Returns a Factory that facilitates the creation of a minimal set of plugins
	 * needed to adequately test the {@link TestPlugin} by generating:
	 * <ul>
	 * <li>{@link TestPluginData}
	 * </ul>
	 * <li>via the
	 * {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption
	 *                           {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER}
	 *                           if consumer is null
	 */
	public static Factory factory(Consumer<ActorContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
		}
		TestPluginData testPluginData = TestPluginData.builder()//
				.addTestActorPlan("actor", new TestActorPlan(0, consumer))//
				.build();

		return factory(testPluginData);
	}

}