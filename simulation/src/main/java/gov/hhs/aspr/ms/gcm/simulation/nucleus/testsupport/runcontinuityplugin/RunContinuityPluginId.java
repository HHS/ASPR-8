package gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.runcontinuityplugin;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;

/**
 * Static plugin id implementation for the person properties plugin
 */
public final class RunContinuityPluginId implements PluginId {
	public final static PluginId PLUGIN_ID = new RunContinuityPluginId();

	private RunContinuityPluginId() {
	};
}
