package plugins.reports;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = ReportsPluginId.class)
public class AT_ReportsPluginId {

	@Test
	public void test() {
		assertNotNull(ReportsPluginId.PLUGIN_ID);
	}
}
