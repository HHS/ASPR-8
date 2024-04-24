package gov.hhs.aspr.ms.gcm.simulation.plugins.materials;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the materials plugin
 */
public final class MaterialsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new MaterialsPluginId();

	private MaterialsPluginId() {
	};
}
