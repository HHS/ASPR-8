package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_PersonPropertyInteractionReportPluginData {

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		PersonPropertyInteractionReportPluginData.Builder builder = PersonPropertyInteractionReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.Builder.class, name = "setReportLabel", args = {
			ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = //
					PersonPropertyInteractionReportPluginData.builder()//
							.setReportPeriod(ReportPeriod.DAILY)//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, personPropertyInteractionReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInteractionReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.Builder.class, name = "setReportPeriod", args = {
			ReportPeriod.class })
	public void testSetReportPeriod() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = //
					PersonPropertyInteractionReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel)//
							.build();

			assertEquals(reportPeriod, personPropertyInteractionReportPluginData.getReportPeriod());
		}

		// precondition: if the report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInteractionReportPluginData.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.Builder.class, name = "removePersonPropertyId", args = {
			PersonPropertyId.class })
	public void testRemovePersonPropertyId() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");

		Set<TestPersonPropertyId> expectedPropertyIds = new LinkedHashSet<>();
		expectedPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);

		PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = //
				PersonPropertyInteractionReportPluginData.builder()//
						.setReportPeriod(ReportPeriod.DAILY)//
						.setReportLabel(reportLabel)//
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK)
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK)
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)
						.removePersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK)
						.build();

		assertEquals(expectedPropertyIds, personPropertyInteractionReportPluginData.getPersonPropertyIds());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.Builder.class, name = "addPersonPropertyId", args = {
			PersonPropertyId.class })
	public void testAddPersonPropertyId() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");

		Set<TestPersonPropertyId> expectedPropertyIds = new LinkedHashSet<>();
		expectedPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);
		expectedPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);

		PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = //
				PersonPropertyInteractionReportPluginData.builder()//
						.setReportPeriod(ReportPeriod.DAILY)//
						.setReportLabel(reportLabel)//
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK)
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK)
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK)
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK).build();

		assertEquals(expectedPropertyIds, personPropertyInteractionReportPluginData.getPersonPropertyIds());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6316245099247321601L);
		for (int i = 0; i < 10; i++) {

			// build a PersonPropertyReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];

			PersonPropertyInteractionReportPluginData.Builder builder = //
					PersonPropertyInteractionReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel);

			for (int j = 0; j < 10; j++) {
				TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId
						.getRandomPersonPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder.addPersonPropertyId(testPersonPropertyId);
				}
			}
			builder.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);

			PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = builder.build();

			// show that the returned clone builder will build an identical instance if no
			// mutations are made
			PersonPropertyInteractionReportPluginData.Builder cloneBuilder = personPropertyInteractionReportPluginData
					.toBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(personPropertyInteractionReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// addPersonPropertyId
			cloneBuilder = personPropertyInteractionReportPluginData.toBuilder();
			cloneBuilder.addPersonPropertyId(TestPersonPropertyId.getUnknownPersonPropertyId());
			assertNotEquals(personPropertyInteractionReportPluginData, cloneBuilder.build());

			// removePersonPropertyId
			cloneBuilder = personPropertyInteractionReportPluginData.toBuilder();
			cloneBuilder.removePersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
			assertNotEquals(personPropertyInteractionReportPluginData, cloneBuilder.build());

			// setReportLabel
			cloneBuilder = personPropertyInteractionReportPluginData.toBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(personPropertyInteractionReportPluginData, cloneBuilder.build());

			// setReportPeriod
			cloneBuilder = personPropertyInteractionReportPluginData.toBuilder();
			ReportPeriod nextReportPeriod = personPropertyInteractionReportPluginData.getReportPeriod().next();
			cloneBuilder.setReportPeriod(nextReportPeriod);
			assertNotEquals(personPropertyInteractionReportPluginData, cloneBuilder.build());

		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.class, name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");

		Set<TestPersonPropertyId> expectedPropertyIds = new LinkedHashSet<>();
		expectedPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedPropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);

		PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = //
				PersonPropertyInteractionReportPluginData.builder()//
						.setReportPeriod(ReportPeriod.DAILY)//
						.setReportLabel(reportLabel)//
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK)
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK)
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK)
						.removePersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK)
						.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK).build();

		assertEquals(expectedPropertyIds, personPropertyInteractionReportPluginData.getPersonPropertyIds());
	}

	private PersonPropertyInteractionReportPluginData getRandomPersonPropertyInteractionReportPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		PersonPropertyInteractionReportPluginData.Builder builder = PersonPropertyInteractionReportPluginData.builder();
		ReportLabel reportLabel = new SimpleReportLabel("report label" + randomGenerator.nextInt(100));
		builder.setReportLabel(reportLabel);
		int count = ReportPeriod.values().length;
		int index = randomGenerator.nextInt(count);
		ReportPeriod reportPeriod = ReportPeriod.values()[index];
		builder.setReportPeriod(reportPeriod);

		boolean first = true;
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId
				.getShuffledPersonPropertyIds(randomGenerator)) {
			if (first) {
				builder.addPersonPropertyId(testPersonPropertyId);
				first = false;
			} else {
				if (randomGenerator.nextBoolean()) {
					builder.addPersonPropertyId(testPersonPropertyId);
				}
			}
		}

		return builder.build();
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		PersonPropertyInteractionReportPluginData pluginData = getRandomPersonPropertyInteractionReportPluginData(0);

		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.class, name = "checkVersionSupported", args = {
			String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(PersonPropertyInteractionReportPluginData.checkVersionSupported(version));
			assertFalse(PersonPropertyInteractionReportPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(PersonPropertyInteractionReportPluginData.checkVersionSupported("badVersion"));
			assertFalse(PersonPropertyInteractionReportPluginData.checkVersionSupported(version + "0"));
			assertFalse(PersonPropertyInteractionReportPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9157025935584862941L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			PersonPropertyInteractionReportPluginData reportPluginData = getRandomPersonPropertyInteractionReportPluginData(
					randomGenerator.nextLong());
			assertFalse(reportPluginData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			PersonPropertyInteractionReportPluginData reportPluginData = getRandomPersonPropertyInteractionReportPluginData(
					randomGenerator.nextLong());
			assertFalse(reportPluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			PersonPropertyInteractionReportPluginData reportPluginData = getRandomPersonPropertyInteractionReportPluginData(
					randomGenerator.nextLong());
			assertTrue(reportPluginData.equals(reportPluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			PersonPropertyInteractionReportPluginData reportPluginData1 = getRandomPersonPropertyInteractionReportPluginData(
					seed);
			PersonPropertyInteractionReportPluginData reportPluginData2 = getRandomPersonPropertyInteractionReportPluginData(
					seed);
			assertFalse(reportPluginData1 == reportPluginData2);
			for (int j = 0; j < 10; j++) {
				assertTrue(reportPluginData1.equals(reportPluginData2));
				assertTrue(reportPluginData2.equals(reportPluginData1));
			}
		}

		// different inputs yield unequal objects.
		Set<PersonPropertyInteractionReportPluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PersonPropertyInteractionReportPluginData pluginData = getRandomPersonPropertyInteractionReportPluginData(
					randomGenerator.nextLong());
			set.add(pluginData);
		}

		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4074270846326821416L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			PersonPropertyInteractionReportPluginData reportPluginData1 = getRandomPersonPropertyInteractionReportPluginData(
					seed);
			PersonPropertyInteractionReportPluginData reportPluginData2 = getRandomPersonPropertyInteractionReportPluginData(
					seed);
			assertEquals(reportPluginData1, reportPluginData2);
			assertEquals(reportPluginData1.hashCode(), reportPluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			PersonPropertyInteractionReportPluginData pluginData = getRandomPersonPropertyInteractionReportPluginData(
					randomGenerator.nextLong());
			hashCodes.add(pluginData.hashCode());
		}
		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReportPluginData.class, name = "toString", args = {})
	public void testToString() {
		PersonPropertyInteractionReportPluginData reportPluginData = getRandomPersonPropertyInteractionReportPluginData(
				7710973343170558582L);

		String actualValue = reportPluginData.toString();

		String expectedValue = "PersonPropertyInteractionReportPluginData [data=Data ["
				+ "reportLabel=SimpleReportLabel [value=report label95], "
				+ "reportPeriod=WEEKLY, "
				+ "personPropertyIds=["
				+ "PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, "
				+ "PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, "
				+ "PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, "
				+ "PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK]]]";
		assertEquals(expectedValue, actualValue);

	}

}
