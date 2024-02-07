package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_RunContinuityPlugin {

	@Test
	@UnitTestMethod(target = RunContinuityPlugin.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(RunContinuityPlugin.builder());
	}

	@Test
	@UnitTestMethod(target = RunContinuityPlugin.Builder.class, name = "build", args = {})
	public void testBuild() {
		
		
		RunContinuityPluginData runContinuityPluginData = RunContinuityPluginData.builder().build();

		Plugin plugin = RunContinuityPlugin.builder()
				.setRunContinuityPluginData(runContinuityPluginData)
				.build();
		assertNotNull(plugin);
		assertEquals(RunContinuityPluginId.PLUGIN_ID, plugin.getPluginId());

		List<PluginData> pluginDatas = plugin.getPluginDatas();
		assertNotNull(pluginDatas);
		assertEquals(1,pluginDatas.size());
		PluginData pluginData = pluginDatas.get(0);
		assertTrue(pluginData==runContinuityPluginData);
		assertNotNull(plugin.getInitializer());

		//precondition test: if no plugin data was included
		ContractException contractException = assertThrows(ContractException.class, ()->RunContinuityPlugin.builder().build());
		assertEquals(RunContinuityError.NULL_RUN_CONTINUITY_PLUGN_DATA,contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RunContinuityPlugin.Builder.class, name = "setRunContinuityPluginData", args = {RunContinuityPluginData.class}, tags= {UnitTag.LOCAL_PROXY})
	public void testSetRunContinuityPluginData() {
		//covered by test of RunContinuityPlugin.Builder.build()
	}
}
