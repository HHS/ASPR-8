package plugins.personproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.personproperties.support.PersonPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

public class AT_PersonPropertyDefinitionEvent {

	@Test
	@UnitTestConstructor(target = PersonPropertyDefinitionEvent.class, args = { PersonPropertyId.class })
	public void testConstructor() {

		// precondition: person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonPropertyDefinitionEvent(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}
}
