package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;

public class AT_Dimension {

	@Test
	@UnitTestMethod(target = FunctionalDimension.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(FunctionalDimension.builder());
	}

	private void testGetMetaDataValues(String... values) {
		List<String> expectedMetaData = new ArrayList<>();
		for (String value : values) {
			expectedMetaData.add(value);
		}

		FunctionalDimension.Builder builder = FunctionalDimension.builder();
		for (String metaDatum : expectedMetaData) {
			builder.addMetaDatum(metaDatum);
		}
		FunctionalDimension dimension = builder.build();
		List<String> actualMetaData = dimension.getExperimentMetaData();
		assertEquals(expectedMetaData, actualMetaData);
	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.class, name = "getExperimentMetaData", args = {})
	public void testGetExperimentMetaData() {
		// test several numbers and duplications of meta data
		testGetMetaDataValues();
		testGetMetaDataValues("A");
		testGetMetaDataValues("B", "A");
		testGetMetaDataValues("B", "B", "Z");
		testGetMetaDataValues("A", "B", "C", "A");
	}

	private List<String> getValuesAsList(String... values) {
		List<String> result = new ArrayList<>();
		for (String value : values) {
			result.add(value);
		}
		return result;
	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.class, name = "executeLevel", args = { DimensionContext.class, int.class })
	public void testExecuteLevel() {

		Map<Integer, List<String>> expectedScenarioMetaData = new LinkedHashMap<>();
		expectedScenarioMetaData.put(0, getValuesAsList("A"));
		expectedScenarioMetaData.put(1, getValuesAsList("A", "B"));
		expectedScenarioMetaData.put(2, getValuesAsList("A", "B", "C"));
		expectedScenarioMetaData.put(3, getValuesAsList("A", "B", "C", "D"));

		FunctionalDimension.Builder builder = FunctionalDimension.builder();
		for (Integer i : expectedScenarioMetaData.keySet()) {
			builder.addLevel((map) -> {
				return expectedScenarioMetaData.get(i);
			});
		}

		FunctionalDimension dimension = builder.build();

		for (int i = 0; i < dimension.levelCount(); i++) {
			List<String> expectedValues = expectedScenarioMetaData.get(i);
			List<String> actualValues = dimension.executeLevel(null, i);
			assertEquals(expectedValues, actualValues);
		}

	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.class, name = "levelCount", args = {})
	public void testLevelCount() {

		FunctionalDimension dimension = FunctionalDimension.builder().build();
		assertEquals(0, dimension.levelCount());

		dimension = FunctionalDimension	.builder()//
										.addLevel((map) -> {
											return new ArrayList<>();
										})//
										.build();
		assertEquals(1, dimension.levelCount());

		dimension = FunctionalDimension	.builder()//
										.addLevel((map) -> {
											return new ArrayList<>();
										})//
										.addLevel((map) -> {
											return new ArrayList<>();
										})//
										.build();
		assertEquals(2, dimension.levelCount());
	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.Builder.class, name = "addMetaDatum", args = { String.class })
	public void testAddMetaDatum() {
		// test several numbers and duplications of meta data
		testGetMetaDataValues();
		testGetMetaDataValues("A");
		testGetMetaDataValues("B", "A");
		testGetMetaDataValues("B", "B", "Z");
		testGetMetaDataValues("A", "B", "C", "A");
	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.Builder.class, name = "addLevel", args = { Function.class })
	public void testAddLevel() {
		Map<Integer, List<String>> expectedScenarioMetaData = new LinkedHashMap<>();
		expectedScenarioMetaData.put(0, getValuesAsList("A"));
		expectedScenarioMetaData.put(1, getValuesAsList("A", "B"));
		expectedScenarioMetaData.put(2, getValuesAsList("A", "B", "C"));
		expectedScenarioMetaData.put(3, getValuesAsList("A", "B", "C", "D"));

		FunctionalDimension.Builder builder = FunctionalDimension.builder();
		for (Integer i : expectedScenarioMetaData.keySet()) {
			builder.addLevel((map) -> {
				return expectedScenarioMetaData.get(i);
			});
		}

		FunctionalDimension dimension = builder.build();

		for (int i = 0; i < dimension.levelCount(); i++) {
			List<String> expectedValues = expectedScenarioMetaData.get(i);
			List<String> actualValues = dimension.executeLevel(null, i);
			assertEquals(expectedValues, actualValues);
		}
	}

	@Test
	@UnitTestMethod(target = FunctionalDimension.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(FunctionalDimension.builder().build());
	}

}
