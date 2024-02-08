package gov.hhs.aspr.ms.gcm.nucleus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_PluginDataBuilderContext {

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
	@UnitTestMethod(target = PluginDataBuilderContext.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PluginDataBuilderContext.builder());
	}

	@Test
	@UnitTestMethod(target = PluginDataBuilderContext.class, name = "getPluginDataBuilder", args = { Class.class })
	public void testGetPluginDataBuilder() {
		PluginDataBuilder p1 = new PluginData1.Builder();

		PluginDataBuilder p2 = new PluginData2.Builder();

		Set<PluginDataBuilder> expectedContents = new LinkedHashSet<>();
		expectedContents.add(p1);
		expectedContents.add(p2);

		PluginDataBuilderContext.Builder builder = PluginDataBuilderContext.builder();

		builder.add(p1);
		builder.add(p2);

		

		PluginDataBuilderContext pluginDataBuilderContext = builder.build();
		
		
		Set<PluginDataBuilder> actualContents = new LinkedHashSet<>();
		
		
		
		assertDoesNotThrow(() -> actualContents.add(pluginDataBuilderContext.getPluginDataBuilder(PluginData1.Builder.class)));
		assertDoesNotThrow(() -> actualContents.add(pluginDataBuilderContext.getPluginDataBuilder(PluginData2.Builder.class)));

		assertEquals(expectedContents, actualContents);

		/*
		 * precondition test : if more than one plugin data builder matches the
		 * given class reference
		 */
		ContractException contractException = assertThrows(ContractException.class,
				() -> pluginDataBuilderContext.getPluginDataBuilder(PluginDataBuilder.class));
		assertEquals(NucleusError.AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());

		/*
		 * precondition test : if no plugin data builder matches the given class
		 * reference
		 */
		contractException = assertThrows(ContractException.class,
				() -> pluginDataBuilderContext.getPluginDataBuilder(PluginData3.Builder.class));
		assertEquals(NucleusError.UNKNOWN_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());

	}

	
	@Test
	@UnitTestMethod(target = PluginDataBuilderContext.Builder.class, name = "add", args = { PluginDataBuilder.class })
	public void testAdd() {
		PluginDataBuilder p1 = new PluginData1.Builder();

		PluginDataBuilder p2 = new PluginData2.Builder();

		Set<PluginDataBuilder> expectedContents = new LinkedHashSet<>();
		expectedContents.add(p1);
		expectedContents.add(p2);

		PluginDataBuilderContext.Builder builder = PluginDataBuilderContext.builder();

		builder.add(p1);
		builder.add(p2);

		

		PluginDataBuilderContext pluginDataBuilderContext = builder.build();
		
		
		Set<PluginDataBuilder> actualContents = new LinkedHashSet<>();
		
		
		
		assertDoesNotThrow(() -> actualContents.add(pluginDataBuilderContext.getPluginDataBuilder(PluginData1.Builder.class)));
		assertDoesNotThrow(() -> actualContents.add(pluginDataBuilderContext.getPluginDataBuilder(PluginData2.Builder.class)));

		assertEquals(expectedContents, actualContents);

		ContractException contractException = assertThrows(ContractException.class,
				() -> PluginDataBuilderContext.builder().add(null));
		assertEquals(NucleusError.NULL_PLUGIN_DATA_BUILDER, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PluginDataBuilderContext.Builder.class, name = "build", args = {})
	public void testBuild() {
		PluginDataBuilder p1 = new PluginData1.Builder();

		PluginDataBuilder p2 = new PluginData2.Builder();

		Set<PluginDataBuilder> expectedContents = new LinkedHashSet<>();
		expectedContents.add(p1);
		expectedContents.add(p2);

		PluginDataBuilderContext.Builder builder = PluginDataBuilderContext.builder();

		builder.add(p1);
		builder.add(p2);

		

		PluginDataBuilderContext pluginDataBuilderContext = builder.build();
		
		
		Set<PluginDataBuilder> actualContents = new LinkedHashSet<>();
		
		
		
		assertDoesNotThrow(() -> actualContents.add(pluginDataBuilderContext.getPluginDataBuilder(PluginData1.Builder.class)));
		assertDoesNotThrow(() -> actualContents.add(pluginDataBuilderContext.getPluginDataBuilder(PluginData2.Builder.class)));

		assertEquals(expectedContents, actualContents);

	}
}
