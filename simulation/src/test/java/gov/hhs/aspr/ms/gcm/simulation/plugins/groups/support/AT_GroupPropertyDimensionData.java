package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestGroupPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GroupPropertyDimensionData {

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.Builder.class, name = "addValue", args = { String.class, Object.class })
	public void testAddValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3468803942988565031L);

		for (int i = 0; i < 50; i++) {
			List<Object> expectedValues = new ArrayList<>();

			GroupPropertyDimensionData.Builder builder = GroupPropertyDimensionData.builder()//
					.setGroupId(new GroupId(i))//
					.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);

			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				double value = randomGenerator.nextDouble();
				expectedValues.add(value);
				builder.addValue("Level_" + j, value);
			}
			GroupPropertyDimensionData groupPropertyDimensionData = builder.build();

			List<Object> actualValues = groupPropertyDimensionData.getValues();
			assertEquals(expectedValues, actualValues);
		}

		// precondition test : if the level is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> GroupPropertyDimensionData.builder().addValue(null, "testValue"));
		assertEquals(NucleusError.NULL_DIMENSION_LEVEL_NAME, contractException.getErrorType());

		// precondition test : if the value is null
		ContractException contractException2 = assertThrows(ContractException.class,
				() -> GroupPropertyDimensionData.builder().addValue("Level_0", null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException2.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.Builder.class, name = "build", args = {})
	public void testBuild() {
		GroupPropertyDimensionData groupPropertyDimensionData = GroupPropertyDimensionData.builder()//
				.setGroupId(new GroupId(0))//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK)//
				.build();
		assertNotNull(groupPropertyDimensionData);

		// precondition test: if the group property id is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyDimensionData.builder()
					.setGroupId(new GroupId(0))
					.build();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the group id was not assigned
		contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyDimensionData.builder()
					.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK)
					.build();
		});
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

		// precondition test: if the dimension data contains duplicate level names
		contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyDimensionData.builder()
					.setGroupId(new GroupId(0))//
					.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK)//
					._addLevelName("bad")//
					._addLevelName("bad")//
					.build();
		});
		assertEquals(NucleusError.DUPLICATE_DIMENSION_LEVEL_NAME, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.Builder.class, name = "setGroupId", args = { GroupId.class })
	public void testSetGroupId() {
		for (int i = 0; i < 10; i++) {
			GroupId groupId = new GroupId(i);

			GroupPropertyDimensionData groupPropertyDimensionData = GroupPropertyDimensionData.builder()//
					.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK)//
					.setGroupId(groupId)//
					.build();

			assertEquals(groupId, groupPropertyDimensionData.getGroupId());
		}

		// precondition test : if the group id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> GroupPropertyDimensionData.builder().setGroupId(null));
		assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.Builder.class, name = "setGroupPropertyId", args = {
			GroupPropertyId.class })
	public void testSetGroupPropertyId() {
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {

			GroupPropertyDimensionData groupPropertyDimensionData = GroupPropertyDimensionData.builder()//
					.setGroupId(new GroupId(0))//
					.setGroupPropertyId(testGroupPropertyId)//
					.build();

			assertEquals(testGroupPropertyId, groupPropertyDimensionData.getGroupPropertyId());
		}

		// precondition test : if the group property id is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> GroupPropertyDimensionData.builder().setGroupPropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(GroupPropertyDimensionData.builder());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "getGroupId", args = {})
	public void testGetGroupId() {
		for (int i = 0; i < 10; i++) {
			GroupId groupId = new GroupId(i);
			GroupPropertyDimensionData groupPropertyDimensionData = GroupPropertyDimensionData.builder()//
					.setGroupId(groupId)//
					.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK)
					.build();

			assertEquals(groupId, groupPropertyDimensionData.getGroupId());
		}
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "getGroupPropertyId", args = {})
	public void testGetGroupPropertyId() {
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {

			GroupPropertyDimensionData groupPropertyDimensionData = GroupPropertyDimensionData.builder()//
					.setGroupId(new GroupId(0))
					.setGroupPropertyId(testGroupPropertyId)
					.build();

			assertEquals(testGroupPropertyId, groupPropertyDimensionData.getGroupPropertyId());
		}
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "getValue", args = { int.class })
	public void testGetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

		List<Object> expectedValues = new ArrayList<>();
		List<String> expectedLevelNames = new ArrayList<>();

		TestGroupPropertyId targetPropertyId = TestGroupPropertyId.getRandomTestGroupPropertyId(randomGenerator);

		GroupPropertyDimensionData.Builder builder = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(targetPropertyId)//
				.setGroupId(new GroupId(0));

		int levels = randomGenerator.nextInt();

		for (int i = 0; i < levels; i++) {
			Object expectedValue = targetPropertyId.getRandomPropertyValue(randomGenerator);
			expectedValues.add(expectedValue);
			expectedLevelNames.add("Level_" + i);
			builder.addValue("Level_" + i, expectedValue);
		}

		GroupPropertyDimensionData groupPropertyDimensionData = builder.build();

		assertEquals(expectedLevelNames.size(), expectedValues.size());

		for (int i = 0; i < expectedValues.size(); i++) {
			Object expectedValue = expectedValues.get(i);
			Object actualValue = groupPropertyDimensionData.getValue(i);
			assertEquals(expectedValue, actualValue);
		}

		// preconditions: negative level
		ContractException contractException = assertThrows(ContractException.class, () -> {
			groupPropertyDimensionData.getValue(-1);
		});
		assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());

		// preconditions: level greater than total levels
		contractException = assertThrows(ContractException.class, () -> {
			groupPropertyDimensionData.getValue(groupPropertyDimensionData.getLevelCount() + 2);
		});

		assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "getValues", args = {})
	public void testGetValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

		for (int i = 0; i < 50; i++) {
			List<Object> expectedValues = new ArrayList<>();

			GroupPropertyDimensionData.Builder builder = GroupPropertyDimensionData.builder()//
					.setGroupId(new GroupId(0))//
					.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);

			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				double value = randomGenerator.nextDouble();
				expectedValues.add(value);
				builder.addValue("Level_" + j, value);
			}
			GroupPropertyDimensionData groupPropertyDimensionData = builder.build();

			List<Object> actualValues = groupPropertyDimensionData.getValues();
			assertEquals(expectedValues, actualValues);
		}
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "getVersion", args = {})
	public void testGetVersion() {

		GroupPropertyDimensionData dimData = GroupPropertyDimensionData.builder()//
				.setGroupId(new GroupId(0))//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK)//
				.build();

		assertEquals(StandardVersioning.VERSION, dimData.getVersion());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList("", StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(StandardVersioning.checkVersionSupported(version));
			assertFalse(StandardVersioning.checkVersionSupported(version + "badVersion"));
			assertFalse(StandardVersioning.checkVersionSupported("badVersion"));
			assertFalse(StandardVersioning.checkVersionSupported(version + "0"));
			assertFalse(StandardVersioning.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "hashCode", args = {})
	public void testHashCode() {
		GroupPropertyDimensionData dimensionData1 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(0))//
				.build();

		GroupPropertyDimensionData dimensionData2 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(1))//
				.build();

		GroupPropertyDimensionData dimensionData3 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(0))//
				.build();

		GroupPropertyDimensionData dimensionData4 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(1))//
				.build();

		GroupPropertyDimensionData dimensionData5 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(1))//
				.build();

		GroupPropertyDimensionData dimensionData6 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(0))//
				.build();

		assertEquals(dimensionData1.hashCode(), dimensionData1.hashCode());

		assertNotEquals(dimensionData1.hashCode(), dimensionData2.hashCode());
		assertNotEquals(dimensionData1.hashCode(), dimensionData3.hashCode());
		assertNotEquals(dimensionData1.hashCode(), dimensionData4.hashCode());
		assertNotEquals(dimensionData1.hashCode(), dimensionData5.hashCode());

		assertNotEquals(dimensionData2.hashCode(), dimensionData3.hashCode());
		assertNotEquals(dimensionData2.hashCode(), dimensionData4.hashCode());
		assertNotEquals(dimensionData2.hashCode(), dimensionData5.hashCode());
		assertNotEquals(dimensionData2.hashCode(), dimensionData6.hashCode());

		assertNotEquals(dimensionData3.hashCode(), dimensionData4.hashCode());
		assertNotEquals(dimensionData3.hashCode(), dimensionData5.hashCode());
		assertNotEquals(dimensionData3.hashCode(), dimensionData6.hashCode());

		assertNotEquals(dimensionData4.hashCode(), dimensionData5.hashCode());
		assertNotEquals(dimensionData4.hashCode(), dimensionData6.hashCode());

		assertNotEquals(dimensionData5.hashCode(), dimensionData6.hashCode());

		assertEquals(dimensionData1.hashCode(), dimensionData6.hashCode());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		GroupPropertyDimensionData dimensionData1 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(0))//
				.build();

		GroupPropertyDimensionData dimensionData2 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(1))//
				.build();

		GroupPropertyDimensionData dimensionData3 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(0))//
				.build();

		GroupPropertyDimensionData dimensionData4 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(1))//
				.build();

		GroupPropertyDimensionData dimensionData5 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(1))//
				.build();

		GroupPropertyDimensionData dimensionData6 = GroupPropertyDimensionData.builder()//
				.setGroupPropertyId(TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK)//
				.setGroupId(new GroupId(0))//
				.build();

		assertEquals(dimensionData1, dimensionData1);

		assertNotEquals(dimensionData1, null);
		assertNotEquals(dimensionData1, new Object());

		assertNotEquals(dimensionData1, dimensionData2);
		assertNotEquals(dimensionData1, dimensionData3);
		assertNotEquals(dimensionData1, dimensionData4);
		assertNotEquals(dimensionData1, dimensionData5);

		assertNotEquals(dimensionData2, dimensionData3);
		assertNotEquals(dimensionData2, dimensionData4);
		assertNotEquals(dimensionData2, dimensionData5);
		assertNotEquals(dimensionData2, dimensionData6);

		assertNotEquals(dimensionData3, dimensionData4);
		assertNotEquals(dimensionData3, dimensionData5);
		assertNotEquals(dimensionData3, dimensionData6);

		assertNotEquals(dimensionData4, dimensionData5);
		assertNotEquals(dimensionData4, dimensionData6);

		assertNotEquals(dimensionData5, dimensionData6);

		assertEquals(dimensionData1, dimensionData6);
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "toString", args = {})
	public void testToString() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1528599899244176790L);

		TestGroupPropertyId targetPropertyId = TestGroupPropertyId.getRandomTestGroupPropertyId(randomGenerator);

		Object targetValue1 = targetPropertyId.getRandomPropertyValue(randomGenerator);
		Object targetValue2 = targetPropertyId.getRandomPropertyValue(randomGenerator);
		GroupId targetGroupId = new GroupId(0);

		GroupPropertyDimensionData dimensionData = GroupPropertyDimensionData.builder()//
				.setGroupId(targetGroupId)//
				.setGroupPropertyId(targetPropertyId)//
				.addValue("Level_0", targetValue1)//
				.addValue("Level_1", targetValue2)//
				.build();

		StringBuilder builder = new StringBuilder();
		builder.append("GroupPropertyDimensionData [data=");
		builder.append("Data [levelNames=[");
		builder.append("Level_0, Level_1]");
		builder.append(", values=[");
		builder.append(targetValue1.toString() + ", ");
		builder.append(targetValue2.toString() + "]");
		builder.append(", groupId=");
		builder.append(targetGroupId.toString());
		builder.append(", groupPropertyId=");
		builder.append(targetPropertyId.toString());
		builder.append("]");
		builder.append("]");

		String expectedString = builder.toString();

		assertEquals(expectedString, dimensionData.toString());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyDimensionData.class, name = "toBuilder", args = {})
	public void testToBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1528599899244176790L);

		TestGroupPropertyId targetPropertyId = TestGroupPropertyId.getRandomTestGroupPropertyId(randomGenerator);

		Object targetValue1 = targetPropertyId.getRandomPropertyValue(randomGenerator);
		Object targetValue2 = targetPropertyId.getRandomPropertyValue(randomGenerator);
		GroupId targetGroupId = new GroupId(0);

		GroupPropertyDimensionData dimensionData = GroupPropertyDimensionData.builder()//
				.setGroupId(targetGroupId)//
				.setGroupPropertyId(targetPropertyId)//
				.addValue("Level_0", targetValue1)//
				.addValue("Level_1", targetValue2)//
				.build();

		// show that the returned clone builder will build an identical instance if no
		// mutations are made
		GroupPropertyDimensionData.Builder cloneBuilder = dimensionData.toBuilder();
		assertNotNull(cloneBuilder);
		assertEquals(dimensionData, cloneBuilder.build());

		// show that the clone builder builds a distinct instance if any mutation is
		// made

		// setGroupId
		cloneBuilder = dimensionData.toBuilder();
		cloneBuilder.setGroupId(new GroupId(99));
		assertNotEquals(dimensionData, cloneBuilder.build());

		// setGroupPropertyId
		cloneBuilder = dimensionData.toBuilder();
		cloneBuilder.setGroupPropertyId(TestGroupPropertyId.getRandomTestGroupPropertyId(randomGenerator));
		assertNotEquals(dimensionData, cloneBuilder.build());

		// addValue
		cloneBuilder = dimensionData.toBuilder();
		cloneBuilder.addValue("Level_2", "newValue");
		assertNotEquals(dimensionData, cloneBuilder.build());
	}
}
