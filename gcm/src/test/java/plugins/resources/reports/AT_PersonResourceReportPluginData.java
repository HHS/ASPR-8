package plugins.resources.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_PersonResourceReportPluginData {

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
	@UnitTestMethod(target = PersonResourceReportPluginData.Builder.class, name = "setReportPeriod", args = { ReportPeriod.class })
	public void testSetReportPeriod() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			PersonResourceReportPluginData personResourceReportPluginData = //
					PersonResourceReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel)//
													.build();

			assertEquals(reportPeriod, personResourceReportPluginData.getReportPeriod());
		}

		// precondition: if the report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonResourceReportPluginData.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.Builder.class, name = "setDefaultInclusion", args = { boolean.class })
	public void testSetDefaultInclusion() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		PersonResourceReportPluginData personResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertEquals(true, personResourceReportPluginData.getDefaultInclusionPolicy());

		personResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(true)//
												.build();
		assertEquals(true, personResourceReportPluginData.getDefaultInclusionPolicy());

		personResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(false)//
												.build();
		assertEquals(false, personResourceReportPluginData.getDefaultInclusionPolicy());

	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.Builder.class, name = "includeResource", args = { ResourceId.class })
	public void testIncludeResource() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		PersonResourceReportPluginData personResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(personResourceReportPluginData.getIncludedResourceIds().isEmpty());

		// show that inclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		PersonResourceReportPluginData.Builder builder = PersonResourceReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
		}

		personResourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, personResourceReportPluginData.getIncludedResourceIds());

		// show that inclusion will override exclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = PersonResourceReportPluginData.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
			builder.includeResource(resourceId);
		}

		personResourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, personResourceReportPluginData.getIncludedResourceIds());

		// precondition: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonResourceReportPluginData.builder().includeResource(null);
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.Builder.class, name = "excludeResource", args = { ResourceId.class })
	public void testExcludeResource() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-exclusion
		PersonResourceReportPluginData personResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(personResourceReportPluginData.getExcludedResourceIds().isEmpty());

		// show that exclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		PersonResourceReportPluginData.Builder builder = PersonResourceReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
		}

		personResourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, personResourceReportPluginData.getExcludedResourceIds());

		// show that exclusion will override inclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = PersonResourceReportPluginData.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
			builder.excludeResource(resourceId);
		}

		personResourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, personResourceReportPluginData.getExcludedResourceIds());

		// precondition: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonResourceReportPluginData.builder().excludeResource(null);
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
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
	@UnitTestMethod(target = PersonResourceReportPluginData.class, name = "getReportPeriod", args = {})
	public void testGetReportPeriod() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			PersonResourceReportPluginData personResourceReportPluginData = //
					PersonResourceReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel)//
													.build();

			assertEquals(reportPeriod, personResourceReportPluginData.getReportPeriod());
		}
	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.class, name = "getIncludedResourceIds", args = {})
	public void testGetIncludedResourceIds() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-inclusion
		PersonResourceReportPluginData personResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(personResourceReportPluginData.getIncludedResourceIds().isEmpty());

		// show that inclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		PersonResourceReportPluginData.Builder builder = PersonResourceReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
		}

		personResourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, personResourceReportPluginData.getIncludedResourceIds());

		// show that inclusion will override exclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = PersonResourceReportPluginData.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
			builder.includeResource(resourceId);
		}

		personResourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, personResourceReportPluginData.getIncludedResourceIds());

	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.class, name = "getExcludedResourceIds", args = {})
	public void testGetExcludedResourceIds() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-exclusion
		PersonResourceReportPluginData personResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(personResourceReportPluginData.getExcludedResourceIds().isEmpty());

		// show that exclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		PersonResourceReportPluginData.Builder builder = PersonResourceReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
		}

		personResourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, personResourceReportPluginData.getExcludedResourceIds());

		// show that exclusion will override inclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = PersonResourceReportPluginData.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
			builder.excludeResource(resourceId);
		}

		personResourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, personResourceReportPluginData.getExcludedResourceIds());

	}

	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.class, name = "getDefaultInclusionPolicy", args = {})
	public void testGetDefaultInclusionPolicy() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		PersonResourceReportPluginData personResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertEquals(true, personResourceReportPluginData.getDefaultInclusionPolicy());

		personResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(true)//
												.build();
		assertEquals(true, personResourceReportPluginData.getDefaultInclusionPolicy());

		personResourceReportPluginData = //
				PersonResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(false)//
												.build();
		assertEquals(false, personResourceReportPluginData.getDefaultInclusionPolicy());
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
					builder.includeResource(testResourceId);
				} else {
					builder.excludeResource(testResourceId);
				}
			}

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

			for (int j = 0; j < 10; j++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder1.includeResource(testResourceId);
					builder2.includeResource(testResourceId);
				} else {
					builder1.excludeResource(testResourceId);
					builder2.excludeResource(testResourceId);
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
														.excludeResource(resourceId)//
														.build();
				assertNotEquals(personResourceReportPluginData2, personResourceReportPluginData1);
			}
			// change an excluded property id
			if (!personResourceReportPluginData1.getExcludedResourceIds().isEmpty()) {
				ResourceId resourceId = personResourceReportPluginData1.getExcludedResourceIds().iterator().next();
				personResourceReportPluginData2 = //
						personResourceReportPluginData1	.getCloneBuilder()//
														.includeResource(resourceId)//
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
					builder1.includeResource(testResourceId);
					builder2.includeResource(testResourceId);
				} else {
					builder1.excludeResource(testResourceId);
					builder2.excludeResource(testResourceId);
				}
			}

			boolean defaultInclusion = randomGenerator.nextBoolean();
			builder1.setDefaultInclusion(defaultInclusion).build();
			builder2.setDefaultInclusion(defaultInclusion).build();

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
	
	@Test
	@UnitTestMethod(target = PersonResourceReportPluginData.Builder.class, name = "toString", args = {})
	public void testToString() {
		fail();
	}

}
