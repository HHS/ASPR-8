package plugins.people;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestField;

@UnitTest(target = PeoplePluginId.class)
public class AT_PeoplePluginId {

	@Test
	@UnitTestField(name = "PLUGIN_ID")
	public void testPluginId() {
		assertNotNull(PeoplePluginId.PLUGIN_ID);
	}
}
