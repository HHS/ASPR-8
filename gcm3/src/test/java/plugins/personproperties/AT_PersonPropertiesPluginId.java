package plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = PersonPropertiesPluginId.class)
public class AT_PersonPropertiesPluginId {

	@Test
	public void test() {
		assertNotNull(PersonPropertiesPluginId.PLUGIN_ID);
	}
}
