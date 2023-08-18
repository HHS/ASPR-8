package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import util.errors.ContractException;

/**
 * A plugin context provides plugin's the ability to add actors and data
 * managers to the initialization of each simulation instance(scenario) in an
 * experiment. It provides the set of plugin data objects gathered from the
 * plugins that compose the experiment.
 */
public final class PluginContext {
	private final Simulation simulation;

	protected PluginContext(Simulation simulation) {
		this.simulation = simulation;
	}

	/**
	 * Adds a data manager to the simulation.
	 * 
	 * @throws util.errors.ContractException {@link NucleusError#PLUGIN_INITIALIZATION_CLOSED}
	 *                           if plugin initialization is over
	 */
	public void addDataManager(DataManager dataManager) {
		simulation.addDataManagerForPlugin(dataManager);
	}

	/**
	 * Adds an actor to the simulation.
	 * 
	 * @throws util.errors.ContractException {@link NucleusError#PLUGIN_INITIALIZATION_CLOSED}
	 *                           if plugin initialization is over
	 */
	public ActorId addActor(Consumer<ActorContext> init) {
		return simulation.addActorForPlugin(init);
	}

	/**
	 * Adds a report to the simulation.
	 * 
	 * @throws util.errors.ContractException
	 *                           <ul>
	 *                           <li>{@link NucleusError#PLUGIN_INITIALIZATION_CLOSED}
	 *                           if plugin initialization is over</li>
	 *                           <li>{@link NucleusError#NULL_REPORT_CONTEXT_CONSUMER}
	 *                           if the consumer is null</li>
	 *                           <ul>
	 */
	public void addReport(Consumer<ReportContext> init) {
		simulation.addReportForPlugin(init);
	}

	/**
	 * Returns the plugin data object associated with the given class reference
	 * 
	 * @throws util.errors.ContractException
	 *                           <ul>
	 *                           <li>{@linkplain NucleusError#NULL_PLUGIN_DATA_CLASS}
	 *                           if the class reference is null</li>
	 *                           <li>{@linkplain NucleusError#AMBIGUOUS_PLUGIN_DATA_CLASS}
	 *                           if more than one plugin data object matches the
	 *                           class reference</li>
	 *                           </ul>
	 */
	public <T extends PluginData> Optional<T> getPluginData(Class<T> pluginDataClass) {
		return simulation.getPluginData(pluginDataClass);
	}

	/**
	 * Returns the plugin data objects associated with the given class reference
	 * 
	 * @throws util.errors.ContractException {@linkplain NucleusError#NULL_PLUGIN_DATA_CLASS} if
	 *                           the class reference is null
	 */
	public <T extends PluginData> List<T> getPluginDatas(Class<T> pluginDataClass) {
		return simulation.getPluginDatas(pluginDataClass);
	}

}
