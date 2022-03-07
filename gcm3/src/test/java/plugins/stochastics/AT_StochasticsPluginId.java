package plugins.stochastics;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;

@UnitTest(target = StochasticsPluginId.class)
public class AT_StochasticsPluginId {
	
	@Test
	public void test() {		
		assertNotNull(StochasticsPluginId.PLUGIN_ID);
	}

}
