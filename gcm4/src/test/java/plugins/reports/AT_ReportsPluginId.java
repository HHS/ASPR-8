package plugins.reports;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestField;

public class AT_ReportsPluginId {

	@Test
	@UnitTestField(target = ReportsPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(ReportsPluginId.PLUGIN_ID);
	}
}
