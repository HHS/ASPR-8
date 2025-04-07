package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionPropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_RegionPropertyDimensionData {

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.Builder.class, name = "addValue", args = { String.class, Object.class })
	public void testAddValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3468803942988565031L);

		for (int i = 0; i < 50; i++) {
			List<Object> expectedValues = new ArrayList<>();

			RegionPropertyDimensionData.Builder builder = RegionPropertyDimensionData.builder()//
					.setRegionId(new RegionId() {
					}).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				Double value = randomGenerator.nextDouble();
				expectedValues.add(value);
				builder.addValue("Level_" + j, value);
			}
			RegionPropertyDimensionData regionPropertyDimensionData = builder.build();

			List<Object> actualValues = regionPropertyDimensionData.getValues();
			assertEquals(expectedValues, actualValues);
		}

		// precondition test : if the level is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> RegionPropertyDimensionData.builder().addValue(null, "testValue"));
		assertEquals(NucleusError.NULL_DIMENSION_LEVEL_NAME, contractException.getErrorType());

		// precondition test : if the value is null
		contractException = assertThrows(ContractException.class,
				() -> RegionPropertyDimensionData.builder().addValue("Level_0", null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.Builder.class, name = "build", args = {})
	public void testBuild() {
		RegionPropertyDimensionData regionPropertyDimensionData = RegionPropertyDimensionData.builder()//
				.setRegionId(new RegionId() {//
				}).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE)//
				.build();

		assertNotNull(regionPropertyDimensionData);

		// precondition test: if the region property id is not assigned
		ContractException contractException = assertThrows(ContractException.class,
				() -> RegionPropertyDimensionData.builder().setRegionId(new RegionId() {
				}).build());
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the regionId was not assigned
		contractException = assertThrows(ContractException.class, () -> RegionPropertyDimensionData.builder()
				.setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE).build());
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition test: if the dimension data contains duplicate level names
		contractException = assertThrows(ContractException.class, () -> {
			RegionPropertyDimensionData.builder()//
					.setRegionId(new RegionId() {//
					}).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_6_DOUBLE_IMMUTABLE)//
					._addLevelName("bad")//
					._addLevelName("bad")//
					.build();
		});

		assertEquals(NucleusError.DUPLICATE_DIMENSION_LEVEL_NAME, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.Builder.class, name = "setRegionId", args = { RegionId.class })
	public void testSetRegionId() {
		for (int i = 0; i < 10; i++) {
			RegionId regionId = new RegionId() {
			};

			RegionPropertyDimensionData regionPropertyDimensionData = RegionPropertyDimensionData.builder()//
					.setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE)//
					.setRegionId(regionId)//
					.build();

			assertEquals(regionId, regionPropertyDimensionData.getRegionId());
		}

		// precondition test : if the value is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> RegionPropertyDimensionData.builder().setRegionId(null));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.Builder.class, name = "setRegionPropertyId", args = {
			RegionPropertyId.class })
	public void testSetRegionPropertyId() {
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {

			RegionPropertyDimensionData regionPropertyDimensionData = RegionPropertyDimensionData.builder()//
					.setRegionId(new RegionId() {//
					}).setRegionPropertyId(testRegionPropertyId)//
					.build();

			assertEquals(testRegionPropertyId, regionPropertyDimensionData.getRegionPropertyId());
		}

		// precondition test : if the value is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> RegionPropertyDimensionData.builder().setRegionPropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(RegionPropertyDimensionData.builder());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "getRegionId", args = {})
	public void testGetRegionId() {
		for (int i = 0; i < 10; i++) {
			RegionId regionId = new RegionId() {
			};
			RegionPropertyDimensionData regionPropertyDimensionData = RegionPropertyDimensionData.builder()//
					.setRegionId(regionId)//
					.setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE)//
					.build();

			assertEquals(regionId, regionPropertyDimensionData.getRegionId());
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "getRegionPropertyId", args = {})
	public void testGetRegionPropertyId() {
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {

			RegionPropertyDimensionData regionPropertyDimensionData = RegionPropertyDimensionData.builder()//
					.setRegionId(new RegionId() {
					}).setRegionPropertyId(testRegionPropertyId)
					.build();

			assertEquals(testRegionPropertyId, regionPropertyDimensionData.getRegionPropertyId());
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "getValue", args = { int.class })
	public void testGetValue() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

		List<Object> expectedValues = new ArrayList<>();
		List<String> expectedLevelNames = new ArrayList<>();

		TestRegionPropertyId targetPropertyId = TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator);

		RegionPropertyDimensionData.Builder builder = RegionPropertyDimensionData.builder()//
				.setRegionId(new RegionId() {//
				}).setRegionPropertyId(targetPropertyId);

		int levels = randomGenerator.nextInt();

		for (int i = 0; i < levels; i++) {
			Object expectedValue = targetPropertyId.getRandomPropertyValue(randomGenerator);
			expectedValues.add(expectedValue);
			expectedLevelNames.add("Level_" + i);
			builder.addValue("Level_" + i, expectedValue);
		}

		RegionPropertyDimensionData regionPropertyDimensionData = builder.build();

		assertEquals(expectedLevelNames.size(), expectedValues.size());

		for (int i = 0; i < expectedValues.size(); i++) {
			Object expectedValue = expectedValues.get(i);
			Object actualValue = regionPropertyDimensionData.getValue(i);
			assertEquals(expectedValue, actualValue);
		}

		// preconditions: negative level
		ContractException contractException = assertThrows(ContractException.class, () -> {
			regionPropertyDimensionData.getValue(-1);
		});
		assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());

		// preconditions: level greater than total levels
		contractException = assertThrows(ContractException.class, () -> {
			regionPropertyDimensionData.getValue(regionPropertyDimensionData.getLevelCount() + 2);
		});
		assertEquals(NucleusError.INVALID_DIMENSION_LEVEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "getValues", args = {})
	public void testGetValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

		for (int i = 0; i < 50; i++) {
			List<Object> expectedValues = new ArrayList<>();

			RegionPropertyDimensionData.Builder builder = RegionPropertyDimensionData.builder()//
					.setRegionId(new RegionId() {
					}).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				Double value = randomGenerator.nextDouble();
				expectedValues.add(value);
				builder.addValue("Level_" + j, value);
			}
			RegionPropertyDimensionData regionPropertyDimensionData = builder.build();

			List<Object> actualValues = regionPropertyDimensionData.getValues();
			assertEquals(expectedValues, actualValues);
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "getVersion", args = {})
	public void testGetVersion() {

		RegionPropertyDimensionData dimData = RegionPropertyDimensionData.builder()//
				.setRegionId(new RegionId() {//
				}).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_6_DOUBLE_IMMUTABLE)
				.build();

		assertEquals(StandardVersioning.VERSION, dimData.getVersion());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(StandardVersioning.checkVersionSupported(version));
			assertFalse(StandardVersioning.checkVersionSupported(version + "badVersion"));
			assertFalse(StandardVersioning.checkVersionSupported("badVersion"));
			assertFalse(StandardVersioning.checkVersionSupported(version + "0"));
			assertFalse(StandardVersioning.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(331499833066074706L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RegionPropertyDimensionData regionPropertyDimensionData1 = getRandomRegionPropertyDimensionData(seed);
			RegionPropertyDimensionData regionPropertyDimensionData2 = getRandomRegionPropertyDimensionData(seed);

			assertEquals(regionPropertyDimensionData1, regionPropertyDimensionData2);
			assertEquals(regionPropertyDimensionData1.hashCode(), regionPropertyDimensionData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RegionPropertyDimensionData regionPropertyDimensionData = getRandomRegionPropertyDimensionData(
					randomGenerator.nextLong());
			hashCodes.add(regionPropertyDimensionData.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6276127520796404855L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			RegionPropertyDimensionData regionPropertyDimensionData = getRandomRegionPropertyDimensionData(
					randomGenerator.nextLong());
			assertFalse(regionPropertyDimensionData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			RegionPropertyDimensionData regionPropertyDimensionData = getRandomRegionPropertyDimensionData(
					randomGenerator.nextLong());
			assertFalse(regionPropertyDimensionData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			RegionPropertyDimensionData regionPropertyDimensionData = getRandomRegionPropertyDimensionData(
					randomGenerator.nextLong());
			assertTrue(regionPropertyDimensionData.equals(regionPropertyDimensionData));
		}

		// symmetric, transitive and consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RegionPropertyDimensionData regionPropertyDimensionData1 = getRandomRegionPropertyDimensionData(seed);
			RegionPropertyDimensionData regionPropertyDimensionData2 = getRandomRegionPropertyDimensionData(seed);
			assertFalse(regionPropertyDimensionData1 == regionPropertyDimensionData2);
			for (int j = 0; j < 10; j++) {
				assertTrue(regionPropertyDimensionData1.equals(regionPropertyDimensionData2));
				assertTrue(regionPropertyDimensionData2.equals(regionPropertyDimensionData1));
			}
		}

		// Different inputs yield unequal values
		Set<RegionPropertyDimensionData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RegionPropertyDimensionData regionPropertyDimensionData = getRandomRegionPropertyDimensionData(
					randomGenerator.nextLong());
			set.add(regionPropertyDimensionData);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "toString", args = {})
	public void testToString() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1528599899244176790L);

		RegionId regionId = TestRegionId.getRandomRegionId(randomGenerator);
		TestRegionPropertyId targetPropertyId = TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator);
		Object targetValue1 = targetPropertyId.getRandomPropertyValue(randomGenerator);
		Object targetValue2 = targetPropertyId.getRandomPropertyValue(randomGenerator);

		RegionPropertyDimensionData dimensionData = RegionPropertyDimensionData.builder()//
				.setRegionId(regionId)//
				.setRegionPropertyId(targetPropertyId)//
				.addValue("Level_0", targetValue1)//
				.addValue("Level_1", targetValue2)//
				.build();

		StringBuilder builder = new StringBuilder();
		builder.append("RegionPropertyDimensionData [data=");
		builder.append("Data [levelNames=[");
		builder.append("Level_0, Level_1]");
		builder.append(", values=[");
		builder.append(targetValue1.toString() + ", ");
		builder.append(targetValue2.toString() + "]");
		builder.append(", regionId=");
		builder.append(regionId.toString());
		builder.append(", regionPropertyId=");
		builder.append(targetPropertyId.toString());
		builder.append("]");
		builder.append("]");

		String expectedString = builder.toString();

		assertEquals(expectedString, dimensionData.toString());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimensionData.class, name = "toBuilder", args = {})
	public void testToBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1528599899244176790L);

		TestRegionPropertyId targetPropertyId = TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator);
		Object targetValue1 = targetPropertyId.getRandomPropertyValue(randomGenerator);
		Object targetValue2 = targetPropertyId.getRandomPropertyValue(randomGenerator);

		RegionPropertyDimensionData dimensionData = RegionPropertyDimensionData.builder()//
				.setRegionId(new RegionId() {//
				}).setRegionPropertyId(targetPropertyId)//
				.addValue("Level_0", targetValue1)//
				.addValue("Level_1", targetValue2)//
				.build();

		// show that the returned clone builder will build an identical instance if no
		// mutations are made
		RegionPropertyDimensionData.Builder cloneBuilder = dimensionData.toBuilder();
		assertNotNull(cloneBuilder);
		assertEquals(dimensionData, cloneBuilder.build());

		// show that the clone builder builds a distinct instance if any mutation is
		// made

		// setRegionId
		TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
		cloneBuilder = dimensionData.toBuilder();
		cloneBuilder.setRegionId(randomRegionId);
		assertNotEquals(dimensionData, cloneBuilder.build());

		// setRegionPropertyId
		cloneBuilder = dimensionData.toBuilder();
		cloneBuilder.setRegionPropertyId(TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator));
		assertNotEquals(dimensionData, cloneBuilder.build());

		// addValue
		cloneBuilder = dimensionData.toBuilder();
		cloneBuilder.addValue("Level_2", "newValue");
		assertNotEquals(dimensionData, cloneBuilder.build());
	}

	private RegionPropertyDimensionData getRandomRegionPropertyDimensionData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		RegionPropertyDimensionData.Builder builder = RegionPropertyDimensionData.builder();

		builder.setRegionId(new SimpleRegionId(randomGenerator.nextInt()));

		TestRegionPropertyId regionPropertyId = TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator);
		builder.setRegionPropertyId(regionPropertyId);

		int count = randomGenerator.nextInt(3) + 1;
		for (int i = 0; i < count; i++) {
			Object propertyValue = regionPropertyId.getRandomPropertyValue(randomGenerator);
			builder.addValue("Level_" + i, propertyValue);
		}
		return builder.build();
	}
}
