package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_FunctionalDimension {

	@Test
	@UnitTestConstructor(target = FunctionalDimension.class, args = { FunctionalDimensionData.class })
	public void testConstructor() {
		FunctionalDimensionData functionalDimensionData = FunctionalDimensionData.builder()
				.addMetaDatum("A")//
				.addValue("Level_0", (c) -> {
					return new ArrayList<>();
				})//
				.build();
		FunctionalDimension functionalDimension = new FunctionalDimension(functionalDimensionData);
		assertNotNull(functionalDimension);
	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.class, name = "executeLevel", args = { DimensionContext.class,
			int.class })
	public void testExecuteLevel() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2924521933883974690L);

		// run several test cases
		for (int i = 0; i < 30; i++) {

			Map<Integer, List<String>> expectedScenarioMetaData = new LinkedHashMap<>();
			FunctionalDimensionData.Builder builder = FunctionalDimensionData.builder();

			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				List<String> randomTestData = getRandomTestData(randomGenerator);
				expectedScenarioMetaData.put(j, randomTestData);
				builder.addValue("Level_" + j, (map) -> {
					return randomTestData;
				});
			}

			FunctionalDimensionData dimensionData = builder.build();
			FunctionalDimension functionalDimension = new FunctionalDimension(dimensionData);

			for (int m = 0; m < functionalDimension.levelCount(); m++) {
				List<String> expectedValues = expectedScenarioMetaData.get(m);
				List<String> actualValues = functionalDimension.executeLevel(null, m);
				assertEquals(expectedValues, actualValues);
			}
		}
	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.class, name = "getDimensionData", args = {})
	public void testGetDimensionData() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8376720485839224759L);

		for (int i = 0; i < 30; i++) {
			FunctionalDimensionData randomFunctionalDimensionData = getRandomFunctionalDimensionData(randomGenerator);
			FunctionalDimension functionalDimension = new FunctionalDimension(randomFunctionalDimensionData);

			assertEquals(randomFunctionalDimensionData, functionalDimension.getDimensionData());
		}
	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.class, name = "getExperimentMetaData", args = {})
	public void testGetExperimentMetaData() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8376720485839224753L);

		for (int i = 0; i < 10; i++) {
			List<String> expectedMetaData = getRandomTestData(randomGenerator);
			FunctionalDimensionData.Builder dimDataBuilder = FunctionalDimensionData.builder();

			for (int j = 0; j < expectedMetaData.size(); j++) {
				String datum = expectedMetaData.get(j);
				dimDataBuilder.addMetaDatum(datum);
				dimDataBuilder.addValue("Level_" + j, (c) -> {
					List<String> result = new ArrayList<>();
					result.add(datum);
					return result;
				});
			}

			FunctionalDimensionData dimData = dimDataBuilder.build();
			FunctionalDimension functionalDimension = new FunctionalDimension(dimData);

			List<String> actualMetaData = functionalDimension.getExperimentMetaData();
			assertEquals(expectedMetaData, actualMetaData);
		}
	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.class, name = "levelCount", args = {})
	public void testLevelCount() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8913942504408118065L);

		for (int i = 0; i < 50; i++) {

			FunctionalDimensionData.Builder dimDataBuilder = FunctionalDimensionData.builder();

			int n = randomGenerator.nextInt(10);
			for (int j = 0; j < n; j++) {
				dimDataBuilder.addValue("Level_" + j, (c) -> {
					return new ArrayList<>();
				});
			}
			FunctionalDimensionData functionalDimensionData = dimDataBuilder.build();
			FunctionalDimension functionalDimension = new FunctionalDimension(functionalDimensionData);

			assertEquals(n, functionalDimension.levelCount());
		}
	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.class, name = "toString", args = {})
	public void testToString() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8376720485839224753L);

		FunctionalDimensionData randomFunctionalDimensionData = getRandomFunctionalDimensionData(randomGenerator);

		FunctionalDimension functionalDimension = new FunctionalDimension(randomFunctionalDimensionData);

		String actualValue = functionalDimension.toString();

		String expectedValue = "FunctionalDimension [functionalDimensionData="
				+ randomFunctionalDimensionData + "]";

		assertEquals(expectedValue, actualValue);
	}

	private FunctionalDimensionData getRandomFunctionalDimensionData(RandomGenerator randomGenerator) {

		String[] dataOptions = { "A", "B", "C", "D", "E" };
		FunctionalDimensionData.Builder dimDataBuilder = FunctionalDimensionData.builder();

		int n = randomGenerator.nextInt(10);
		for (int i = 0; i < n; i++) {
			String selectedDatum = dataOptions[randomGenerator.nextInt(5)];
			dimDataBuilder.addMetaDatum(selectedDatum);
			dimDataBuilder.addValue("Level_" + i, (c) -> {
				List<String> result = new ArrayList<>();
				result.add(selectedDatum);
				return result;
			});
		}

		return dimDataBuilder.build();
	}

	private List<String> getRandomTestData(RandomGenerator randomGenerator) {
		String[] dataOptions = { "A", "B", "C", "D", "E" };
		List<String> result = new ArrayList<>();

		int n = randomGenerator.nextInt(10);
		for (int i = 0; i < n; i++) {
			String selectedDatum = dataOptions[randomGenerator.nextInt(5)];
			result.add(selectedDatum);
		}

		return result;
	}
}
