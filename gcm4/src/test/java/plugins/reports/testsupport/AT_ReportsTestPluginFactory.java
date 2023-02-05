package plugins.reports.testsupport;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;

public class AT_ReportsTestPluginFactory {

	@Test
	@UnitTestMethod(target = ReportsTestPluginFactory.class, name = "getPluginFromReport", args= {Consumer.class}, tags= {UnitTag.INCOMPLETE})
	public void testGetPluginFromReport() {
		        
    }
	
}
