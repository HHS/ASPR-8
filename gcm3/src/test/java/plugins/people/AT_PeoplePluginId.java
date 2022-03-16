package plugins.people;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import annotations.UnitTest;

@UnitTest(target = PeoplePluginId.class)
public class AT_PeoplePluginId {

	public void test() {
		assertNotNull(PeoplePluginId.PLUGIN_ID);
	}
}
