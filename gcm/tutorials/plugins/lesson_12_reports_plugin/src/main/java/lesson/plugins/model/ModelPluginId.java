package lesson.plugins.model;

import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.nucleus.SimplePluginId;

/**
 * Static plugin id implementation for the Model Plugin
 * 
 *
 */

public final class ModelPluginId implements PluginId {
	private ModelPluginId() {
	};

	public final static PluginId PLUGIN_ID = new SimplePluginId("model plugin id");

}
