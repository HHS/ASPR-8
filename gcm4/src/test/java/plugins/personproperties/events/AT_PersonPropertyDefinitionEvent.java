package plugins.personproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.personproperties.support.PersonPropertyId;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_PersonPropertyDefinitionEvent {

	@Test
	@UnitTestConstructor(target = PersonPropertyDefinitionEvent.class, args = { PersonPropertyId.class })
	public void testConstructor() {

		// precondition: person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonPropertyDefinitionEvent(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDefinitionEvent.class, name = "personPropertyId", args = {})
	public void testPersonPropertyId() {
		// nothing to test
	}

}
