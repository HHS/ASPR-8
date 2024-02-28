package gov.hhs.aspr.ms.gcm.plugins.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_ReportPlugin {

	@Test
	@UnitTestMethod(target = ReportsPlugin.class, name = "getReportsPlugin", args = {})
	public void testGetReportPlugin() {

		Plugin reportPlugin = ReportsPlugin.getReportsPlugin();

		// show that the plugin has the correct idea
		assertEquals(ReportsPluginId.PLUGIN_ID, reportPlugin.getPluginId());

	}

}
