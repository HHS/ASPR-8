package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_Plugin {

	private static final class XPluginData implements PluginData {

		@Override
		public PluginDataBuilder toBuilder() {
			throw new UnsupportedOperationException();
		}

	}

	private static final class YPluginData implements PluginData {
		private final int value;

		public YPluginData(int value) {
			this.value = value;
		}

		@Override
		public PluginDataBuilder toBuilder() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			YPluginData other = (YPluginData) obj;
			return value == other.value;
		}

	}

	private static enum PluginIds implements PluginId {
		PLUGIN_ID_1, PLUGIN_ID_2, PLUGIN_ID_3, PLUGIN_ID_4, PLUGIN_ID_5;

		public static PluginIds getRandomPluginId(RandomGenerator randomGenerator) {
			int index = randomGenerator.nextInt(PluginIds.values().length);
			return PluginIds.values()[index];
		}

		public static Set<PluginIds> getRandomPluginIds(RandomGenerator randomGenerator) {
			Set<PluginIds> result = new LinkedHashSet<>();
			for (PluginIds pluginIds : PluginIds.values()) {
				if (randomGenerator.nextBoolean()) {
					result.add(pluginIds);
				}
			}
			return result;
		}
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

		assertEquals(expectedPluginDatas, new LinkedHashSet<>(plugin.getPluginDatas()));
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

		assertEquals(expectedPluginDatas, new LinkedHashSet<>(plugin.getPluginDatas()));

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

	@UnitTestMethod(target = Plugin.class, name = "hashCode", args = {})
	@Test
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1667890710500097680L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			Plugin plugin1 = getRandomPlugin(seed);
			Plugin plugin2 = getRandomPlugin(seed);

			assertEquals(plugin1, plugin2);
			assertEquals(plugin1.hashCode(), plugin2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			Plugin plugin = getRandomPlugin(randomGenerator.nextLong());
			hashCodes.add(plugin.hashCode());
		}

		assertEquals(100, hashCodes.size());

		// The order in which plugindatas are added does not matter
		Plugin.Builder builder1 = Plugin.builder();
		Plugin.Builder builder2 = Plugin.builder();

		builder1.addPluginData(new YPluginData(5));
		builder1.addPluginData(new YPluginData(9));
		builder2.addPluginData(new YPluginData(9));
		builder2.addPluginData(new YPluginData(5));

		PluginIds pluginId = PluginIds.getRandomPluginId(randomGenerator);
		builder1.setPluginId(pluginId);
		builder2.setPluginId(pluginId);

		Set<PluginIds> randomPluginIds = PluginIds.getRandomPluginIds(randomGenerator);
		for (PluginIds pluginIds : randomPluginIds) {
			if (pluginIds != pluginId) {
				builder1.addPluginDependency(pluginIds);
				builder2.addPluginDependency(pluginIds);
			}
		}

		Plugin plugin1 = builder1.build();
		Plugin plugin2 = builder2.build();

		assertEquals(plugin1, plugin2);
		assertEquals(plugin1.hashCode(), plugin2.hashCode());
	}

	private Plugin getRandomPlugin(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		Plugin.Builder builder = Plugin.builder();

		Set<PluginData> pluginDatas = new LinkedHashSet<>();
		int pluginDataCount = randomGenerator.nextInt(5) + 1;
		while (pluginDatas.size() < pluginDataCount) {
			pluginDatas.add(new YPluginData(randomGenerator.nextInt(1000)));
		}

		for (PluginData pluginData : pluginDatas) {
			builder.addPluginData(pluginData);
		}

		PluginIds pluginId = PluginIds.getRandomPluginId(randomGenerator);
		builder.setPluginId(pluginId);

		Set<PluginIds> randomPluginIds = PluginIds.getRandomPluginIds(randomGenerator);
		for (PluginIds pluginIds : randomPluginIds) {
			if (pluginIds != pluginId) {
				builder.addPluginDependency(pluginIds);
			}
		}

		builder.setInitializer((c) -> {
		});

		return builder.build();
	}

	@UnitTestMethod(target = Plugin.class, name = "equals", args = { Object.class })
	@Test
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1276670681120545443L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			Plugin plugin = getRandomPlugin(randomGenerator.nextLong());
			assertFalse(plugin.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			Plugin plugin = getRandomPlugin(randomGenerator.nextLong());
			assertFalse(plugin.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			Plugin plugin = getRandomPlugin(randomGenerator.nextLong());
			assertTrue(plugin.equals(plugin));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			Plugin plugin1 = getRandomPlugin(seed);
			Plugin plugin2 = getRandomPlugin(seed);
			assertFalse(plugin1 == plugin2);
			for (int j = 0; j < 10; j++) {
				assertTrue(plugin1.equals(plugin2));
				assertTrue(plugin2.equals(plugin1));
			}
		}

		// different inputs yield unequal plugins
		Set<Plugin> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			Plugin plugin = getRandomPlugin(randomGenerator.nextLong());
			set.add(plugin);
		}
		assertEquals(100, set.size());

		// The order in which plugindatas are added does not matter
		Plugin.Builder builder1 = Plugin.builder();
		Plugin.Builder builder2 = Plugin.builder();

		builder1.addPluginData(new YPluginData(5));
		builder1.addPluginData(new YPluginData(9));
		builder2.addPluginData(new YPluginData(9));
		builder2.addPluginData(new YPluginData(5));

		PluginIds pluginId = PluginIds.getRandomPluginId(randomGenerator);
		builder1.setPluginId(pluginId);
		builder2.setPluginId(pluginId);

		Set<PluginIds> randomPluginIds = PluginIds.getRandomPluginIds(randomGenerator);
		for (PluginIds pluginIds : randomPluginIds) {
			if (pluginIds != pluginId) {
				builder1.addPluginDependency(pluginIds);
				builder2.addPluginDependency(pluginIds);
			}
		}

		Plugin plugin1 = builder1.build();
		Plugin plugin2 = builder2.build();

		assertFalse(plugin1 == plugin2);
		assertTrue(plugin1.equals(plugin2));
		assertTrue(plugin2.equals(plugin1));
	}

}
