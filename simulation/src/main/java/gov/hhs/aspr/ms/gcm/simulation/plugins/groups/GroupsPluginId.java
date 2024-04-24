package gov.hhs.aspr.ms.gcm.simulation.plugins.groups;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the GroupPlugin
 */
public final class GroupsPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new GroupsPluginId();

	private GroupsPluginId() {
	};
}
