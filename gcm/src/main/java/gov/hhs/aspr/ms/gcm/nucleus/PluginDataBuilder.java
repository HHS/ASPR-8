package gov.hhs.aspr.ms.gcm.nucleus;

/**
 * PlugingDataBuilder is an interface for the builders of plugins.
 */
/*
 * start code_ref=plugin_data_plugin_builder|code_cap=Every plugin data class
 * has a corresponding builder class to aid in the experiment's generation of
 * alternate scenarios.
 */
public interface PluginDataBuilder {
	/**
	 * Returns a plugin data
	 */
	public PluginData build();
}
/* end */