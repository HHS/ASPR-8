package plugins.globals;

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
import plugins.globals.initialdata.GlobalInitialData;
import plugins.properties.PropertiesPlugin;
import plugins.reports.ReportPlugin;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GlobalPlugin.class)
public class AT_GlobalPlugin {

	@Test
	@UnitTestConstructor(args = { GlobalInitialData.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "init", args = { PluginContext.class })
	public void testInit() {

		// Create a mock plugin context
		MockPluginContext mockPluginContext = new MockPluginContext();

		/*
		 * Create a compartment plugin and show that it adds the property
		 * content to the plugin context
		 */
		new GlobalPlugin(GlobalInitialData.builder().build()).init(mockPluginContext);

		// show that the documented plugin dependencies were added

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(ComponentPlugin.PLUGIN_ID);
		expectedDependencies.add(PropertiesPlugin.PLUGIN_ID);
		expectedDependencies.add(ReportPlugin.PLUGIN_ID);

		assertEquals(expectedDependencies, mockPluginContext.getPluginDependencies());

		/*
		 * Show that a single Resolver was added to the mock plugin context. There is
		 * no good way to prove it is the correct one, but if it were wrong,
		 * then the tests of event resolution in this plugin would fail.
		 */
		Map<ResolverId, Consumer<ResolverContext>> resolverMap = mockPluginContext.getResolverMap();
		assertEquals(1, resolverMap.size());

	}

}
