package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.SimulationState;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionPropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_RegionPropertyDimension {

	@Test
	@UnitTestConstructor(target = RegionPropertyDimension.class, args = { RegionPropertyDimensionData.class })
	public void testConstructor() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8376720485839224759L);

		TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator);

		RegionPropertyDimensionData regionPropertyDimensionData = RegionPropertyDimensionData.builder()//
				.setRegionId(new RegionId() {//
				}).setRegionPropertyId(testRegionPropertyId)//
				.addValue("Level_0", testRegionPropertyId.getRandomPropertyValue(randomGenerator))//
				.build();

		RegionPropertyDimension regionPropertyDimension = new RegionPropertyDimension(regionPropertyDimensionData);
		assertNotNull(regionPropertyDimension);
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

			// create a RegionPropertyDimensionData with the level values
			RegionPropertyDimensionData.Builder dimDataBuilder = RegionPropertyDimensionData.builder()//
					.setRegionId(regionId)//
					.setRegionPropertyId(targetPropertyId);

			for (int k = 0; k < expectedValues.size(); k++) {
				dimDataBuilder.addValue("Level_" + k, expectedValues.get(k));
			}

			RegionPropertyDimensionData regionPropertyDimensionData = dimDataBuilder.build();
			RegionPropertyDimension regionPropertyDimension = new RegionPropertyDimension(regionPropertyDimensionData);

			// show that for each level the dimension properly assigns the value
			// to a region property data builder
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
				dimensionContextBuilder.setSimulationState(SimulationState.builder().build());
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
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "getDimensionData", args = {})
	public void testGetDimensionData() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

		for (int i = 0; i < 30; i++) {

			TestRegionPropertyId randomRegionPropertyId = TestRegionPropertyId
					.getRandomRegionPropertyId(randomGenerator);

			RegionPropertyDimensionData regionPropertyDimensionData = RegionPropertyDimensionData.builder()//
					.setRegionId(new RegionId() {//
					}).setRegionPropertyId(randomRegionPropertyId)//
					.addValue("Level_" + 0, randomRegionPropertyId.getRandomPropertyValue(randomGenerator))//
					.build();

			RegionPropertyDimension regionPropertyDimension = new RegionPropertyDimension(regionPropertyDimensionData);

			assertEquals(regionPropertyDimensionData, regionPropertyDimension.getDimensionData());
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "getExperimentMetaData", args = {})
	public void testGetExperimentMetaData() {
		for (TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {

			List<String> expectedExperimentMetaData = new ArrayList<>();
			expectedExperimentMetaData.add(testRegionPropertyId.toString());

			RegionPropertyDimensionData regionPropertyDimensionData = RegionPropertyDimensionData.builder()//
					.setRegionId(new RegionId() {//
					}).setRegionPropertyId(testRegionPropertyId)//
					.build();

			RegionPropertyDimension regionPropertyDimension = new RegionPropertyDimension(regionPropertyDimensionData);

			assertEquals(expectedExperimentMetaData, regionPropertyDimension.getExperimentMetaData());
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "levelCount", args = {})
	public void testLevelCount() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

		for (int i = 0; i < 50; i++) {

			RegionPropertyDimensionData.Builder builder = RegionPropertyDimensionData.builder()//
					.setRegionId(new RegionId() {//
					}).setRegionPropertyId(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				double value = randomGenerator.nextDouble();
				builder.addValue("Level_" + j, value);
			}
			RegionPropertyDimensionData regionPropertyDimensionData = builder.build();
			RegionPropertyDimension regionPropertyDimension = new RegionPropertyDimension(regionPropertyDimensionData);

			assertEquals(n, regionPropertyDimension.levelCount());
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDimension.class, name = "toString", args = {})
	public void testToString() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);
		TestRegionPropertyId randomRegionPropertyId = TestRegionPropertyId.getRandomRegionPropertyId(randomGenerator);

		RegionPropertyDimensionData.Builder builder = RegionPropertyDimensionData.builder()//
				.setRegionId(new RegionId() {//
				}).setRegionPropertyId(randomRegionPropertyId);

		for (int i = 0; i < 10; i++) {
			builder.addValue("Level_" + i, randomRegionPropertyId.getRandomPropertyValue(randomGenerator));
		}

		RegionPropertyDimensionData regionPropertyDimensionData = builder.build();
		RegionPropertyDimension regionPropertyDimension = new RegionPropertyDimension(regionPropertyDimensionData);

		String actualValue = regionPropertyDimension.toString();

		String expectedValue = "RegionPropertyDimension [regionPropertyDimensionData="
				+ regionPropertyDimensionData + "]";

		assertEquals(expectedValue, actualValue);
	}
}
