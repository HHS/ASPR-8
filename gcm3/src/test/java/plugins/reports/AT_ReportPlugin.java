package plugins.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.PluginContext;
import nucleus.ResolverContext;
import nucleus.ResolverId;
import nucleus.testsupport.MockPluginContext;
import plugins.reports.initialdata.ReportsInitialData;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ReportPlugin.class)
public class AT_ReportPlugin {

	@Test
	@UnitTestConstructor(args = { ReportsInitialData.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "init", args = { PluginContext.class })
	public void testInit() {

		// Create a mock plugin context
		MockPluginContext mockPluginContext = new MockPluginContext();

		/*
		 * Create a report plugin
		 */
		new ReportPlugin(ReportsInitialData.builder().build()).init(mockPluginContext);

		// show that the documented plugin has no dependencies

		assertTrue(mockPluginContext.getPluginDependencies().isEmpty());

		/*
		 * Show that a single Resolver was added to the mock plugin context.
		 * There is no good way to prove it is the correct one, but if it were
		 * wrong, then the tests of event resolution in this plugin would fail.
		 */
		Map<ResolverId, Consumer<ResolverContext>> resolverMap = mockPluginContext.getResolverMap();
		assertEquals(1, resolverMap.size());

	}

}
