package plugins.reports.testsupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.TestSimulation;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;
import util.wrappers.MutableBoolean;

public class AT_ReportsTestPluginFactory {

	@Test
	@UnitTestMethod(target = ReportsTestPluginFactory.class, name = "getPluginFromReport", args= {Consumer.class})
	public void testGetPluginFromReport() {
		
		MutableBoolean executed = new MutableBoolean();
		
		Plugin plugin = ReportsTestPluginFactory.getPluginFromReport((c)->executed.setValue(true));
		
		Simulation.builder().addPlugin(plugin).build().execute();
		
		assertTrue(executed.getValue());
    }
	
}
