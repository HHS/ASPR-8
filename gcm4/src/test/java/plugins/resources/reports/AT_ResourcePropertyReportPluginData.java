package plugins.resources.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_ResourcePropertyReportPluginData {

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		PersonResourceReportPluginData.Builder builder = PersonResourceReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report period is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		PersonResourceReportPluginData	.builder()//
										.setReportLabel(new SimpleReportLabel(getClass()))//
										.build());
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// precondition test: if the report label is not assigned
		contractException = assertThrows(ContractException.class, () -> //
		PersonResourceReportPluginData	.builder()//
										.setReportPeriod(ReportPeriod.DAILY)//
										.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			PersonResourceReportPluginData personResourceReportPluginData = //
					PersonResourceReportPluginData	.builder()//
													.setReportPeriod(ReportPeriod.DAILY)//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, personResourceReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonResourceReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			PersonResourceReportPluginData personResourceReportPluginData = //
					PersonResourceReportPluginData	.builder()//
													.setReportPeriod(ReportPeriod.DAILY)//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, personResourceReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.class, name = "getEmptyBuilder", args = {})
	public void testGetEmptyBuilder() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		PersonResourceReportPluginData filledPersonResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setReportPeopleWithoutResources(true)//
												.setReportZeroPopulations(true)//
												.includeResourceId(TestResourceId.RESOURCE_1).excludeResourceId(TestResourceId.RESOURCE_2)//
												.setDefaultInclusion(false)//
												.build();

		// show that the empty builder is indeed empty

		// the report label is not set
		ContractException contractException = assertThrows(ContractException.class, () -> {
			filledPersonResourceReportPluginData.getEmptyBuilder().build();
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// the report period is not set
		contractException = assertThrows(ContractException.class, () -> {
			filledPersonResourceReportPluginData.getEmptyBuilder()//
												.setReportLabel(new SimpleReportLabel("report label"))//
												.build();
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// After filling the report label and report period we should get the
		// same results as if starting from an empty builder
		reportLabel = new SimpleReportLabel("another label");
		reportPeriod = ReportPeriod.END_OF_SIMULATION;

		PersonResourceReportPluginData personResourceReportPluginData1 = //
				filledPersonResourceReportPluginData.getEmptyBuilder()//
													.setReportLabel(reportLabel)//
													.setReportPeriod(reportPeriod)//
													.build();
		PersonResourceReportPluginData personResourceReportPluginData2 = //
				PersonResourceReportPluginData	.builder()//
												.setReportLabel(reportLabel)//
												.setReportPeriod(reportPeriod)//
												.build();

		assertEquals(personResourceReportPluginData1, personResourceReportPluginData2);
	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a PersonResourceReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];

			PersonResourceReportPluginData.Builder builder = //
					PersonResourceReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel);

			for (int j = 0; j < 10; j++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder.includeResourceId(testResourceId);
				} else {
					builder.excludeResourceId(testResourceId);
				}
			}

			builder.setReportPeopleWithoutResources(randomGenerator.nextBoolean());
			builder.setReportZeroPopulations(randomGenerator.nextBoolean());

			builder.setDefaultInclusion(randomGenerator.nextBoolean()).build();

			PersonResourceReportPluginData personResourceReportPluginData = builder.build();

			// create the clone builder and have it build
			PersonResourceReportPluginData clonePersonResourceReportPluginData = personResourceReportPluginData.getCloneBuilder().build();

			// the result should equal the original if the clone builder was
			// initialized with the correct state
			assertEquals(personResourceReportPluginData, clonePersonResourceReportPluginData);

		}
	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a PersonResourceReportPluginData from the same random
			// inputs
			PersonResourceReportPluginData.Builder builder1 = PersonResourceReportPluginData.builder();
			PersonResourceReportPluginData.Builder builder2 = PersonResourceReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);
			builder2.setReportPeriod(reportPeriod);

			boolean reportPeopleWithoutResources = randomGenerator.nextBoolean();
			builder1.setReportPeopleWithoutResources(reportPeopleWithoutResources);//
			builder2.setReportPeopleWithoutResources(reportPeopleWithoutResources);//

			boolean reportZeroPopulations = randomGenerator.nextBoolean();
			builder1.setReportZeroPopulations(reportZeroPopulations);//
			builder2.setReportZeroPopulations(reportZeroPopulations);

			for (int j = 0; j < 10; j++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder1.includeResourceId(testResourceId);
					builder2.includeResourceId(testResourceId);
				} else {
					builder1.excludeResourceId(testResourceId);
					builder2.excludeResourceId(testResourceId);
				}
			}

			boolean defaultInclusion = randomGenerator.nextBoolean();
			builder1.setDefaultInclusion(defaultInclusion).build();
			builder2.setDefaultInclusion(defaultInclusion).build();

			PersonResourceReportPluginData personResourceReportPluginData1 = builder1.build();
			PersonResourceReportPluginData personResourceReportPluginData2 = builder2.build();

			assertEquals(personResourceReportPluginData1, personResourceReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the default inclusion
			personResourceReportPluginData2 = //
					personResourceReportPluginData1	.getCloneBuilder()//
													.setDefaultInclusion(!defaultInclusion)//
													.build();
			assertNotEquals(personResourceReportPluginData2, personResourceReportPluginData1);

			// change the reportPeopleWithoutResources
			personResourceReportPluginData2 = //
					personResourceReportPluginData1	.getCloneBuilder()//
													.setReportPeopleWithoutResources(!reportPeopleWithoutResources)//
													.build();
			assertNotEquals(personResourceReportPluginData2, personResourceReportPluginData1);

			// change the reportZeroPopulations
			personResourceReportPluginData2 = //
					personResourceReportPluginData1	.getCloneBuilder()//
													.setReportZeroPopulations(!reportZeroPopulations)//
													.build();
			assertNotEquals(personResourceReportPluginData2, personResourceReportPluginData1);

			// change the report period
			int ord = reportPeriod.ordinal() + 1;
			ord = ord % ReportPeriod.values().length;
			reportPeriod = ReportPeriod.values()[ord];
			personResourceReportPluginData2 = //
					personResourceReportPluginData1	.getCloneBuilder()//
													.setReportPeriod(reportPeriod)//
													.build();
			assertNotEquals(personResourceReportPluginData2, personResourceReportPluginData1);

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			personResourceReportPluginData2 = //
					personResourceReportPluginData1	.getCloneBuilder()//
													.setReportLabel(reportLabel)//
													.build();
			assertNotEquals(personResourceReportPluginData2, personResourceReportPluginData1);

			// change an included property id
			if (!personResourceReportPluginData1.getIncludedResourceIds().isEmpty()) {
				ResourceId resourceId = personResourceReportPluginData1.getIncludedResourceIds().iterator().next();
				personResourceReportPluginData2 = //
						personResourceReportPluginData1	.getCloneBuilder()//
														.excludeResourceId(resourceId)//
														.build();
				assertNotEquals(personResourceReportPluginData2, personResourceReportPluginData1);
			}
			// change an excluded property id
			if (!personResourceReportPluginData1.getExcludedResourceIds().isEmpty()) {
				ResourceId resourceId = personResourceReportPluginData1.getExcludedResourceIds().iterator().next();
				personResourceReportPluginData2 = //
						personResourceReportPluginData1	.getCloneBuilder()//
														.includeResourceId(resourceId)//
														.build();
				assertNotEquals(personResourceReportPluginData2, personResourceReportPluginData1);
			}

		}

	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a PersonResourceReportPluginData from the same random
			// inputs
			PersonResourceReportPluginData.Builder builder1 = PersonResourceReportPluginData.builder();
			PersonResourceReportPluginData.Builder builder2 = PersonResourceReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);
			builder2.setReportPeriod(reportPeriod);

			for (int j = 0; j < 10; j++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder1.includeResourceId(testResourceId);
					builder2.includeResourceId(testResourceId);
				} else {
					builder1.excludeResourceId(testResourceId);
					builder2.excludeResourceId(testResourceId);
				}
			}

			boolean defaultInclusion = randomGenerator.nextBoolean();
			builder1.setDefaultInclusion(defaultInclusion).build();
			builder2.setDefaultInclusion(defaultInclusion).build();

			boolean reportPeopleWithoutResources = randomGenerator.nextBoolean();
			builder1.setReportPeopleWithoutResources(reportPeopleWithoutResources);//
			builder2.setReportPeopleWithoutResources(reportPeopleWithoutResources);//

			boolean reportZeroPopulations = randomGenerator.nextBoolean();
			builder1.setReportZeroPopulations(reportZeroPopulations);//
			builder2.setReportZeroPopulations(reportZeroPopulations);

			PersonResourceReportPluginData personResourceReportPluginData1 = builder1.build();
			PersonResourceReportPluginData personResourceReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = personResourceReportPluginData1.hashCode();
			assertEquals(hashCode, personResourceReportPluginData1.hashCode());
			assertEquals(hashCode, personResourceReportPluginData1.hashCode());
			assertEquals(hashCode, personResourceReportPluginData1.hashCode());
			assertEquals(hashCode, personResourceReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(personResourceReportPluginData1.hashCode(), personResourceReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(personResourceReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are
		 * unique values -- this is dependent on the random seed
		 */
		assertEquals(50, observedHashCodes.size());

	}

}
