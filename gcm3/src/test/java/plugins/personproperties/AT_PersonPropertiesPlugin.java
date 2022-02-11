package plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import plugins.compartments.CompartmentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.properties.PropertiesPlugin;
import plugins.regions.RegionPlugin;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertiesPlugin.class)
public class AT_PersonPropertiesPlugin {

	@Test
	@UnitTestConstructor(args = { PersonPropertyInitialData.class })
	public void testConstructor() {
		assertThrows(ContractException.class, () -> new PersonPropertiesPlugin(null));
	}

	@Test
	@UnitTestMethod(name = "init", args = { PluginContext.class })
	public void testInit() {

		// Create a mock plugin context
		MockPluginContext mockPluginContext = new MockPluginContext();

		/*
		 * Create a person properties plugin
		 */
		new PersonPropertiesPlugin(PersonPropertyInitialData.builder().build()).init(mockPluginContext);

		// show that the documented plugin dependencies were added

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();

		expectedDependencies.add(PartitionsPlugin.PLUGIN_ID);
		expectedDependencies.add(PeoplePlugin.PLUGIN_ID);
		expectedDependencies.add(PropertiesPlugin.PLUGIN_ID);
		expectedDependencies.add(CompartmentPlugin.PLUGIN_ID);
		expectedDependencies.add(RegionPlugin.PLUGIN_ID);
		

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
