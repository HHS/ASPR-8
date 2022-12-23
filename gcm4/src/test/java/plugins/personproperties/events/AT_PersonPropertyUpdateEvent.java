package plugins.personproperties.events;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = PersonPropertyUpdateEvent.class)
public class AT_PersonPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, PersonPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
