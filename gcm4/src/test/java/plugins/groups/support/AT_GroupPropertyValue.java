package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_GroupPropertyValue {

	@Test
	@UnitTestConstructor(target = GroupPropertyValue.class, args = { GroupPropertyId.class, Object.class })
	public void testConstructor() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2797741161017158600L);
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		String value = Integer.toString(randomGenerator.nextInt(100));

		assertNotNull(new GroupPropertyValue(groupPropertyId, value));

		// precondition: null group property id
		ContractException contractException = assertThrows(ContractException.class, () -> new GroupPropertyValue(null, value));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition: null value
		contractException = assertThrows(ContractException.class, () -> new GroupPropertyValue(groupPropertyId, null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupPropertyValue.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyValue.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyValue.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyValue.class, name = "groupPropertyId", args = {})
	public void testGroupPropertyId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GroupPropertyValue.class, name = "value", args = {})
	public void testValue() {
		// nothing to test
	}

}
