package lesson.plugins.disease;

import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.nucleus.SimplePluginId;

/**
 * Static plugin id implementation for the GlobalsPlugin
 * 
 *
 */
/* start code_ref=plugin_data_plugin_id */
public final class DiseasePluginId implements PluginId {
	private DiseasePluginId() {
	}

	public final static PluginId PLUGIN_ID = new SimplePluginId("disease plugin id");
}
/* end */
