package plugins.personproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyUpdateEvent.class)
public class AT_PersonPropertyUpdateEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, PersonPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
