package gov.hhs.aspr.ms.gcm.plugins.stochastics;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.util.errors.ContractError;
import net.jcip.annotations.ThreadSafe;

/**
 * <p>
 * <b>Summary</b> A nucleus plugin for managing random number generators. The
 * plugin provides a general random generator as well as a set of random
 * generators mapped to a set of identifiers. All random generators are
 * implemented by org.apache.commons.math3.random.Well44497b
 * </p>
 * <p>
 * <b>Plugin Datas</b>
 * <ul>
 * <li><b>Stochastics Plugin Data</b>: Provides initial state for the data
 * manager</li>
 * </ul>
 * <p>
 * <b>Events </b> The plugin supports no events.
 ** </p>
 * <p>
 * <b>Data Managers</b>
 * <ul>
 * <li><b>StochasticsDataManger</b>: Manages the random generators and provides
 * various related capabilities.
 * </ul>
 * <p>
 * <b>Reports</b> The plugin defines no reports
 * </p>
 * <p>
 * <b>Actors: </b>No actors provided.
 * </p>
 * <p>
 * <b>Support classes</b>
 * <ul>
 * <li><b>StochasticsError: </b>Enumeration implementing
 * {@linkplain ContractError} for this plugin.</li>
 * <li><b>RandomNumberGeneratorId: </b>Marker interface for generator id
 * values</li>
 * </ul>
 * <p>
 * <b>Required Plugins</b> This plugin has no plugin dependencies
 * </p>
 */
@ThreadSafe
public final class StochasticsPlugin {

	private StochasticsPlugin() {

	}

	/**
	 * Returns a plugin that will add a StochasticsDataManager to the simulation at
	 * initialization
	 */
	public static Plugin getStochasticsPlugin(StochasticsPluginData stochasticsPluginData) {

		return Plugin.builder()//
				.addPluginData(stochasticsPluginData)//
				.setPluginId(StochasticsPluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					StochasticsPluginData pluginData = c.getPluginData(StochasticsPluginData.class).get();
					c.addDataManager(new StochasticsDataManager(pluginData));
				}).build();//
	}
}
