package plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.DataManagerContext;
import nucleus.ResolverId;
import nucleus.testsupport.MockPluginContext;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.testsupport.attributes.initialdata.AttributeInitialData;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.people.PeoplePlugin;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = AttributesPlugin.class)
public class AT_AttributesPlugin {

	@Test
	@UnitTestConstructor(args = { AttributeInitialData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new AttributesPlugin(null));
		assertEquals(AttributeError.NULL_ATTRIBUTE_INITIAL_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "init", args = { PluginContext.class })
	public void testInit() {
		// Create a mock plugin context
		MockPluginContext mockPluginContext = new MockPluginContext();

		/*
		 * Create an attributes plugin 
		 */
		new AttributesPlugin(AttributeInitialData.builder().build()).init(mockPluginContext);

		// show that the documented plugin dependencies were added
		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PartitionsPlugin.PLUGIN_ID);
		expectedDependencies.add(PeoplePlugin.PLUGIN_ID);

		assertEquals(expectedDependencies, mockPluginContext.getPluginDependencies());

		/*
		 * Show that a single Resolver was added to the mock plugin context.
		 * There is no good way to prove it is the correct one, but if it were
		 * wrong, then the tests of event resolution in this plugin would fail.
		 */
		Map<ResolverId, Consumer<DataManagerContext>> resolverMap = mockPluginContext.getResolverMap();
		assertEquals(1, resolverMap.size());
	}

}
