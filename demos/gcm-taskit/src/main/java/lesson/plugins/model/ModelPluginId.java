package lesson.plugins.model;

import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.nucleus.SimplePluginId;

public final class ModelPluginId {
	private ModelPluginId() {
	}

	public final static PluginId PLUGIN_ID = new SimplePluginId("model plugin");
}
