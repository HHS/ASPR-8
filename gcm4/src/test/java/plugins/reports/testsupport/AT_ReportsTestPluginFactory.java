package plugins.reports.testsupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.Simulation;
import tools.annotations.UnitTestMethod;
import util.wrappers.MutableBoolean;

public class AT_ReportsTestPluginFactory {

	@Test
	@UnitTestMethod(target = ReportsTestPluginFactory.class, name = "getPluginFromReport", args = { Consumer.class })
	public void testGetPluginFromReport() {

		/*
		 * Create a boolean defaulted to false.
		 */
		MutableBoolean executed = new MutableBoolean();

		/*
		 * Create a report and get the corresponding plugin. if the report was
		 * added then it should get executed by a simulation and the boolean
		 * will be set to true.
		 */
		Plugin plugin = ReportsTestPluginFactory.getPluginFromReport((c) -> executed.setValue(true));

		/*
		 * Execute a simulation with the plugin.
		 */
		Simulation.builder().addPlugin(plugin).build().execute();

		/*
		 * Show that the report executed and thus must have been properly added
		 * to the plugin.
		 */
		assertTrue(executed.getValue());
	}

}
