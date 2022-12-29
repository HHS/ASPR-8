package plugins.people;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = PeoplePluginId.class)
public class AT_PeoplePluginId {

	@Test
	public void test() {
		assertNotNull(PeoplePluginId.PLUGIN_ID);
	}
}
