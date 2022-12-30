package plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestField;

@UnitTest(target = PersonPropertiesPluginId.class)
public class AT_PersonPropertiesPluginId {

	@Test
	@UnitTestField(name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(PersonPropertiesPluginId.PLUGIN_ID);
	}
}
