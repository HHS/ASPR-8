package plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.PluginContext;
import nucleus.PluginId;
import nucleus.ResolverContext;
import nucleus.ResolverId;
import nucleus.testsupport.MockPluginContext;
import plugins.components.ComponentPlugin;
import plugins.people.PeoplePlugin;
import plugins.stochastics.StochasticsPlugin;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PartitionsPlugin.class)
public final class AT_PartitionsPlugin {

	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "init", args = { PluginContext.class })
	public void testInit() {

		// Create a mock plugin context
		MockPluginContext mockPluginContext = new MockPluginContext();

		/*
		 * Create a partitions plugin and show that it adds the property
		 * content to the plugin context
		 */
		new PartitionsPlugin().init(mockPluginContext);

		// show that the documented plugin dependencies were added

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(ComponentPlugin.PLUGIN_ID);
		expectedDependencies.add(PeoplePlugin.PLUGIN_ID);
		expectedDependencies.add(StochasticsPlugin.PLUGIN_ID);		

		assertEquals(expectedDependencies, mockPluginContext.getPluginDependencies());

		/*
		 * Show that a single Resolver was added to the mock plugin context.
		 * There is no good way to prove it is the correct one, but if it were
		 * wrong, then the tests of event resolution in this plugin would fail.
		 */
		Map<ResolverId, Consumer<ResolverContext>> resolverMap = mockPluginContext.getResolverMap();
		assertEquals(1, resolverMap.size());
	}

}
