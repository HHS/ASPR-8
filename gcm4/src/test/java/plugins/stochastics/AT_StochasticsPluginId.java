package plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestField;

@UnitTest(target = StochasticsPluginId.class)
public class AT_StochasticsPluginId {
	
	@Test
	@UnitTestField(name = "PLUGIN_ID")
	public void testPluginId() {		
		assertNotNull(StochasticsPluginId.PLUGIN_ID);
	}

}
