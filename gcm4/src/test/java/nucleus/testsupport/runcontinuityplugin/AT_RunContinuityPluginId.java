package nucleus.testsupport.runcontinuityplugin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestField;

public class AT_RunContinuityPluginId {

	@Test
	@UnitTestField(target = RunContinuityPluginId.class, name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(RunContinuityPluginId.PLUGIN_ID);
	}

}
