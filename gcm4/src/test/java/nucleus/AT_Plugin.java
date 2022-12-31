package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_Plugin {

	private static final class XPluginData implements PluginData {

		@Override
		public PluginDataBuilder getCloneBuilder() {
			return null;
		}

	}

	private static enum PluginIds implements PluginId {
		PLUGIN_ID_1, PLUGIN_ID_2, PLUGIN_ID_3,
	}

	@Test
	@UnitTestMethod(target = Plugin.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(Plugin.builder());
	}

	@Test
	@UnitTestMethod(target = Plugin.class, name = "getInitializer", args = {})
	public void testGetInitializer() {
		Plugin plugin = Plugin	.builder()//
								.setPluginId(PluginIds.PLUGIN_ID_1)//
								.build();//

		assertFalse(plugin.getInitializer().isPresent());

		Consumer<PluginContext> initializer = (c) -> {
		};

		plugin = Plugin	.builder()//
						.setPluginId(PluginIds.PLUGIN_ID_1)//
						.setInitializer(initializer)//
						.build();//

		assertTrue(plugin.getInitializer().isPresent());
		assertEquals(initializer, plugin.getInitializer().get());

	}

	@Test
	@UnitTestMethod(target = Plugin.class, name = "getPluginDatas", args = {})
	public void testGetPluginDatas() {
		Plugin plugin = Plugin	.builder()//
								.setPluginId(PluginIds.PLUGIN_ID_1)//
								.build();//

		assertTrue(plugin.getPluginDatas().isEmpty());

		XPluginData xPluginData1 = new XPluginData();
		XPluginData xPluginData2 = new XPluginData();
		Set<PluginData> expectedPluginDatas = new LinkedHashSet<>();
		expectedPluginDatas.add(xPluginData1);
		expectedPluginDatas.add(xPluginData2);

		plugin = Plugin	.builder()//
						.setPluginId(PluginIds.PLUGIN_ID_1)//
						.addPluginData(xPluginData1)//
						.addPluginData(xPluginData2)//
						.build();//

		assertEquals(expectedPluginDatas, plugin.getPluginDatas());
	}

	@Test
	@UnitTestMethod(target = Plugin.class, name = "getPluginDependencies", args = {})
	public void testGetPluginDependencies() {
		Plugin plugin = Plugin	.builder()//
								.setPluginId(PluginIds.PLUGIN_ID_1)//
								.build();//

		assertTrue(plugin.getPluginDependencies().isEmpty());

		Set<PluginId> expectedPluginIds = new LinkedHashSet<>();
		expectedPluginIds.add(PluginIds.PLUGIN_ID_2);
		expectedPluginIds.add(PluginIds.PLUGIN_ID_3);

		plugin = Plugin	.builder()//
						.setPluginId(PluginIds.PLUGIN_ID_1)//
						.addPluginDependency(PluginIds.PLUGIN_ID_2)//
						.addPluginDependency(PluginIds.PLUGIN_ID_3)//
						.build();//

		assertEquals(expectedPluginIds, plugin.getPluginDependencies());
	}

	@Test
	@UnitTestMethod(target = Plugin.class, name = "getPluginId", args = {})
	public void testGetPluginId() {

		Plugin plugin = Plugin	.builder()//
								.setPluginId(PluginIds.PLUGIN_ID_1)//
								.build();//

		assertEquals(PluginIds.PLUGIN_ID_1, plugin.getPluginId());

		plugin = Plugin	.builder()//
						.setPluginId(PluginIds.PLUGIN_ID_2)//
						.build();//

		assertEquals(PluginIds.PLUGIN_ID_2, plugin.getPluginId());

	}

	@Test
	@UnitTestMethod(target = Plugin.Builder.class, name = "addPluginData", args = { PluginData.class })
	public void testAddPluginData() {

		XPluginData xPluginData1 = new XPluginData();
		XPluginData xPluginData2 = new XPluginData();
		Set<PluginData> expectedPluginDatas = new LinkedHashSet<>();
		expectedPluginDatas.add(xPluginData1);
		expectedPluginDatas.add(xPluginData2);

		Plugin plugin = Plugin	.builder()//
								.setPluginId(PluginIds.PLUGIN_ID_1)//
								.addPluginData(xPluginData1)//
								.addPluginData(xPluginData2)//
								.build();//

		assertEquals(expectedPluginDatas, plugin.getPluginDatas());

		// precondition test: if the plugin data is null
		ContractException contractException = assertThrows(ContractException.class, () -> Plugin.builder().addPluginData(null));
		assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = Plugin.Builder.class, name = "addPluginDependency", args = { PluginId.class })
	public void testAddPluginDependency() {

		Set<PluginId> expectedPluginIds = new LinkedHashSet<>();
		expectedPluginIds.add(PluginIds.PLUGIN_ID_2);
		expectedPluginIds.add(PluginIds.PLUGIN_ID_3);

		Plugin plugin = Plugin	.builder()//
								.setPluginId(PluginIds.PLUGIN_ID_1)//
								.addPluginDependency(PluginIds.PLUGIN_ID_2)//
								.addPluginDependency(PluginIds.PLUGIN_ID_3)//
								.build();//

		assertEquals(expectedPluginIds, plugin.getPluginDependencies());

		// precondition test: if a plugin dependency is null
		ContractException contractException = assertThrows(ContractException.class, () -> Plugin.builder().addPluginDependency(null));
		assertEquals(NucleusError.NULL_PLUGIN_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = Plugin.Builder.class, name = "build", args = {})
	public void testBuild() {

		Plugin plugin = Plugin.builder().setPluginId(PluginIds.PLUGIN_ID_1).build();
		assertNotNull(plugin);

		// precondition test: if the plugin id was not set
		ContractException contractException = assertThrows(ContractException.class, () -> Plugin.builder().build());
		assertEquals(NucleusError.NULL_PLUGIN_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = Plugin.Builder.class, name = "setInitializer", args = { Consumer.class })
	public void testSetInitializer() {

		Consumer<PluginContext> initializer = (c) -> {
		};

		Plugin plugin = Plugin	.builder()//
								.setPluginId(PluginIds.PLUGIN_ID_1)//
								.setInitializer(initializer)//
								.build();//

		assertTrue(plugin.getInitializer().isPresent());
		assertEquals(initializer, plugin.getInitializer().get());

		// precondition test: if the initializer is null
		ContractException contractException = assertThrows(ContractException.class, () -> Plugin.builder().setInitializer(null));
		assertEquals(NucleusError.NULL_PLUGIN_INITIALIZER, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = Plugin.Builder.class, name = "setPluginId", args = { PluginId.class })
	public void testSetPluginId() {
		Plugin plugin = Plugin	.builder()//
								.setPluginId(PluginIds.PLUGIN_ID_1)//
								.build();//

		assertEquals(PluginIds.PLUGIN_ID_1, plugin.getPluginId());

		plugin = Plugin	.builder()//
						.setPluginId(PluginIds.PLUGIN_ID_2)//
						.build();//

		assertEquals(PluginIds.PLUGIN_ID_2, plugin.getPluginId());

		// precondition test: if the plugin id is null
		assertThrows(ContractException.class, () -> Plugin.builder().setPluginId(null));

	}

}
