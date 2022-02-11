package plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.personproperties.testsupport.TestPersonPropertyId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyInitialization.class)
public class AT_PersonPropertyInitialization {

	@Test
	@UnitTestConstructor(args = { PersonPropertyId.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyId", args = {})
	public void testGetPersonPropertyId() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Double value = 2.7;
		PersonPropertyInitialization personPropertyInitialization = new PersonPropertyInitialization(personPropertyId, value);
		assertEquals(personPropertyId, personPropertyInitialization.getPersonPropertyId());
	}

	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Double value = 2.7;
		PersonPropertyInitialization personPropertyInitialization = new PersonPropertyInitialization(personPropertyId, value);
		assertEquals(value, personPropertyInitialization.getValue());
	}

}
