package plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.PluginContext;
import nucleus.DataManagerContext;
import nucleus.ResolverId;
import nucleus.testsupport.MockPluginContext;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.people.initialdata.PeopleInitialData;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PeoplePlugin.class)
public class AT_PeoplePlugin {

	@Test
	@UnitTestConstructor(args = { CompartmentInitialData.class })
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
		new PeoplePlugin(PeopleInitialData.builder().build()).init(mockPluginContext);

		// show that the documented plugin has not dependencies		
		assertTrue(mockPluginContext.getPluginDependencies().isEmpty());

		/*
		 * Show that a single Resolver was added to the mock plugin context. There is
		 * no good way to prove it is the correct one, but if it were wrong,
		 * then the tests of event resolution in this plugin would fail.
		 */
		Map<ResolverId, Consumer<DataManagerContext>> resolverMap = mockPluginContext.getResolverMap();
		assertEquals(1, resolverMap.size());

	}

}
