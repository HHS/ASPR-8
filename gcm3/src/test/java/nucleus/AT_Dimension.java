package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.MathArrays.Function;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = Dimension.class)
public class AT_Dimension {

	@Test
	@UnitTestMethod(name = "builder", args = {})
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
	@UnitTestMethod(name = "getMetaData", args = {})
	public void testGetMetatData() {
		// test several numbers and duplications of meta data
		testGetMetaDataValues();
		testGetMetaDataValues("A");
		testGetMetaDataValues("B", "A");
		testGetMetaDataValues("B", "B", "Z");
		testGetMetaDataValues("A", "B", "C", "A");
	}

	@Test
	@UnitTestMethod(name = "getPoint", args = {})
	public void testGetPoint() {

		Dimension.Builder builder = Dimension.builder();
		builder.addPoint((map) -> {
			return new ArrayList<>();
		});
		builder.addPoint((map) -> {
			return new ArrayList<>();
		});
		builder.addPoint((map) -> {
			return new ArrayList<>();
		});
		builder.addPoint((map) -> {
			return new ArrayList<>();
		});
		Dimension dimension = builder.build();

		for (int i = 0; i < dimension.size(); i++) {
			assertNotNull(dimension.getPoint(i));
		}

	}

	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		Dimension.Builder builder = Dimension.builder();
		Dimension dimension = builder.build();
		assertEquals(0, dimension.size());

		builder.addPoint((map) -> {
			return new ArrayList<>();
		});
		dimension = builder.build();
		assertEquals(1, dimension.size());

		builder.addPoint((map) -> {
			return new ArrayList<>();
		});
		builder.addPoint((map) -> {
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
	@UnitTestMethod(target = Dimension.Builder.class, name = "addPoint", args = { Function.class, List.class })
	public void testAddPoint() {
		Dimension.Builder builder = Dimension.builder();
		builder.addPoint((map) -> {
			return new ArrayList<>();
		});
		builder.addPoint((map) -> {
			return new ArrayList<>();
		});
		builder.addPoint((map) -> {
			return new ArrayList<>();
		});
		builder.addPoint((map) -> {
			return new ArrayList<>();
		});
		Dimension dimension = builder.build();

		for (int i = 0; i < dimension.size(); i++) {
			assertNotNull(dimension.getPoint(i));
		}
	}

	@Test
	@UnitTestMethod(target = Dimension.Builder.class, name = "build", args = {})
	public void testBuild() {
		//covered by other tests
	}

}
