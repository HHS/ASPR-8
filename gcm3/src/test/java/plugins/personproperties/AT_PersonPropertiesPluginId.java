package plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import annotations.UnitTest;

@UnitTest(target = PersonPropertiesPluginId.class)
public class AT_PersonPropertiesPluginId {

	public void test() {
		assertNotNull(PersonPropertiesPluginId.PLUGIN_ID);
	}
}
