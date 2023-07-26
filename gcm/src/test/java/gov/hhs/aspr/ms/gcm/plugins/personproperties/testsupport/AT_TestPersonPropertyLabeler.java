package gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.partitions.support.Equality;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestPersonPropertyLabeler {


	@Test
	@UnitTestConstructor(target = TestPersonPropertyLabeler.class, args = { PersonPropertyId.class, Equality.class,
			int.class })
	public void testTestPersonPropertyLabeler() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = TestPersonPropertyLabeler.class, name = "getEquality", args = {})
	public void testGetEquality() {
		for (Equality equality : Equality.values()) {
			TestPersonPropertyLabeler testPersonPropertyLabeler = new TestPersonPropertyLabeler(
					TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, equality, 5);
			assertEquals(equality, testPersonPropertyLabeler.getEquality());
		}
	}

	@Test
	@UnitTestMethod(target = TestPersonPropertyLabeler.class, name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			TestPersonPropertyLabeler testPersonPropertyLabeler = new TestPersonPropertyLabeler(
					TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, Equality.EQUAL, i);
			assertEquals(i, testPersonPropertyLabeler.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = TestPersonPropertyLabeler.class, name = "toString", args = {})
	public void testToString() {
		TestPersonPropertyLabeler testPersonPropertyLabeler = new TestPersonPropertyLabeler(
				TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, Equality.EQUAL, 15);

		String actualValue = testPersonPropertyLabeler.toString();

		String expectedValue = "TestPersonPropertyLabeler [equality=EQUAL, value=15, personPropertyId=PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK]";

		assertEquals(expectedValue, actualValue);
	}

}
