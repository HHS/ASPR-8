package plugins.groups;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import plugins.groups.initialdata.GroupInitialData;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.properties.PropertiesPlugin;
import plugins.reports.ReportPlugin;
import plugins.stochastics.StochasticsPlugin;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupPlugin.class)
public class AT_GroupPlugin {

	@Test
	@UnitTestConstructor(args = { GroupInitialData.class })
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
		new GroupPlugin(GroupInitialData.builder().build()).init(mockPluginContext);

		// show that the documented plugin dependencies were added

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		
		expectedDependencies.add(PartitionsPlugin.PLUGIN_ID);
		expectedDependencies.add(PeoplePlugin.PLUGIN_ID);
		expectedDependencies.add(PropertiesPlugin.PLUGIN_ID);
		expectedDependencies.add(ReportPlugin.PLUGIN_ID);
		expectedDependencies.add(StochasticsPlugin.PLUGIN_ID);

		assertEquals(expectedDependencies, mockPluginContext.getPluginDependencies());

		/*
		 * Show that a single Resolver was added to the mock plugin context. There is
		 * no good way to prove it is the correct one, but if it were wrong,
		 * then the tests of event resolution in this plugin would fail.
		 */
		Map<ResolverId, Consumer<DataManagerContext>> resolverMap = mockPluginContext.getResolverMap();
		assertEquals(1, resolverMap.size());

	}

}
