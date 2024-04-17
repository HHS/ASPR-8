package gov.hhs.aspr.ms.gcm.simulation.plugins.properties;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

/**
 * A nucleus plugin for representing people, dealing only with their existence.
 */
public final class PropertiesPlugin {

	private PropertiesPlugin() {

	}

	/**
	 * Returns the properties plugin.
	 * <p>
	 * Depends on plugins: none
	 * <p>
	 * Provides data mangers: none
	 * </p>
	 */
	public static Plugin getPropertiesPlugin() {

		return Plugin.builder()//				
				.setPluginId(PropertiesPluginId.PLUGIN_ID)//				
				.build();
	}

}
