package plugins.personproperties.events;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import tools.annotations.UnitTestConstructor;

public class AT_PersonPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(target = PersonPropertyUpdateEvent.class, args = { PersonId.class, PersonPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
