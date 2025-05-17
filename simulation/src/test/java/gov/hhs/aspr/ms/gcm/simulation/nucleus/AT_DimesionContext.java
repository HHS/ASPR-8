package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_DimesionContext {

	private static class PluginData1 implements PluginData {

		private static class Builder implements PluginDataBuilder {

			@Override
			public PluginData build() {
				return null;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (!(obj instanceof Builder)) {
					return false;
				}

				return true;
			}
		}

		@Override
		public PluginDataBuilder toBuilder() {
			return new Builder();
		}
	}

	private static class PluginData2 implements PluginData {

		private static class Builder implements PluginDataBuilder {

			@Override
			public PluginData build() {
				return null;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (!(obj instanceof Builder)) {
					return false;
				}

				return true;
			}
		}

		@Override
		public PluginDataBuilder toBuilder() {
			return new Builder();
		}
	}

	private static class PluginData3 implements PluginData {

		private static class Builder implements PluginDataBuilder {

			@Override
			public PluginData build() {
				return null;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (!(obj instanceof Builder)) {
					return false;
				}

				return true;
			}
		}

		@Override
		public PluginDataBuilder toBuilder() {
			return new Builder();
		}
	}

	private static class PluginData4 implements PluginData {

		private static class Builder implements PluginDataBuilder {

			@Override
			public PluginData build() {
				return null;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (!(obj instanceof Builder)) {
					return false;
				}

				return true;
			}
		}

		@Override
		public PluginDataBuilder toBuilder() {
			return new Builder();
		}
	}

	@Test
	@UnitTestMethod(target = DimensionContext.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(DimensionContext.builder());
	}

	@Test
	@UnitTestMethod(target = DimensionContext.class, name = "getPluginDataBuilder", args = { Class.class })
	public void testGetPluginDataBuilder() {
		// Note that p2 and p4 are both of type2 and that there is no type4 instance
		PluginData p1 = new PluginData1();

		PluginData p2 = new PluginData2();

		PluginData p3 = new PluginData3();

		PluginData p4 = new PluginData2();

		DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

		dimensionContextBuilder.add(p1);
		dimensionContextBuilder.add(p2);
		dimensionContextBuilder.add(p3);
		dimensionContextBuilder.add(p4);

		DimensionContext dimensionContext = dimensionContextBuilder.build();

		// There should be exactly one type one
		PluginDataBuilder p = dimensionContext.getPluginDataBuilder(PluginData1.Builder.class);
		assertEquals(p1.toBuilder(), p);

		// There should be exactly one type three
		p = dimensionContext.getPluginDataBuilder(PluginData3.Builder.class);
		assertEquals(p3.toBuilder(), p);

		/*
		 * precondition test : if the class reference is null
		 */
		ContractException contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginDataBuilder(null));
		assertEquals(NucleusError.NULL_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());

		/*
		 * precondition test : if more than one plugin data builder matches the given
		 * class reference
		 */

		// there are 4 instances matching PluginData.Builder
		contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginDataBuilder(PluginDataBuilder.class));
		assertEquals(NucleusError.AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());

		// there are 2 instances matching PluginData2
		contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginDataBuilder(PluginData2.Builder.class));
		assertEquals(NucleusError.AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());

		/*
		 * precondition test : if no plugin data builder matches the given class
		 * reference
		 */
		// there are 0 instances matching PluginData4
		contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginDataBuilder(PluginData4.Builder.class));
		assertEquals(NucleusError.UNKNOWN_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = DimensionContext.class, name = "getPluginDataBuilders", args = { Class.class })
	public void testGetPluginDataBuilders() {

		// Note that p2 and p4 are both of type2 and that there is no type4 instance
		PluginData p1 = new PluginData1();

		PluginData p2 = new PluginData2();

		PluginData p3 = new PluginData3();

		PluginData p4 = new PluginData2();

		DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

		dimensionContextBuilder.add(p1);
		dimensionContextBuilder.add(p2);
		dimensionContextBuilder.add(p3);
		dimensionContextBuilder.add(p4);

		DimensionContext dimensionContext = dimensionContextBuilder.build();

		/*
		 * In the assertions that follow, we are guaranteed that two builders of the
		 * same type will be equal due to the implementation in the local plugin
		 * classes.
		 */

		// There should be exactly one type1 builder
		List<PluginData1.Builder> pluginData1Builders = dimensionContext
				.getPluginDataBuilders(PluginData1.Builder.class);
		assertNotNull(pluginData1Builders);
		assertEquals(1, pluginData1Builders.size());
		assertEquals(p1.toBuilder(), pluginData1Builders.get(0));

		// There should be exactly one type3 builder
		List<PluginData3.Builder> pluginData3Builders = dimensionContext.getPluginDataBuilders(PluginData3.Builder.class);
		assertNotNull(pluginData3Builders);
		assertEquals(1, pluginData3Builders.size());
		assertEquals(p3.toBuilder(), pluginData3Builders.get(0));

		// There should be exactly two type2 builders
		List<PluginData2.Builder> pluginData2Builders = dimensionContext.getPluginDataBuilders(PluginData2.Builder.class);
		assertNotNull(pluginData2Builders);
		assertEquals(2, pluginData2Builders.size());
		assertTrue(pluginData2Builders.contains(p2.toBuilder()));
		assertTrue(pluginData2Builders.contains(p4.toBuilder()));

		// There should be exactly four PluginDatas
		List<PluginDataBuilder> pluginDataBuilders = dimensionContext.getPluginDataBuilders(PluginDataBuilder.class);
		assertNotNull(pluginDataBuilders);
		assertEquals(4, pluginDataBuilders.size());
		assertTrue(pluginDataBuilders.contains(p1.toBuilder()));
		assertTrue(pluginDataBuilders.contains(p2.toBuilder()));
		assertTrue(pluginDataBuilders.contains(p3.toBuilder()));
		assertTrue(pluginDataBuilders.contains(p4.toBuilder()));

		// There should be exactly zero type4s
		List<PluginData4.Builder> pluginData4Builders = dimensionContext.getPluginDataBuilders(PluginData4.Builder.class);
		assertNotNull(pluginData4Builders);
		assertEquals(0, pluginData4Builders.size());

		/*
		 * precondition test : if the class reference is null
		 */
		ContractException contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginDataBuilders(null));
		assertEquals(NucleusError.NULL_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = DimensionContext.class, name = "getPluginData", args = { Class.class })
	public void testGetPluginData() {

		// Note that p2 and p4 are both of type2 and that there is no type4 instance
		PluginData p1 = new PluginData1();

		PluginData p2 = new PluginData2();

		PluginData p3 = new PluginData3();

		PluginData p4 = new PluginData2();

		DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

		dimensionContextBuilder.add(p1);
		dimensionContextBuilder.add(p2);
		dimensionContextBuilder.add(p3);
		dimensionContextBuilder.add(p4);

		DimensionContext dimensionContext = dimensionContextBuilder.build();

		// There should be exactly one type one
		PluginData p = dimensionContext.getPluginData(PluginData1.class);
		assertEquals(p1, p);

		// There should be exactly one type three
		p = dimensionContext.getPluginData(PluginData3.class);
		assertEquals(p3, p);

		/*
		 * precondition test : if the class reference is null
		 */
		ContractException contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginData(null));
		assertEquals(NucleusError.NULL_PLUGIN_DATA_CLASS, contractException.getErrorType());

		/*
		 * precondition test : if more than one plugin data builder matches the given
		 * class reference
		 */

		// there are 4 instances matching PluginData
		contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginData(PluginData.class));
		assertEquals(NucleusError.AMBIGUOUS_PLUGIN_DATA_CLASS, contractException.getErrorType());

		// there are 2 instances matching PluginData2
		contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginData(PluginData2.class));
		assertEquals(NucleusError.AMBIGUOUS_PLUGIN_DATA_CLASS, contractException.getErrorType());

		/*
		 * precondition test : if no plugin data builder matches the given class
		 * reference
		 */
		// there are 0 instances matching PluginData4
		contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginData(PluginData4.class));
		assertEquals(NucleusError.UNKNOWN_PLUGIN_DATA_CLASS, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = DimensionContext.class, name = "getPluginDatas", args = { Class.class })
	public void testGetPluginDatas() {

		// Note that p2 and p4 are both of type2 and that there is no type4 instance
		PluginData p1 = new PluginData1();

		PluginData p2 = new PluginData2();

		PluginData p3 = new PluginData3();

		PluginData p4 = new PluginData2();

		DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

		dimensionContextBuilder.add(p1);
		dimensionContextBuilder.add(p2);
		dimensionContextBuilder.add(p3);
		dimensionContextBuilder.add(p4);

		DimensionContext dimensionContext = dimensionContextBuilder.build();

		// There should be exactly one type1s
		List<PluginData1> pluginData1s = dimensionContext.getPluginDatas(PluginData1.class);
		assertNotNull(pluginData1s);
		assertEquals(1, pluginData1s.size());
		assertEquals(p1, pluginData1s.get(0));

		// There should be exactly one type3s
		List<PluginData3> pluginData3s = dimensionContext.getPluginDatas(PluginData3.class);
		assertNotNull(pluginData3s);
		assertEquals(1, pluginData3s.size());
		assertEquals(p3, pluginData3s.get(0));

		// There should be exactly two type2s
		List<PluginData2> pluginData2s = dimensionContext.getPluginDatas(PluginData2.class);
		assertNotNull(pluginData2s);
		assertEquals(2, pluginData2s.size());
		assertTrue(pluginData2s.contains(p2));
		assertTrue(pluginData2s.contains(p4));

		// There should be exactly four PluginDatas
		List<PluginData> pluginDatas = dimensionContext.getPluginDatas(PluginData.class);
		assertNotNull(pluginDatas);
		assertEquals(4, pluginDatas.size());
		assertTrue(pluginDatas.contains(p1));
		assertTrue(pluginDatas.contains(p2));
		assertTrue(pluginDatas.contains(p3));
		assertTrue(pluginDatas.contains(p4));

		// There should be exactly zero type4s
		List<PluginData4> pluginData4s = dimensionContext.getPluginDatas(PluginData4.class);
		assertNotNull(pluginData4s);
		assertEquals(0, pluginData4s.size());

		/*
		 * precondition test : if the class reference is null
		 */
		ContractException contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginDatas(null));
		assertEquals(NucleusError.NULL_PLUGIN_DATA_CLASS, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = DimensionContext.Builder.class, name = "add", args = { PluginData.class })
	public void testAdd() {
		PluginData p1 = new PluginData1();

		PluginData p2 = new PluginData2();

		Set<PluginData> expectedContents = new LinkedHashSet<>();
		expectedContents.add(p1);
		expectedContents.add(p2);

		DimensionContext.Builder builder = DimensionContext.builder();

		PluginDataBuilder p1b = builder.add(p1);
		PluginDataBuilder p2b = builder.add(p2);

		assertEquals(p1.toBuilder(), p1b);
		assertEquals(p2.toBuilder(), p2b);

		DimensionContext dimensionContext = builder.build();
		Set<PluginData> actualContents = new LinkedHashSet<>();
		assertDoesNotThrow(() -> actualContents.add(dimensionContext.getPluginData(PluginData1.class)));
		assertDoesNotThrow(() -> actualContents.add(dimensionContext.getPluginData(PluginData2.class)));

		assertEquals(expectedContents, actualContents);

		ContractException contractException = assertThrows(ContractException.class,
				() -> DimensionContext.builder().add(null));
		assertEquals(NucleusError.NULL_PLUGIN_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = DimensionContext.Builder.class, name = "build", args = {})
	public void testBuild() {
		PluginData p1 = new PluginData1();

		PluginData p2 = new PluginData2();

		Set<PluginData> expectedContents = new LinkedHashSet<>();
		expectedContents.add(p1);
		expectedContents.add(p2);

		DimensionContext.Builder builder = DimensionContext.builder();

		PluginDataBuilder p1b = builder.add(p1);
		PluginDataBuilder p2b = builder.add(p2);

		assertEquals(p1.toBuilder(), p1b);
		assertEquals(p2.toBuilder(), p2b);

		DimensionContext dimensionContext = builder.build();

		Set<PluginData> actualContents = new LinkedHashSet<>();
		assertDoesNotThrow(() -> actualContents.add(dimensionContext.getPluginData(PluginData1.class)));
		assertDoesNotThrow(() -> actualContents.add(dimensionContext.getPluginData(PluginData2.class)));

		assertEquals(expectedContents, actualContents);

	}
}
