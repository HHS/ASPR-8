package plugins.partitions.testsupport.attributes.events;

import org.junit.jupiter.api.Test;

import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.people.support.PersonId;
import tools.annotations.UnitTestConstructor;

public class AT_AttributeUpdateEvent {

	@Test
	@UnitTestConstructor(target = AttributeUpdateEvent.class, args = { PersonId.class, AttributeId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}
}
