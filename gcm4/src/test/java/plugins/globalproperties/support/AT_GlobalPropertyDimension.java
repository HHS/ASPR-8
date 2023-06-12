package plugins.globalproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DimensionContext;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.testsupport.TestGlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_GlobalPropertyDimension {

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.Builder.class, name = "addValue", args = { Object.class })
	public void testAddValue() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3468803942988565031L);

		for (int i = 0; i < 50; i++) {
			List<Object> expectedValues = new ArrayList<>();

			GlobalPropertyDimension.Builder builder = GlobalPropertyDimension.builder()//
					.setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);
			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				double value = randomGenerator.nextDouble();
				expectedValues.add(value);
				builder.addValue(value);
			}
			GlobalPropertyDimension globalPropertyDimension = builder.build();

			List<Object> actualValues = globalPropertyDimension.getValues();
			assertEquals(expectedValues, actualValues);
		}

		// precondition test : if the value is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> GlobalPropertyDimension.builder().addValue(null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.Builder.class, name = "build", args = {})
	public void testBuild() {
		GlobalPropertyDimension globalPropertyDimension = //
				GlobalPropertyDimension.builder()//
						.setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE)//
						.build();
		assertNotNull(globalPropertyDimension);

		// precondition test : if the global property id is not assigned
		ContractException contractException = assertThrows(ContractException.class,
				() -> GlobalPropertyDimension.builder().build());
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.Builder.class, name = "setAssignmentTime", args = { double.class })
	public void testSetAssignmentTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7384717734933740607L);

		for (int i = 0; i < 50; i++) {
			GlobalPropertyDimension.Builder builder = GlobalPropertyDimension.builder()//
					.setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);
			double assignmentTime = randomGenerator.nextDouble();
			builder.setAssignmentTime(assignmentTime);
			GlobalPropertyDimension globalPropertyDimension = builder.build();

			assertEquals(assignmentTime, globalPropertyDimension.getAssignmentTime());
		}
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.Builder.class, name = "setGlobalPropertyId", args = {
			GlobalPropertyId.class })
	public void testSetGlobalPropertyId() {
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {

			GlobalPropertyDimension.Builder builder = GlobalPropertyDimension.builder()//
					.setGlobalPropertyId(testGlobalPropertyId);

			GlobalPropertyDimension globalPropertyDimension = builder.build();
			assertEquals(testGlobalPropertyId, globalPropertyDimension.getGlobalPropertyId());
		}

		// precondition test : if the value is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> GlobalPropertyDimension.builder().setGlobalPropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(GlobalPropertyDimension.builder());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.class, name = "executeLevel", args = { DimensionContext.class,
			int.class })
	public void testExecuteLevel() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2924521933883974690L);

		// run several test cases
		for (int i = 0; i < 30; i++) {

			// select a random number of levels
			int levelCount = randomGenerator.nextInt(10);
			// select a random property id
			TestGlobalPropertyId targetPropertyId = TestGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);

			// generate random values for the level
			List<Object> exectedValues = new ArrayList<>();
			for (int j = 0; j < levelCount; j++) {
				exectedValues.add(targetPropertyId.getRandomPropertyValue(randomGenerator));
			}

			// create a GlobalPropertyDimension with the level values
			GlobalPropertyDimension.Builder dimBuilder = GlobalPropertyDimension.builder()//
					.setGlobalPropertyId(targetPropertyId);

			for (Object value : exectedValues) {
				dimBuilder.addValue(value);
			}

			GlobalPropertyDimension globalPropertyDimension = dimBuilder.build();

			// show that for each level the dimension properly assigns the value
			// to a global property data builder
			for (int level = 0; level < levelCount; level++) {
				/*
				 * Create a GlobalPropertiesPluginData, filling it with the test
				 * property definitions and any values that are required
				 */
				GlobalPropertiesPluginData.Builder pluginDataBuilder = GlobalPropertiesPluginData.builder();
				for (TestGlobalPropertyId propertyId : TestGlobalPropertyId.values()) {
					PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
					pluginDataBuilder.defineGlobalProperty(propertyId, propertyDefinition, 0.0);
					if (propertyDefinition.getDefaultValue().isEmpty()) {
						pluginDataBuilder.setGlobalPropertyValue(propertyId,
								propertyId.getRandomPropertyValue(randomGenerator), 0.0);
					}
				}

				// Create a dimension context that contain the plugin data
				// builder
				DimensionContext.Builder dimensionContextBuilder = DimensionContext.builder();

				pluginDataBuilder = (GlobalPropertiesPluginData.Builder) dimensionContextBuilder
						.add(pluginDataBuilder.build());
				DimensionContext dimensionContext = dimensionContextBuilder.build();

				// execute the dimension with the level
				globalPropertyDimension.executeLevel(dimensionContext, level);

				/*
				 * get the GlobalPropertiesPluginData from the corresponding
				 * builder
				 */
				GlobalPropertiesPluginData globalPropertiesPluginData = pluginDataBuilder.build();

				/*
				 * show that the GlobalPropertiesPluginData has the value we
				 * expect for the given level
				 */
				Optional<Object> optionalValue = globalPropertiesPluginData.getGlobalPropertyValue(targetPropertyId);
				assertTrue(optionalValue.isPresent());
				Object actualValue = optionalValue.get();
				Object expectedValue = exectedValues.get(level);
				assertEquals(expectedValue, actualValue);
			}
		}

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.class, name = "getExperimentMetaData", args = {})
	public void testGetExperimentMetaData() {
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {

			List<String> expectedExperimentMetaData = new ArrayList<>();
			expectedExperimentMetaData.add(testGlobalPropertyId.toString());

			GlobalPropertyDimension.Builder builder = GlobalPropertyDimension.builder()//
					.setGlobalPropertyId(testGlobalPropertyId);

			GlobalPropertyDimension globalPropertyDimension = builder.build();
			assertEquals(expectedExperimentMetaData, globalPropertyDimension.getExperimentMetaData());
		}

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.class, name = "getGlobalPropertyId", args = {})
	public void testGetGlobalPropertyId() {
		for (TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {

			GlobalPropertyDimension.Builder builder = GlobalPropertyDimension.builder()//
					.setGlobalPropertyId(testGlobalPropertyId);

			GlobalPropertyDimension globalPropertyDimension = builder.build();
			assertEquals(testGlobalPropertyId, globalPropertyDimension.getGlobalPropertyId());
		}
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.class, name = "getValues", args = {})
	public void testGetValues() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4581428044056639458L);

		for (int i = 0; i < 50; i++) {
			List<Object> expectedValues = new ArrayList<>();

			GlobalPropertyDimension.Builder builder = GlobalPropertyDimension.builder()//
					.setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);
			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				double value = randomGenerator.nextDouble();
				expectedValues.add(value);
				builder.addValue(value);
			}
			GlobalPropertyDimension globalPropertyDimension = builder.build();

			List<Object> actualValues = globalPropertyDimension.getValues();
			assertEquals(expectedValues, actualValues);
		}

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.class, name = "levelCount", args = {})
	public void testLevelCount() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

		for (int i = 0; i < 50; i++) {

			GlobalPropertyDimension.Builder builder = GlobalPropertyDimension.builder()//
					.setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);
			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				double value = randomGenerator.nextDouble();
				builder.addValue(value);
			}
			GlobalPropertyDimension globalPropertyDimension = builder.build();

			assertEquals(n, globalPropertyDimension.levelCount());
		}
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDimension.class, name = "getAssignmentTime", args = {})
	public void testGetAssignmentTime() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1528599899244176790L);

		for (int i = 0; i < 50; i++) {
			GlobalPropertyDimension.Builder builder = GlobalPropertyDimension.builder()//
					.setGlobalPropertyId(TestGlobalPropertyId.GLOBAL_PROPERTY_6_DOUBLE_IMMUTABLE);
			double assignmentTime = randomGenerator.nextDouble();
			builder.setAssignmentTime(assignmentTime);
			GlobalPropertyDimension globalPropertyDimension = builder.build();

			assertEquals(assignmentTime, globalPropertyDimension.getAssignmentTime());
		}
	}

}
