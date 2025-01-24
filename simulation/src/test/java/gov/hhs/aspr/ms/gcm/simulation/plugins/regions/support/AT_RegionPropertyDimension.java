package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionPropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_RegionPropertyDimension {
	@Test
	@UnitTestMethod(target = RegionPropertyDimension.Builder.class, name = "addValue", args = { Object.class })
	public void testAddValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3468803942988565031L);

		for (int i = 0; i < 50; i++) {
			List<Object> expectedValues = new ArrayList<>();

			RegionPropertyDimension.Builder builder = RegionPropertyDimension.builder()//
					.setRegionId(new RegionId() {
					}).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				double value = randomGenerator.nextDouble();
				expectedValues.add(value);
				builder.addValue(value);
			}
			RegionPropertyDimension regionPropertyDimension = builder.build();

			List<Object> actualValues = regionPropertyDimension.getValues();
			assertEquals(expectedValues, actualValues);
		}

		// precondition test : if the value is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> RegionPropertyDimension.builder().addValue(null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.Builder.class, name = "build", args = {})
	public void testBuild() {
		RegionPropertyDimension regionPropertyDimension = //
				RegionPropertyDimension.builder()//
						.setRegionId(new RegionId() {
						}).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE).build();
		assertNotNull(regionPropertyDimension);

		// precondition test : if the global property id is not assigned
		ContractException contractException = assertThrows(ContractException.class,
				() -> RegionPropertyDimension.builder().setRegionId(new RegionId() {
				}).build());
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// // if the regionId was not assigned
		contractException = assertThrows(ContractException.class, () -> RegionPropertyDimension.builder()
				.setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE).build());
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.Builder.class, name = "setRegionId", args = { RegionId.class })
	public void testSetRegionId() {
		for (int i = 0; i < 10; i++) {
			RegionId regionId = new RegionId() {
			};

			RegionPropertyDimension.Builder builder = RegionPropertyDimension.builder()//
					.setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE).setRegionId(regionId);

			RegionPropertyDimension regionPropertyDimension = builder.build();
			assertEquals(regionId, regionPropertyDimension.getRegionId());
		}

		// precondition test : if the value is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> RegionPropertyDimension.builder().setRegionId(null));
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.Builder.class, name = "setRegionPropertyId", args = {
			RegionPropertyId.class })
	public void testSetRegionPropertyId() {
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {

			RegionPropertyDimension.Builder builder = RegionPropertyDimension.builder()//
					.setRegionId(new RegionId() {
					}).setRegionPropertyId(testRegionPropertyId);

			RegionPropertyDimension regionPropertyDimension = builder.build();
			assertEquals(testRegionPropertyId, regionPropertyDimension.getRegionPropertyId());
		}

		// precondition test : if the value is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> RegionPropertyDimension.builder().setRegionPropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(RegionPropertyDimension.builder());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "executeLevel", args = { DimensionContext.class,
			int.class })
	public void testExecuteLevel() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2924521933883974690L);

		// run several test cases
		for (int i = 0; i < 30; i++) {

			RegionId regionId = new RegionId() {
			};
			// select a random number of levels
			int levelCount = randomGenerator.nextInt(10);
			// select a random property id
			TestRegionPropertyId targetPropertyId = TestRegionPropertyId
					.getRandomMutableRegionPropertyId(randomGenerator);

			// generate random values for the level
			List<Object> expectedValues = new ArrayList<>();
			for (int j = 0; j < levelCount; j++) {
				expectedValues.add(targetPropertyId.getRandomPropertyValue(randomGenerator));
			}

			// create a RegionPropertyDimension with the level values
			RegionPropertyDimension.Builder dimBuilder = RegionPropertyDimension.builder()//
					.setRegionId(regionId).setRegionPropertyId(targetPropertyId);

			for (Object value : expectedValues) {
				dimBuilder.addValue(value);
			}

			RegionPropertyDimension regionPropertyDimension = dimBuilder.build();

			// show that for each level the dimension properly assigns the value
			// to a global property data builder
			for (int level = 0; level < levelCount; level++) {
				/*
				 * Create a RegionsPluginData, filling it with the test property definitions and
				 * any values that are required
				 */
				RegionsPluginData.Builder pluginDataBuilder = RegionsPluginData.builder();
				for (TestRegionPropertyId propertyId : TestRegionPropertyId.values()) {
					PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
					pluginDataBuilder.defineRegionProperty(propertyId, propertyDefinition);

					if (propertyDefinition.getDefaultValue().isEmpty()) {
						pluginDataBuilder.setRegionPropertyValue(regionId, propertyId,
								propertyId.getRandomPropertyValue(randomGenerator));
					}
				}

				pluginDataBuilder.addRegion(regionId);
				// Create a dimension context that contain the plugin data
				// builder
				DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

				pluginDataBuilder = (RegionsPluginData.Builder) dimensionContextBuilder.add(pluginDataBuilder.build());
				DimensionContext dimensionContext = dimensionContextBuilder.build();

				// execute the dimension with the level
				regionPropertyDimension.executeLevel(dimensionContext, level);

				/*
				 * get the RegionsPluginData from the corresponding builder
				 */
				RegionsPluginData regionsPluginData = pluginDataBuilder.build();

				/*
				 * show that the RegionsPluginData has the value we expect for the given level
				 */

				Map<RegionPropertyId, Object> regionPropertyValues = regionsPluginData
						.getRegionPropertyValues(regionId);

				assertTrue(regionPropertyValues.containsKey(targetPropertyId));

				Object actualValue = regionPropertyValues.get(targetPropertyId);
				Object expectedValue = expectedValues.get(level);
				assertEquals(expectedValue, actualValue);
			}
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "getExperimentMetaData", args = {})
	public void testGetExperimentMetaData() {
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {

			List<String> expectedExperimentMetaData = new ArrayList<>();
			expectedExperimentMetaData.add(testRegionPropertyId.toString());

			RegionPropertyDimension.Builder builder = RegionPropertyDimension.builder()//
					.setRegionId(new RegionId() {
					}).setRegionPropertyId(testRegionPropertyId);

			RegionPropertyDimension regionPropertyDimension = builder.build();
			assertEquals(expectedExperimentMetaData, regionPropertyDimension.getExperimentMetaData());
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "getRegionId", args = {})
	public void testGetRegionId() {
		for (int i = 0; i < 10; i++) {
			RegionId regionId = new RegionId() {
			};
			RegionPropertyDimension.Builder builder = RegionPropertyDimension.builder()//
					.setRegionId(regionId).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

			RegionPropertyDimension regionPropertyDimension = builder.build();
			assertEquals(regionId, regionPropertyDimension.getRegionId());
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "getRegionPropertyId", args = {})
	public void testGetRegionPropertyId() {
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {

			RegionPropertyDimension.Builder builder = RegionPropertyDimension.builder()//
					.setRegionId(new RegionId() {
					}).setRegionPropertyId(testRegionPropertyId);

			RegionPropertyDimension regionPropertyDimension = builder.build();
			assertEquals(testRegionPropertyId, regionPropertyDimension.getRegionPropertyId());
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "getValues", args = {})
	public void testGetValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

		for (int i = 0; i < 50; i++) {
			List<Object> expectedValues = new ArrayList<>();

			RegionPropertyDimension.Builder builder = RegionPropertyDimension.builder()//
					.setRegionId(new RegionId() {
					}).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				double value = randomGenerator.nextDouble();
				expectedValues.add(value);
				builder.addValue(value);
			}
			RegionPropertyDimension regionPropertyDimension = builder.build();

			List<Object> actualValues = regionPropertyDimension.getValues();
			assertEquals(expectedValues, actualValues);
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "levelCount", args = {})
	public void testLevelCount() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

		for (int i = 0; i < 50; i++) {

			RegionPropertyDimension.Builder builder = RegionPropertyDimension.builder()//
					.setRegionId(new RegionId() {
					}).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				double value = randomGenerator.nextDouble();
				builder.addValue(value);
			}
			RegionPropertyDimension regionPropertyDimension = builder.build();

			assertEquals(n, regionPropertyDimension.levelCount());
		}
	}

	private RegionPropertyDimension getRandomRegionPropertyDimension(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		RegionPropertyDimension.Builder builder = RegionPropertyDimension.builder();
		builder.setRegionId(TestRegionId.getRandomRegionId(randomGenerator));
		TestRegionPropertyId regionPropertyId = TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator);
		builder.setRegionPropertyId(TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator));
		int count = randomGenerator.nextInt(3) + 1;
		for (int i = 0; i < count; i++) {
			Object propertyValue = regionPropertyId.getRandomPropertyValue(randomGenerator);
			builder.addValue(propertyValue);
		}
		return builder.build();
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6276127520796404855L);

		// never equal to null
		for (int i = 0; i < 30; i++) {
			RegionPropertyDimension regionPropertyDimension = getRandomRegionPropertyDimension(
					randomGenerator.nextLong());
			assertFalse(regionPropertyDimension.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			RegionPropertyDimension regionPropertyDimension = getRandomRegionPropertyDimension(
					randomGenerator.nextLong());
			assertTrue(regionPropertyDimension.equals(regionPropertyDimension));
		}

		// symmetric, transitive and consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RegionPropertyDimension regionPropertyDimension1 = getRandomRegionPropertyDimension(seed);
			RegionPropertyDimension regionPropertyDimension2 = getRandomRegionPropertyDimension(seed);

			assertTrue(regionPropertyDimension1.equals(regionPropertyDimension2));
			assertTrue(regionPropertyDimension2.equals(regionPropertyDimension1));
		}

		// Different inputs yield unequal values
		Set<RegionPropertyDimension> regionPropertyDimensions = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RegionPropertyDimension regionPropertyDimension = getRandomRegionPropertyDimension(
					randomGenerator.nextLong());
			regionPropertyDimensions.add(regionPropertyDimension);
		}
		assertTrue(regionPropertyDimensions.size() > 95);

	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(331499833066074706L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RegionPropertyDimension regionPropertyDimension1 = getRandomRegionPropertyDimension(seed);
			RegionPropertyDimension regionPropertyDimension2 = getRandomRegionPropertyDimension(seed);

			assertEquals(regionPropertyDimension1, regionPropertyDimension2);
			assertEquals(regionPropertyDimension1.hashCode(), regionPropertyDimension2.hashCode());
		}
		
		//hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RegionPropertyDimension regionPropertyDimension = getRandomRegionPropertyDimension(
					randomGenerator.nextLong());
			hashCodes.add(regionPropertyDimension.hashCode());
		}
		assertTrue(hashCodes.size() > 95);
	}

}
