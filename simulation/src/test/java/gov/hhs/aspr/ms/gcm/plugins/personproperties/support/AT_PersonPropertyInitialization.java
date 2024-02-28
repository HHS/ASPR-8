package gov.hhs.aspr.ms.gcm.plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_PersonPropertyInitialization {

	@Test
	@UnitTestConstructor(target = PersonPropertyValueInitialization.class, args = { PersonPropertyId.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyValueInitialization.class, name = "getPersonPropertyId", args = {})
	public void testGetPersonPropertyId() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Double value = 2.7;
		PersonPropertyValueInitialization personPropertyValueInitialization = new PersonPropertyValueInitialization(personPropertyId, value);
		assertEquals(personPropertyId, personPropertyValueInitialization.getPersonPropertyId());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyValueInitialization.class, name = "getValue", args = {})
	public void testGetValue() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Double value = 2.7;
		PersonPropertyValueInitialization personPropertyValueInitialization = new PersonPropertyValueInitialization(personPropertyId, value);
		assertEquals(value, personPropertyValueInitialization.getValue());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyValueInitialization.class, name = "equals", args = { Object.class })
	public void testEquals() {
		PersonPropertyValueInitialization personProp1 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, 2.7);
		PersonPropertyValueInitialization personProp2 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false);
		PersonPropertyValueInitialization personProp3 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		PersonPropertyValueInitialization personProp4 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 20);
		PersonPropertyValueInitialization personProp5 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, 2.7);
		PersonPropertyValueInitialization personProp6 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 20);

		// reflexive
		assertEquals(personProp1, personProp1);
		assertEquals(personProp2, personProp2);
		assertEquals(personProp3, personProp3);
		assertEquals(personProp4, personProp4);
		assertEquals(personProp5, personProp5);
		assertEquals(personProp6, personProp6);

		// symmetric
		assertEquals(personProp1, personProp5);
		assertEquals(personProp5, personProp1);
		assertEquals(personProp4, personProp6);
		assertEquals(personProp6, personProp4);

		assertNotEquals(personProp1, personProp2);
		assertNotEquals(personProp1, personProp3);
		assertNotEquals(personProp1, personProp4);
		assertNotEquals(personProp1, personProp6);

		assertNotEquals(personProp2, personProp1);
		assertNotEquals(personProp2, personProp3);
		assertNotEquals(personProp2, personProp4);
		assertNotEquals(personProp2, personProp5);
		assertNotEquals(personProp2, personProp6);

		assertNotEquals(personProp3, personProp1);
		assertNotEquals(personProp3, personProp2);
		assertNotEquals(personProp3, personProp4);
		assertNotEquals(personProp3, personProp5);
		assertNotEquals(personProp3, personProp6);

		assertNotEquals(personProp4, personProp1);
		assertNotEquals(personProp4, personProp2);
		assertNotEquals(personProp4, personProp3);
		assertNotEquals(personProp4, personProp5);

		assertNotEquals(personProp5, personProp2);
		assertNotEquals(personProp5, personProp3);
		assertNotEquals(personProp5, personProp4);
		assertNotEquals(personProp5, personProp6);

		assertNotEquals(personProp6, personProp1);
		assertNotEquals(personProp6, personProp2);
		assertNotEquals(personProp6, personProp3);
		assertNotEquals(personProp6, personProp5);

		assertNotEquals(personProp1, null);
		assertNotEquals(personProp2, null);
		assertNotEquals(personProp3, null);
		assertNotEquals(personProp4, null);
		assertNotEquals(personProp5, null);
		assertNotEquals(personProp6, null);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyValueInitialization.class, name = "toString", args = {})
	public void testToString() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK;
		Double value = 2.7;
		PersonPropertyValueInitialization personPropertyValueInitialization = new PersonPropertyValueInitialization(personPropertyId, value);
		String expectedString = "PersonPropertyAssignment [personPropertyId=PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, value=2.7]";

		assertEquals(expectedString, personPropertyValueInitialization.toString());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyValueInitialization.class, name = "hashCode", args = {})
	public void testHashCode() {
		PersonPropertyValueInitialization personProp1 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, 2.7);
		PersonPropertyValueInitialization personProp2 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false);
		PersonPropertyValueInitialization personProp3 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 20);
		PersonPropertyValueInitialization personProp4 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, 2.7);
		PersonPropertyValueInitialization personProp5 = new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 20);

		// reflexive
		assertEquals(personProp1.hashCode(), personProp1.hashCode());
		assertEquals(personProp2.hashCode(), personProp2.hashCode());
		assertEquals(personProp3.hashCode(), personProp3.hashCode());
		assertEquals(personProp4.hashCode(), personProp4.hashCode());
		assertEquals(personProp5.hashCode(), personProp5.hashCode());

		// symmetric
		assertEquals(personProp1.hashCode(), personProp4.hashCode());
		assertEquals(personProp4.hashCode(), personProp1.hashCode());
		assertEquals(personProp3.hashCode(), personProp5.hashCode());
		assertEquals(personProp5.hashCode(), personProp3.hashCode());

		assertNotEquals(personProp1.hashCode(), personProp2.hashCode());
		assertNotEquals(personProp1.hashCode(), personProp3.hashCode());
		assertNotEquals(personProp1.hashCode(), personProp5.hashCode());

		assertNotEquals(personProp2.hashCode(), personProp1.hashCode());
		assertNotEquals(personProp2.hashCode(), personProp3.hashCode());
		assertNotEquals(personProp2.hashCode(), personProp4.hashCode());
		assertNotEquals(personProp2.hashCode(), personProp5.hashCode());

		assertNotEquals(personProp3.hashCode(), personProp1.hashCode());
		assertNotEquals(personProp3.hashCode(), personProp2.hashCode());
		assertNotEquals(personProp3.hashCode(), personProp4.hashCode());

		assertNotEquals(personProp4.hashCode(), personProp2.hashCode());
		assertNotEquals(personProp4.hashCode(), personProp3.hashCode());
		assertNotEquals(personProp4.hashCode(), personProp5.hashCode());

		assertNotEquals(personProp5.hashCode(), personProp1.hashCode());
		assertNotEquals(personProp5.hashCode(), personProp2.hashCode());
		assertNotEquals(personProp5.hashCode(), personProp4.hashCode());
	}
}
