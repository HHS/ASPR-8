package gov.hhs.aspr.ms.gcm.lessons.plugins.policy;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;

public final class PolicyPlugin {

	private PolicyPlugin() {

	}

	public static Plugin getPolicyPlugin(PolicyPluginData policyPluginData) {

		return Plugin.builder()//
				.addPluginData(policyPluginData)//
				.setPluginId(PolicyPluginId.PLUGIN_ID)//
				.setInitializer((c) -> {
					PolicyPluginData pluginData = c.getPluginData(PolicyPluginData.class).get();
					c.addDataManager(new PolicyDataManager(pluginData));
				})//
				.build();
	}

}
