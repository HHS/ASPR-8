package plugins.materials;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = MaterialsPluginId.class)
public class AT_MaterialsPluginId {

	@Test
	public void test() {
		assertNotNull(MaterialsPluginId.PLUGIN_ID);
	}
}
