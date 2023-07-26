package gov.hhs.aspr.ms.gcm.plugins.personproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_PersonPropertyReportPluginData {

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report period is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		PersonPropertyReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel(getClass()))//
				.build());
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// precondition test: if the report label is not assigned
		contractException = assertThrows(ContractException.class, () -> //
		PersonPropertyReportPluginData.builder()//
				.setReportPeriod(ReportPeriod.DAILY)//
				.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.Builder.class, name = "setReportLabel", args = {
			ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			PersonPropertyReportPluginData personPropertyReportPluginData = //
					PersonPropertyReportPluginData.builder()//
							.setReportPeriod(ReportPeriod.DAILY)//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, personPropertyReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.Builder.class, name = "setReportPeriod", args = {
			ReportPeriod.class })
	public void testSetReportPeriod() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			PersonPropertyReportPluginData personPropertyReportPluginData = //
					PersonPropertyReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel)//
							.build();

			assertEquals(reportPeriod, personPropertyReportPluginData.getReportPeriod());
		}

		// precondition: if the report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReportPluginData.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.Builder.class, name = "setDefaultInclusion", args = {
			boolean.class })
	public void testSetDefaultInclusion() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		PersonPropertyReportPluginData personPropertyReportPluginData = //
				PersonPropertyReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertEquals(true, personPropertyReportPluginData.getDefaultInclusionPolicy());

		personPropertyReportPluginData = //
				PersonPropertyReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(true)//
						.build();
		assertEquals(true, personPropertyReportPluginData.getDefaultInclusionPolicy());

		personPropertyReportPluginData = //
				PersonPropertyReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(false)//
						.build();
		assertEquals(false, personPropertyReportPluginData.getDefaultInclusionPolicy());

	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.Builder.class, name = "includePersonProperty", args = {
			PersonPropertyId.class })
	public void testIncludePersonProperty() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		PersonPropertyReportPluginData personPropertyReportPluginData = //
				PersonPropertyReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(personPropertyReportPluginData.getIncludedProperties().isEmpty());

		// show that inclusion alone works
		Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();

		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
			builder.includePersonProperty(personPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedPersonPropertyIds, personPropertyReportPluginData.getIncludedProperties());

		// show that inclusion will override exclusion
		expectedPersonPropertyIds.clear();

		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		builder = PersonPropertyReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
			builder.excludePersonProperty(personPropertyId);
			builder.includePersonProperty(personPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedPersonPropertyIds, personPropertyReportPluginData.getIncludedProperties());

		// precondition: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReportPluginData.builder().includePersonProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.Builder.class, name = "excludePersonProperty", args = {
			PersonPropertyId.class })
	public void testExcludePersonProperty() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-exclusion
		PersonPropertyReportPluginData personPropertyReportPluginData = //
				PersonPropertyReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(personPropertyReportPluginData.getExcludedProperties().isEmpty());

		// show that exclusion alone works
		Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();

		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
			builder.excludePersonProperty(personPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedPersonPropertyIds, personPropertyReportPluginData.getExcludedProperties());

		// show that exclusion will override inclusion
		expectedPersonPropertyIds.clear();

		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		builder = PersonPropertyReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
			builder.includePersonProperty(personPropertyId);
			builder.excludePersonProperty(personPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedPersonPropertyIds, personPropertyReportPluginData.getExcludedProperties());

		// precondition: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReportPluginData.builder().excludePersonProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			PersonPropertyReportPluginData personPropertyReportPluginData = //
					PersonPropertyReportPluginData.builder()//
							.setReportPeriod(ReportPeriod.DAILY)//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, personPropertyReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.class, name = "getReportPeriod", args = {})
	public void testGetReportPeriod() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			PersonPropertyReportPluginData personPropertyReportPluginData = //
					PersonPropertyReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel)//
							.build();

			assertEquals(reportPeriod, personPropertyReportPluginData.getReportPeriod());
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.class, name = "getIncludedProperties", args = {})
	public void testGetIncludedProperties() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-inclusion
		PersonPropertyReportPluginData personPropertyReportPluginData = //
				PersonPropertyReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(personPropertyReportPluginData.getIncludedProperties().isEmpty());

		// show that inclusion alone works
		Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();

		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
			builder.includePersonProperty(personPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedPersonPropertyIds, personPropertyReportPluginData.getIncludedProperties());

		// show that inclusion will override exclusion
		expectedPersonPropertyIds.clear();

		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		builder = PersonPropertyReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
			builder.excludePersonProperty(personPropertyId);
			builder.includePersonProperty(personPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedPersonPropertyIds, personPropertyReportPluginData.getIncludedProperties());

	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.class, name = "getExcludedProperties", args = {})
	public void testGetExcludedProperties() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-exclusion
		PersonPropertyReportPluginData personPropertyReportPluginData = //
				PersonPropertyReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(personPropertyReportPluginData.getExcludedProperties().isEmpty());

		// show that exclusion alone works
		Set<PersonPropertyId> expectedPersonPropertyIds = new LinkedHashSet<>();

		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
			builder.excludePersonProperty(personPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedPersonPropertyIds, personPropertyReportPluginData.getExcludedProperties());

		// show that exclusion will override inclusion
		expectedPersonPropertyIds.clear();

		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
		expectedPersonPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		builder = PersonPropertyReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (PersonPropertyId personPropertyId : expectedPersonPropertyIds) {
			builder.includePersonProperty(personPropertyId);
			builder.excludePersonProperty(personPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedPersonPropertyIds, personPropertyReportPluginData.getExcludedProperties());

	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.class, name = "getDefaultInclusionPolicy", args = {})
	public void testGetDefaultInclusionPolicy() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		PersonPropertyReportPluginData personPropertyReportPluginData = //
				PersonPropertyReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertEquals(true, personPropertyReportPluginData.getDefaultInclusionPolicy());

		personPropertyReportPluginData = //
				PersonPropertyReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(true)//
						.build();
		assertEquals(true, personPropertyReportPluginData.getDefaultInclusionPolicy());

		personPropertyReportPluginData = //
				PersonPropertyReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(false)//
						.build();
		assertEquals(false, personPropertyReportPluginData.getDefaultInclusionPolicy());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {

			// build a PersonPropertyReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];

			PersonPropertyReportPluginData.Builder builder = //
					PersonPropertyReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel);

			for (int j = 0; j < 10; j++) {
				TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId
						.getRandomPersonPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder.includePersonProperty(testPersonPropertyId);
				} else {
					builder.excludePersonProperty(testPersonPropertyId);
				}
			}

			builder.setDefaultInclusion(randomGenerator.nextBoolean()).build();

			PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

			// create the clone builder and have it build
			PersonPropertyReportPluginData clonePersonPropertyReportPluginData = personPropertyReportPluginData
					.getCloneBuilder().build();

			// the result should equal the original if the clone builder was
			// initialized with the correct state
			assertEquals(personPropertyReportPluginData, clonePersonPropertyReportPluginData);

		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a PersonPropertyReportPluginData from the same random
			// inputs
			PersonPropertyReportPluginData.Builder builder1 = PersonPropertyReportPluginData.builder();
			PersonPropertyReportPluginData.Builder builder2 = PersonPropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);
			builder2.setReportPeriod(reportPeriod);

			for (int j = 0; j < 10; j++) {
				TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId
						.getRandomPersonPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder1.includePersonProperty(testPersonPropertyId);
					builder2.includePersonProperty(testPersonPropertyId);
				} else {
					builder1.excludePersonProperty(testPersonPropertyId);
					builder2.excludePersonProperty(testPersonPropertyId);
				}
			}

			boolean defaultInclusion = randomGenerator.nextBoolean();
			builder1.setDefaultInclusion(defaultInclusion).build();
			builder2.setDefaultInclusion(defaultInclusion).build();

			PersonPropertyReportPluginData personPropertyReportPluginData1 = builder1.build();
			PersonPropertyReportPluginData personPropertyReportPluginData2 = builder2.build();

			assertEquals(personPropertyReportPluginData1, personPropertyReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the default inclusion
			personPropertyReportPluginData2 = //
					personPropertyReportPluginData1.getCloneBuilder()//
							.setDefaultInclusion(!defaultInclusion)//
							.build();
			assertNotEquals(personPropertyReportPluginData2, personPropertyReportPluginData1);

			// change the report period
			int ord = reportPeriod.ordinal() + 1;
			ord = ord % ReportPeriod.values().length;
			reportPeriod = ReportPeriod.values()[ord];
			personPropertyReportPluginData2 = //
					personPropertyReportPluginData1.getCloneBuilder()//
							.setReportPeriod(reportPeriod)//
							.build();
			assertNotEquals(personPropertyReportPluginData2, personPropertyReportPluginData1);

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			personPropertyReportPluginData2 = //
					personPropertyReportPluginData1.getCloneBuilder()//
							.setReportLabel(reportLabel)//
							.build();
			assertNotEquals(personPropertyReportPluginData2, personPropertyReportPluginData1);

			// change an included property id
			if (!personPropertyReportPluginData1.getIncludedProperties().isEmpty()) {
				PersonPropertyId personPropertyId = personPropertyReportPluginData1.getIncludedProperties().iterator()
						.next();
				personPropertyReportPluginData2 = //
						personPropertyReportPluginData1.getCloneBuilder()//
								.excludePersonProperty(personPropertyId)//
								.build();
				assertNotEquals(personPropertyReportPluginData2, personPropertyReportPluginData1);
			}
			// change an excluded property id
			if (!personPropertyReportPluginData1.getExcludedProperties().isEmpty()) {
				PersonPropertyId personPropertyId = personPropertyReportPluginData1.getExcludedProperties().iterator()
						.next();
				personPropertyReportPluginData2 = //
						personPropertyReportPluginData1.getCloneBuilder()//
								.includePersonProperty(personPropertyId)//
								.build();
				assertNotEquals(personPropertyReportPluginData2, personPropertyReportPluginData1);
			}

		}

	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a PersonPropertyReportPluginData from the same random
			// inputs
			PersonPropertyReportPluginData.Builder builder1 = PersonPropertyReportPluginData.builder();
			PersonPropertyReportPluginData.Builder builder2 = PersonPropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);
			builder2.setReportPeriod(reportPeriod);

			for (int j = 0; j < 10; j++) {
				TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId
						.getRandomPersonPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder1.includePersonProperty(testPersonPropertyId);
					builder2.includePersonProperty(testPersonPropertyId);
				} else {
					builder1.excludePersonProperty(testPersonPropertyId);
					builder2.excludePersonProperty(testPersonPropertyId);
				}
			}

			boolean defaultInclusion = randomGenerator.nextBoolean();
			builder1.setDefaultInclusion(defaultInclusion).build();
			builder2.setDefaultInclusion(defaultInclusion).build();

			PersonPropertyReportPluginData personPropertyReportPluginData1 = builder1.build();
			PersonPropertyReportPluginData personPropertyReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = personPropertyReportPluginData1.hashCode();
			assertEquals(hashCode, personPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, personPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, personPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, personPropertyReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(personPropertyReportPluginData1.hashCode(), personPropertyReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(personPropertyReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are unique
		 * values -- this is dependent on the random seed
		 */
		assertEquals(50, observedHashCodes.size());

	}

	@Test
	@UnitTestMethod(target = PersonPropertyReportPluginData.class, name = "toString", args = {})
	public void testToString() {
		PersonPropertyReportPluginData personPropertyReportPluginData = PersonPropertyReportPluginData.builder()//
				.setDefaultInclusion(true)//
				.setReportLabel(new SimpleReportLabel("report label"))//
				.setReportPeriod(ReportPeriod.DAILY)//
				.includePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK)//
				.includePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)//
				.excludePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK)//
				.excludePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK).build();//

		String actualValue = personPropertyReportPluginData.toString();

		String expectedValue = "PersonPropertyReportPluginData ["
				+ "data=Data ["
				+ "reportLabel=SimpleReportLabel [value=report label], "
				+ "reportPeriod=DAILY, "
				+ "includedProperties=[PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK], "
				+ "excludedProperties=[PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK], "
				+ "defaultInclusionPolicy=true]]";

		assertEquals(expectedValue, actualValue);

	}
}
