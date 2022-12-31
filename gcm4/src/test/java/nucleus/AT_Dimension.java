package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestMethod;

public class AT_Dimension {

	@Test
	@UnitTestMethod(target = Dimension.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(Dimension.builder());
	}

	private void testGetMetaDataValues(String... values) {
		List<String> expectedMetaData = new ArrayList<>();
		for (String value : values) {
			expectedMetaData.add(value);
		}

		Dimension.Builder builder = Dimension.builder();
		for (String metaDatum : expectedMetaData) {
			builder.addMetaDatum(metaDatum);
		}
		Dimension dimension = builder.build();
		List<String> actualMetaData = dimension.getMetaData();
		assertEquals(expectedMetaData, actualMetaData);
	}

	@Test
	@UnitTestMethod(target = Dimension.class, name = "getMetaData", args = {})
	public void testGetMetatData() {
		// test several numbers and duplications of meta data
		testGetMetaDataValues();
		testGetMetaDataValues("A");
		testGetMetaDataValues("B", "A");
		testGetMetaDataValues("B", "B", "Z");
		testGetMetaDataValues("A", "B", "C", "A");
	}

	@Test
	@UnitTestMethod(target = Dimension.class, name = "getLevel", args = { int.class })
	public void testGetLevel() {

		Dimension.Builder builder = Dimension.builder();
		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		Dimension dimension = builder.build();

		for (int i = 0; i < dimension.size(); i++) {
			assertNotNull(dimension.getLevel(i));
		}

	}

	@Test
	@UnitTestMethod(target = Dimension.class, name = "size", args = {})
	public void testSize() {
		Dimension.Builder builder = Dimension.builder();
		Dimension dimension = builder.build();
		assertEquals(0, dimension.size());

		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		dimension = builder.build();
		assertEquals(1, dimension.size());

		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		dimension = builder.build();
		assertEquals(2, dimension.size());

	}

	@Test
	@UnitTestMethod(target = Dimension.Builder.class, name = "addMetaDatum", args = { String.class })
	public void testAddMetaDatum() {
		// test several numbers and duplications of meta data
		testGetMetaDataValues();
		testGetMetaDataValues("A");
		testGetMetaDataValues("B", "A");
		testGetMetaDataValues("B", "B", "Z");
		testGetMetaDataValues("A", "B", "C", "A");
	}

	@Test
	@UnitTestMethod(target = Dimension.Builder.class, name = "addLevel", args = { Function.class })
	public void testAddLevel() {
		Dimension.Builder builder = Dimension.builder();
		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		builder.addLevel((map) -> {
			return new ArrayList<>();
		});
		Dimension dimension = builder.build();

		for (int i = 0; i < dimension.size(); i++) {
			assertNotNull(dimension.getLevel(i));
		}

	}

	@Test
	@UnitTestMethod(target = Dimension.Builder.class, name = "build", args = {})
	public void testBuild() {
		// covered by other tests
	}

}
