package plugins.people.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = BulkPersonConstructionData.class)
public final class AT_BulkPersonConstructionData implements Event {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(BulkPersonConstructionData.builder());
	}

	@Test
	@UnitTestMethod(target = BulkPersonConstructionData.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(BulkPersonConstructionData.builder().build());
	}

	@Test
	@UnitTestMethod(target = BulkPersonConstructionData.Builder.class, name = "add", args = { PersonConstructionData.class })
	public void testAdd() {
		BulkPersonConstructionData.Builder builder = BulkPersonConstructionData.builder();

		List<PersonConstructionData> expectedPersonConstructionData = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			expectedPersonConstructionData.add(PersonConstructionData.builder().add(i).build());
		}

		for (PersonConstructionData personConstructionData : expectedPersonConstructionData) {
			builder.add(personConstructionData);
		}

		// show the person construction data returned match the ones contributed
		// in the correct order
		assertEquals(expectedPersonConstructionData, builder.build().getPersonConstructionDatas());
	}

	@Test
	@UnitTestMethod(target = BulkPersonConstructionData.Builder.class, name = "addAuxiliaryData", args = { Object.class })
	public void testAddAuxiliaryData() {
		Map<Class<?>, List<Object>> expectedValues = new LinkedHashMap<>();

		List<Object> values = new ArrayList<>();
		values.add("aux1");
		values.add(15);
		values.add(3);
		values.add("aux2");
		values.add("aux1");
		values.add(3);
		values.add(false);

		/*
		 * Fill the builder and add to the expected values map
		 */
		BulkPersonConstructionData.Builder builder = BulkPersonConstructionData.builder();
		for (Object value : values) {
			builder.addAuxiliaryData(value);
			List<Object> list = expectedValues.get(value.getClass());
			if (list == null) {
				list = new ArrayList<>();
				expectedValues.put(value.getClass(), list);
			}
			list.add(value);
		}

		/*
		 * Show that the bulk construction data returns the auxiliary data by
		 * type and in the correct order
		 */
		BulkPersonConstructionData bulkPersonConstructionData = builder.build();
		for (Class<?> c : expectedValues.keySet()) {
			List<Object> expectedList = expectedValues.get(c);
			List<?> actualList = bulkPersonConstructionData.getValues(c);
			assertEquals(expectedList, actualList);
		}

		// show that types that have no values return an empty list
		assertTrue(bulkPersonConstructionData.getValues(Double.class).isEmpty());

		ContractException contractException = assertThrows(ContractException.class, () -> BulkPersonConstructionData.builder().addAuxiliaryData(null));
		assertEquals(PersonError.NULL_AUXILIARY_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getPersonConstructionDatas", args = {})
	public void testGetPersonConstructionDatas() {
		BulkPersonConstructionData.Builder builder = BulkPersonConstructionData.builder();

		List<PersonConstructionData> expectedPersonConstructionData = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			expectedPersonConstructionData.add(PersonConstructionData.builder().add(i).build());
		}

		for (PersonConstructionData personConstructionData : expectedPersonConstructionData) {
			builder.add(personConstructionData);
		}

		// show the person construction data returned match the ones contributed
		// in the correct order
		assertEquals(expectedPersonConstructionData, builder.build().getPersonConstructionDatas());
	}

	@Test
	@UnitTestMethod(name = "getValue", args = { Class.class })
	public void testGetValue() {
		Map<Class<?>, List<Object>> expectedValues = new LinkedHashMap<>();

		List<Object> values = new ArrayList<>();
		values.add("aux1");
		values.add(15);
		values.add(3);
		values.add("aux2");
		values.add("aux1");
		values.add(3);
		values.add(false);

		/*
		 * Fill the builder and add to the expected values map
		 */
		BulkPersonConstructionData.Builder builder = BulkPersonConstructionData.builder();
		for (Object value : values) {
			builder.addAuxiliaryData(value);
			List<Object> list = expectedValues.get(value.getClass());
			if (list == null) {
				list = new ArrayList<>();
				expectedValues.put(value.getClass(), list);
			}
			list.add(value);
		}

		/*
		 * Show that the bulk construction data returns the first auxiliary data
		 * by type
		 * 
		 */
		BulkPersonConstructionData bulkPersonConstructionData = builder.build();
		for (Class<?> c : expectedValues.keySet()) {
			Object expectedValue = expectedValues.get(c).get(0);
			Optional<?> optional = bulkPersonConstructionData.getValue(c);
			assertTrue(optional.isPresent());
			Object actualValue = optional.get();
			assertEquals(expectedValue, actualValue);
		}

		// show that types not contained return

		Optional<Double> optional = bulkPersonConstructionData.getValue(Double.class);
		assertFalse(optional.isPresent());

	}

	@Test
	@UnitTestMethod(name = "build", args = { Class.class })
	public void testGetValues() {
		Map<Class<?>, List<Object>> expectedValues = new LinkedHashMap<>();

		List<Object> values = new ArrayList<>();
		values.add("aux1");
		values.add(15);
		values.add(3);
		values.add("aux2");
		values.add("aux1");
		values.add(3);
		values.add(false);

		/*
		 * Fill the builder and add to the expected values map
		 */
		BulkPersonConstructionData.Builder builder = BulkPersonConstructionData.builder();
		for (Object value : values) {
			builder.addAuxiliaryData(value);
			List<Object> list = expectedValues.get(value.getClass());
			if (list == null) {
				list = new ArrayList<>();
				expectedValues.put(value.getClass(), list);
			}
			list.add(value);
		}

		/*
		 * Show that the bulk construction data returns the auxiliary data by
		 * type and in the correct order
		 */
		BulkPersonConstructionData bulkPersonConstructionData = builder.build();
		for (Class<?> c : expectedValues.keySet()) {
			List<Object> expectedList = expectedValues.get(c);
			List<?> actualList = bulkPersonConstructionData.getValues(c);
			assertEquals(expectedList, actualList);
		}

		// show that types that have no values return an empty list
		assertTrue(bulkPersonConstructionData.getValues(Double.class).isEmpty());
	}
}
