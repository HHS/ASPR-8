package gov.hhs.aspr.ms.gcm.nucleus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;
import util.errors.ContractException;

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
		public PluginDataBuilder getCloneBuilder() {
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
		public PluginDataBuilder getCloneBuilder() {
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
		public PluginDataBuilder getCloneBuilder() {
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
		PluginData p1 = new PluginData1();

		PluginData p2 = new PluginData2();

		PluginData p3 = new PluginData1();

		PluginData p4 = new PluginData2();

		DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

		dimensionContextBuilder.add(p1);
		dimensionContextBuilder.add(p2);
		dimensionContextBuilder.add(p3);
		dimensionContextBuilder.add(p4);

		DimensionContext dimensionContext = dimensionContextBuilder.build();

		PluginDataBuilder p = dimensionContext.getPluginDataBuilder(PluginData1.Builder.class);
		assertEquals(p3.getCloneBuilder(), p);

		p = dimensionContext.getPluginDataBuilder(PluginData2.Builder.class);
		assertEquals(p4.getCloneBuilder(), p);

		/*
		 * precondition test : if more than one plugin data builder matches the
		 * given class reference
		 */
		ContractException contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginDataBuilder(PluginDataBuilder.class));
		assertEquals(NucleusError.AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());

		/*
		 * precondition test : if no plugin data builder matches the given class
		 * reference
		 */
		contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginDataBuilder(PluginData3.Builder.class));
		assertEquals(NucleusError.UNKNOWN_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = DimensionContext.class, name = "getPluginData", args = { Class.class })
	public void testGetPluginData() {
		PluginData p1 = new PluginData1();

		PluginData p2 = new PluginData2();

		PluginData p3 = new PluginData1();

		PluginData p4 = new PluginData2();

		DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

		dimensionContextBuilder.add(p1);
		dimensionContextBuilder.add(p2);
		dimensionContextBuilder.add(p3);
		dimensionContextBuilder.add(p4);

		DimensionContext dimensionContext = dimensionContextBuilder.build();

		PluginData p = dimensionContext.getPluginData(PluginData1.class);
		assertEquals(p3, p);

		p = dimensionContext.getPluginData(PluginData2.class);
		assertEquals(p4, p);

		/*
		 * precondition test : if more than one plugin data builder matches the
		 * given class reference
		 */
		ContractException contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginData(PluginData.class));
		assertEquals(NucleusError.AMBIGUOUS_PLUGIN_DATA_CLASS, contractException.getErrorType());

		/*
		 * precondition test : if no plugin data builder matches the given class
		 * reference
		 */
		contractException = assertThrows(ContractException.class,
				() -> dimensionContext.getPluginData(PluginData3.class));
		assertEquals(NucleusError.UNKNOWN_PLUGIN_DATA_CLASS, contractException.getErrorType());

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

		assertEquals(p1.getCloneBuilder(), p1b);
		assertEquals(p2.getCloneBuilder(), p2b);

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

		assertEquals(p1.getCloneBuilder(), p1b);
		assertEquals(p2.getCloneBuilder(), p2b);

		DimensionContext dimensionContext = builder.build();

		Set<PluginData> actualContents = new LinkedHashSet<>();
		assertDoesNotThrow(() -> actualContents.add(dimensionContext.getPluginData(PluginData1.class)));
		assertDoesNotThrow(() -> actualContents.add(dimensionContext.getPluginData(PluginData2.class)));

		assertEquals(expectedContents, actualContents);

	}
}
