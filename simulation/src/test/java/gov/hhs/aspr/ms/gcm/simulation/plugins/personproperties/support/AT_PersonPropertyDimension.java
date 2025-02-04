package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_PersonPropertyDimension {

	@Test
	@UnitTestConstructor(target = PersonPropertyDimension.class, args = { PersonPropertyDimensionData.class })
	public void testConstructor() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8376720485839224759L);

		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);

		PersonPropertyDimensionData personPropertyDimensionData = PersonPropertyDimensionData.builder()//
				.setPersonPropertyId(testPersonPropertyId)//
				.setTrackTimes(randomGenerator.nextBoolean())//
				.addValue("Level_0", testPersonPropertyId.getRandomPropertyValue(randomGenerator))//
				.build();

		PersonPropertyDimension personPropertyDimension = new PersonPropertyDimension(personPropertyDimensionData);

		assertNotNull(personPropertyDimension);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDimension.class, name = "executeLevel", args = { DimensionContext.class,
			int.class })
	public void testExecuteLevel() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2924521933883974690L);

		// run several test cases
		for (int i = 0; i < 30; i++) {

			// select a random number of levels
			int levelCount = randomGenerator.nextInt(10);
			// select a random property id
			TestPersonPropertyId targetPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);

			// generate random values for the level
			List<Object> expectedValues = new ArrayList<>();
			for (int j = 0; j < levelCount; j++) {
				expectedValues.add(targetPropertyId.getRandomPropertyValue(randomGenerator));
			}

			// create a PersonPropertyDimensionData with the level values
			PersonPropertyDimensionData.Builder dimDataBuilder = PersonPropertyDimensionData.builder()//
					.setPersonPropertyId(targetPropertyId)//
					.setTrackTimes(randomGenerator.nextBoolean());

			for (int k = 0; k < expectedValues.size(); k++) {
				dimDataBuilder.addValue("Level_" + k, expectedValues.get(k));
			}

			PersonPropertyDimensionData personPropertyDimensionData = dimDataBuilder.build();
			PersonPropertyDimension personPropertyDimension = new PersonPropertyDimension(personPropertyDimensionData);

			// show that for each level the dimension properly assigns the value
			// to a person property data builder
			for (int level = 0; level < levelCount; level++) {
				/*
				 * Create a PersonPropertiesPluginData, filling it with the test property
				 * definitions and any values that are required
				 */
				PersonPropertiesPluginData.Builder pluginDataBuilder = PersonPropertiesPluginData.builder();
				for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
					PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
					pluginDataBuilder.definePersonProperty(propertyId, propertyDefinition, 0, false);
				}

				// Create a dimension context that contains the plugin data
				// builder
				DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

				pluginDataBuilder = (PersonPropertiesPluginData.Builder) dimensionContextBuilder
						.add(pluginDataBuilder.build());
				DimensionContext dimensionContext = dimensionContextBuilder.build();

				// execute the dimension with the level
				personPropertyDimension.executeLevel(dimensionContext, level);

				/*
				 * get the PersonPropertiesPluginData from the corresponding builder
				 */
				PersonPropertiesPluginData personsPluginData = pluginDataBuilder.build();

				/*
				 * show that the PersonPropertiesPluginData has the value we expect for the
				 * given level
				 */

				PropertyDefinition actualDefinition = personsPluginData.getPersonPropertyDefinition(targetPropertyId);

				assertTrue(actualDefinition.getDefaultValue().isPresent());
				Object actualValue = actualDefinition.getDefaultValue().get();
				Object expectedValue = expectedValues.get(level);
				assertEquals(expectedValue, actualValue);
			}
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDimension.class, name = "getDimensionData", args = {})
	public void testGetDimensionData() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

		for (int i = 0; i < 30; i++) {

			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);

			PersonPropertyDimensionData personPropertyDimensionData = PersonPropertyDimensionData.builder()//
					.setPersonPropertyId(testPersonPropertyId)//
					.setTrackTimes(randomGenerator.nextBoolean())//
					.addValue("Level_" + i, testPersonPropertyId.getRandomPropertyValue(randomGenerator))//
					.build();

			PersonPropertyDimension personPropertyDimension = new PersonPropertyDimension(personPropertyDimensionData);

			assertEquals(personPropertyDimensionData, personPropertyDimension.getDimensionData());
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDimension.class, name = "getExperimentMetaData", args = {})
	public void testGetExperimentMetaData() {
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

			List<String> expectedExperimentMetaData = new ArrayList<>();
			expectedExperimentMetaData.add(testPersonPropertyId.toString());

			PersonPropertyDimensionData personPropertyDimensionData = PersonPropertyDimensionData.builder()//
					.setPersonPropertyId(testPersonPropertyId)//
					.build();

			PersonPropertyDimension personPropertyDimension = new PersonPropertyDimension(personPropertyDimensionData);
			assertEquals(expectedExperimentMetaData, personPropertyDimension.getExperimentMetaData());
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDimension.class, name = "levelCount", args = {})
	public void testLevelCount() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

		for (int i = 0; i < 50; i++) {

			PersonPropertyDimensionData.Builder builder = PersonPropertyDimensionData.builder()//
					.setPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);

			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				double value = randomGenerator.nextDouble();
				builder.addValue("Level_" + j, value);
			}

			PersonPropertyDimensionData personPropertyDimensionData = builder.build();
			PersonPropertyDimension personPropertyDimension = new PersonPropertyDimension(personPropertyDimensionData);

			assertEquals(n, personPropertyDimension.levelCount());
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyDimension.class, name = "toString", args = {})
	public void testToString() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);
		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.getRandomPersonPropertyId(randomGenerator);

		PersonPropertyDimensionData.Builder builder = PersonPropertyDimensionData.builder()//
				.setPersonPropertyId(testPersonPropertyId)//
				.setTrackTimes(randomGenerator.nextBoolean());

		for (int i = 0; i < 10; i++) {
			builder.addValue("Level_" + i, testPersonPropertyId.getRandomPropertyValue(randomGenerator));
		}

		PersonPropertyDimensionData personPropertyDimensionData = builder.build();
		PersonPropertyDimension personPropertyDimension = new PersonPropertyDimension(personPropertyDimensionData);

		String actualValue = personPropertyDimension.toString();

		String expectedValue = "PersonPropertyDimension [personPropertyDimensionData="
				+ personPropertyDimensionData + "]";

		assertEquals(expectedValue, actualValue);
	}
}
