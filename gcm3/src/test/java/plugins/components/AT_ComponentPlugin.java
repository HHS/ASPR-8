package plugins.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.PluginContext;
import nucleus.ResolverContext;
import nucleus.ResolverId;
import nucleus.testsupport.MockPluginContext;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ComponentPlugin.class)
public class AT_ComponentPlugin {

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
		 * Create a component plugin
		 */
		new ComponentPlugin().init(mockPluginContext);
		

		// show that there are no plugin dependencies
		assertTrue(mockPluginContext.getPluginDependencies().isEmpty());

		/*
		 * Show that a single Resolver was added to the mock plugin context. There is
		 * no good way to prove it is the correct one, but if it were wrong,
		 * then the tests of event resolution in this plugin would fail.
		 */
		Map<ResolverId, Consumer<ResolverContext>> resolverMap = mockPluginContext.getResolverMap();
		assertEquals(1, resolverMap.size());

	}

}
