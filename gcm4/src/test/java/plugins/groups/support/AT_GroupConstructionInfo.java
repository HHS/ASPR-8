package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public final class AT_GroupConstructionInfo {

	@Test
	@UnitTestMethod(target = GroupConstructionInfo.class, name = "getGroupTypeId", args = {})
	public void testGetGroupTypeId() {
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			GroupConstructionInfo groupConstructionInfo = GroupConstructionInfo.builder().setGroupTypeId(testGroupTypeId).build();
			assertEquals(testGroupTypeId, groupConstructionInfo.getGroupTypeId());
		}
	}

	@Test
	@UnitTestMethod(target = GroupConstructionInfo.class, name = "getPropertyValues", args = {})
	public void testGetPropertyValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6591155321511911942L);
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			// build a group of the given type
			GroupConstructionInfo.Builder builder = GroupConstructionInfo.builder();
			builder.setGroupTypeId(testGroupTypeId);

			// create a container to hold expected values
			Map<TestGroupPropertyId, Object> expectedValues = new LinkedHashMap<>();

			// add randomized values to the group
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				Object value = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
				expectedValues.put(testGroupPropertyId, value);
				builder.setGroupPropertyValue(testGroupPropertyId, value);
			}
			// build
			GroupConstructionInfo groupConstructionInfo = builder.build();

			// show that the property values are correct
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				Object expectedValue = expectedValues.get(testGroupPropertyId);
				Object actualValue = groupConstructionInfo.getPropertyValues().get(testGroupPropertyId);
				assertEquals(expectedValue, actualValue);
			}
		}
	}

	@Test
	@UnitTestMethod(target = GroupConstructionInfo.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(GroupConstructionInfo.builder());

	}

	@Test
	@UnitTestMethod(target = GroupConstructionInfo.Builder.class, name = "build", args = {})
	public void testBuild() {
		// show that the builder returns a non-null result
		assertNotNull(GroupConstructionInfo.builder().setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1).build());

		// precondition tests

		// if the group property value is not set
		ContractException contractException = assertThrows(ContractException.class, () -> GroupConstructionInfo.builder().build());
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupConstructionInfo.Builder.class, name = "setGroupTypeId", args = { GroupTypeId.class })
	public void testSetGroupTypeId() {
		// show that the group type id is properly set to the last value in an
		// invocation to setGroupTypeId
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			GroupConstructionInfo.Builder builder = GroupConstructionInfo.builder();
			builder.setGroupTypeId(testGroupTypeId);
			TestGroupTypeId expectedGroupTypeId = testGroupTypeId.next();
			builder.setGroupTypeId(expectedGroupTypeId);
			GroupConstructionInfo groupConstructionInfo = builder.build();
			assertEquals(expectedGroupTypeId, groupConstructionInfo.getGroupTypeId());
		}
		// precondition tests

		// if the group property value is set to null
		ContractException contractException = assertThrows(ContractException.class, () -> GroupConstructionInfo.builder().setGroupTypeId(null).build());
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupConstructionInfo.Builder.class, name = "setGroupPropertyValue", args = { GroupPropertyId.class, Object.class })
	public void testSetGroupPropertyValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5862480779334008230L);

		// show that the group property value is properly set to the last value
		// in an invocation to setGroupPropertyValue
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			GroupConstructionInfo.Builder builder = GroupConstructionInfo.builder();
			builder.setGroupTypeId(testGroupTypeId);

			// construct a container to hold expected values
			Map<TestGroupPropertyId, Object> expectedValues = new LinkedHashMap<>();

			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {

				builder.setGroupPropertyValue(testGroupPropertyId, testGroupPropertyId.getRandomPropertyValue(randomGenerator));
				Object value = testGroupPropertyId.getRandomPropertyValue(randomGenerator);
				expectedValues.put(testGroupPropertyId, value);
				builder.setGroupPropertyValue(testGroupPropertyId, value);

			}

			GroupConstructionInfo groupConstructionInfo = builder.build();
			for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
				Object expectedValue = expectedValues.get(testGroupPropertyId);
				Object actualValue = groupConstructionInfo.getPropertyValues().get(testGroupPropertyId);
				assertEquals(expectedValue, actualValue);
			}
		}
		// precondition tests

		// if a group property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GroupConstructionInfo	.builder()//
									.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
									.setGroupPropertyValue(null, 12).build();//
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// if a group property value is null
		contractException = assertThrows(ContractException.class, () -> {
			GroupConstructionInfo	.builder()//
									.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
									.setGroupPropertyValue(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, null).build();//
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

	}

}
