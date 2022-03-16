package plugins.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import nucleus.util.ContractException;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonError;

@UnitTest(target = PeoplePluginData.class)
public final class AT_PeoplePluginData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PeoplePluginData.builder());
	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		assertTrue(peoplePluginData.getBulkPersonConstructionDatas().isEmpty());

		Set<BulkPersonConstructionData> expectedBulkPersonConstructionDatas = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {

			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData	.builder()//
										.add(PersonContructionData.builder().build())//
										.add(PersonContructionData.builder().build())//
										.add(PersonContructionData.builder().build())//
										.build();//

			expectedBulkPersonConstructionDatas.add(bulkPersonConstructionData);
		}
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		for (BulkPersonConstructionData bulkPersonConstructionData : expectedBulkPersonConstructionDatas) {
			builder.addBulkPersonContructionData(bulkPersonConstructionData);
		}

		peoplePluginData = builder.build();
		assertEquals(expectedBulkPersonConstructionDatas, new LinkedHashSet<>(peoplePluginData.getBulkPersonConstructionDatas()));
	}

	@Test
	@UnitTestMethod(target = PeoplePluginData.Builder.class, name = "addBulkPersonContructionData", args = { BulkPersonConstructionData.class })
	public void testAddBulkPersonContructionData() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		assertTrue(peoplePluginData.getBulkPersonConstructionDatas().isEmpty());

		Set<BulkPersonConstructionData> expectedBulkPersonConstructionDatas = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {

			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData	.builder()//
										.add(PersonContructionData.builder().build())//
										.add(PersonContructionData.builder().build())//
										.add(PersonContructionData.builder().build())//
										.build();//

			expectedBulkPersonConstructionDatas.add(bulkPersonConstructionData);
		}
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		for (BulkPersonConstructionData bulkPersonConstructionData : expectedBulkPersonConstructionDatas) {
			builder.addBulkPersonContructionData(bulkPersonConstructionData);
		}

		peoplePluginData = builder.build();
		assertEquals(expectedBulkPersonConstructionDatas, new LinkedHashSet<>(peoplePluginData.getBulkPersonConstructionDatas()));

		// precondition tests
		
		ContractException contractException = assertThrows(ContractException.class, () -> builder.addBulkPersonContructionData(null));
		assertEquals(PersonError.NULL_BULK_PERSON_CONTRUCTION_DATA, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getBulkPersonConstructionDatas", args = {})
	public void testGetBulkPersonConstructionDatas() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		assertTrue(peoplePluginData.getBulkPersonConstructionDatas().isEmpty());

		Set<BulkPersonConstructionData> expectedBulkPersonConstructionDatas = new LinkedHashSet<>();
		for (int i = 0; i < 10; i++) {

			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData	.builder()//
										.add(PersonContructionData.builder().build())//
										.add(PersonContructionData.builder().build())//
										.add(PersonContructionData.builder().build())//
										.build();//

			expectedBulkPersonConstructionDatas.add(bulkPersonConstructionData);
		}
		PeoplePluginData.Builder builder = PeoplePluginData.builder();
		for (BulkPersonConstructionData bulkPersonConstructionData : expectedBulkPersonConstructionDatas) {
			builder.addBulkPersonContructionData(bulkPersonConstructionData);
		}

		peoplePluginData = builder.build();
		assertEquals(expectedBulkPersonConstructionDatas, new LinkedHashSet<>(peoplePluginData.getBulkPersonConstructionDatas()));



	}

}
