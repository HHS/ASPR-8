package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_PluginDataBuilderContext {

	private static class PluginDataBuilder1 implements PluginDataBuilder {
		@Override
		public PluginData build() {
			return null;
		}
	};

	private static class PluginDataBuilder2 implements PluginDataBuilder {
		@Override
		public PluginData build() {
			return null;
		}
	};

	private static class PluginDataBuilder3 implements PluginDataBuilder {
		@Override
		public PluginData build() {
			return null;
		}
	};

	@Test
	@UnitTestMethod(target = PluginDataBuilderContext.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PluginDataBuilderContext.builder());
	}

	@Test
	@UnitTestMethod(target = PluginDataBuilderContext.class, name = "get", args = { Class.class })
	public void testGet() {
		PluginDataBuilder p1 = new PluginDataBuilder1();

		PluginDataBuilder p2 = new PluginDataBuilder2();

		PluginDataBuilder p3 = new PluginDataBuilder1();

		PluginDataBuilder p4 = new PluginDataBuilder2();

		PluginDataBuilderContext pluginDataBuilderContext = PluginDataBuilderContext.builder()//
																					.add(p1)//
																					.add(p2)//
																					.add(p3)//
																					.add(p4)//
																					.build();

		PluginDataBuilder p = pluginDataBuilderContext.get(PluginDataBuilder1.class);
		assertEquals(p3, p);

		p = pluginDataBuilderContext.get(PluginDataBuilder2.class);
		assertEquals(p4, p);

		/*
		 * precondition test : if more than one plugin data builder matches the
		 * given class reference
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> pluginDataBuilderContext.get(PluginDataBuilder.class));
		assertEquals(NucleusError.AMBIGUOUS_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());

		/*
		 * precondition test : if no plugin data builder matches the given class
		 * reference
		 */
		contractException = assertThrows(ContractException.class, () -> pluginDataBuilderContext.get(PluginDataBuilder3.class));
		assertEquals(NucleusError.UNKNOWN_PLUGIN_DATA_BUILDER_CLASS, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PluginDataBuilderContext.class, name = "getContents", args = {})
	public void testGetContents() {
		PluginDataBuilder p1 = new PluginDataBuilder1();

		PluginDataBuilder p2 = new PluginDataBuilder2();

		PluginDataBuilder p3 = new PluginDataBuilder1();

		PluginDataBuilder p4 = new PluginDataBuilder2();

		Set<PluginDataBuilder> expectedContents = new LinkedHashSet<>();
		expectedContents.add(p3);
		expectedContents.add(p4);

		PluginDataBuilderContext pluginDataBuilderContext = PluginDataBuilderContext.builder()//
																					.add(p1)//
																					.add(p2)//
																					.add(p3)//
																					.add(p4)//
																					.build();
		Set<PluginDataBuilder> actualContents = pluginDataBuilderContext.getContents();

		assertEquals(expectedContents, actualContents);

	}

	@Test
	@UnitTestMethod(target = PluginDataBuilderContext.Builder.class, name = "add", args = { PluginDataBuilder.class })
	public void testAdd() {
		PluginDataBuilder p1 = new PluginDataBuilder1();

		PluginDataBuilder p2 = new PluginDataBuilder2();

		Set<PluginDataBuilder> expectedContents = new LinkedHashSet<>();
		expectedContents.add(p1);
		expectedContents.add(p2);

		PluginDataBuilderContext pluginDataBuilderContext = PluginDataBuilderContext.builder().add(p1).add(p2).build();
		Set<PluginDataBuilder> actualContents = pluginDataBuilderContext.getContents();

		assertEquals(expectedContents, actualContents);

		ContractException contractException = assertThrows(ContractException.class, () -> PluginDataBuilderContext.builder().add(null));
		assertEquals(NucleusError.NULL_PLUGIN_DATA_BUILDER, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PluginDataBuilderContext.Builder.class, name = "build", args = {})
	public void testBuild() {
		PluginDataBuilder p1 = new PluginDataBuilder1();

		PluginDataBuilder p2 = new PluginDataBuilder2();

		Set<PluginDataBuilder> expectedContents = new LinkedHashSet<>();
		expectedContents.add(p1);
		expectedContents.add(p2);

		PluginDataBuilderContext pluginDataBuilderContext = PluginDataBuilderContext.builder().add(p1).add(p2).build();
		Set<PluginDataBuilder> actualContents = pluginDataBuilderContext.getContents();

		assertEquals(expectedContents, actualContents);

	}

}
