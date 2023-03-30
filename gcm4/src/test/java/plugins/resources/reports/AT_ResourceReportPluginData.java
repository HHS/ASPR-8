package plugins.resources.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

public class AT_ResourceReportPluginData {

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report period is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		ResourceReportPluginData	.builder()//
										.setReportLabel(new SimpleReportLabel(getClass()))//
										.build());
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// precondition test: if the report label is not assigned
		contractException = assertThrows(ContractException.class, () -> //
		ResourceReportPluginData	.builder()//
										.setReportPeriod(ReportPeriod.DAILY)//
										.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			ResourceReportPluginData resourceReportPluginData = //
					ResourceReportPluginData	.builder()//
													.setReportPeriod(ReportPeriod.DAILY)//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, resourceReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourceReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "setReportPeriod", args = { ReportPeriod.class })
	public void testSetReportPeriod() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			ResourceReportPluginData resourceReportPluginData = //
					ResourceReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel)//
													.build();

			assertEquals(reportPeriod, resourceReportPluginData.getReportPeriod());
		}

		// precondition: if the report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourceReportPluginData.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "setDefaultInclusion", args = { boolean.class })
	public void testSetDefaultInclusion() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertEquals(true, resourceReportPluginData.getDefaultInclusionPolicy());

		resourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(true)//
												.build();
		assertEquals(true, resourceReportPluginData.getDefaultInclusionPolicy());

		resourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(false)//
												.build();
		assertEquals(false, resourceReportPluginData.getDefaultInclusionPolicy());

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "includeResourceId", args = { ResourceId.class })
	public void testIncludeResourceId() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(resourceReportPluginData.getIncludedResourceIds().isEmpty());

		// show that inclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		ResourceReportPluginData.Builder builder = ResourceReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getIncludedResourceIds());

		// show that inclusion will override exclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = ResourceReportPluginData.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
			builder.includeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getIncludedResourceIds());

		// precondition: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourceReportPluginData.builder().includeResource(null);
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "excludeResourceId", args = { ResourceId.class })
	public void testExcludeResourceId() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-exclusion
		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(resourceReportPluginData.getExcludedResourceIds().isEmpty());

		// show that exclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		ResourceReportPluginData.Builder builder = ResourceReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getExcludedResourceIds());

		// show that exclusion will override inclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = ResourceReportPluginData.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
			builder.excludeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getExcludedResourceIds());

		// precondition: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourceReportPluginData.builder().excludeResource(null);
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			ResourceReportPluginData resourceReportPluginData = //
					ResourceReportPluginData	.builder()//
													.setReportPeriod(ReportPeriod.DAILY)//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, resourceReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getReportPeriod", args = {})
	public void testGetReportPeriod() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			ResourceReportPluginData resourceReportPluginData = //
					ResourceReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel)//
													.build();

			assertEquals(reportPeriod, resourceReportPluginData.getReportPeriod());
		}
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getIncludedResourceIds", args = {})
	public void testGetIncludedResourceIds() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-inclusion
		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(resourceReportPluginData.getIncludedResourceIds().isEmpty());

		// show that inclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		ResourceReportPluginData.Builder builder = ResourceReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getIncludedResourceIds());

		// show that inclusion will override exclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = ResourceReportPluginData.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
			builder.includeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getIncludedResourceIds());

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getExcludedResourceIds", args = {})
	public void testGetExcludedResourceIds() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-exclusion
		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(resourceReportPluginData.getExcludedResourceIds().isEmpty());

		// show that exclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		ResourceReportPluginData.Builder builder = ResourceReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getExcludedResourceIds());

		// show that exclusion will override inclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = ResourceReportPluginData.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
			builder.excludeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getExcludedResourceIds());

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getDefaultInclusionPolicy", args = {})
	public void testGetDefaultInclusionPolicy() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertEquals(true, resourceReportPluginData.getDefaultInclusionPolicy());

		resourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(true)//
												.build();
		assertEquals(true, resourceReportPluginData.getDefaultInclusionPolicy());

		resourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(false)//
												.build();
		assertEquals(false, resourceReportPluginData.getDefaultInclusionPolicy());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getEmptyBuilder", args = {})
	public void testGetEmptyBuilder() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		ResourceReportPluginData filledResourceReportPluginData = //
				ResourceReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.includeResource(TestResourceId.RESOURCE_1).excludeResource(TestResourceId.RESOURCE_2)//
												.setDefaultInclusion(false)//
												.build();

		// show that the empty builder is indeed empty

		// the report label is not set
		ContractException contractException = assertThrows(ContractException.class, () -> {
			filledResourceReportPluginData.getEmptyBuilder().build();
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// the report period is not set
		contractException = assertThrows(ContractException.class, () -> {
			filledResourceReportPluginData.getEmptyBuilder()//
												.setReportLabel(new SimpleReportLabel("report label"))//
												.build();
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// After filling the report label and report period we should get the
		// same results as if starting from an empty builder
		reportLabel = new SimpleReportLabel("another label");
		reportPeriod = ReportPeriod.END_OF_SIMULATION;

		ResourceReportPluginData resourceReportPluginData1 = //
				filledResourceReportPluginData.getEmptyBuilder()//
													.setReportLabel(reportLabel)//
													.setReportPeriod(reportPeriod)//
													.build();
		ResourceReportPluginData resourceReportPluginData2 = //
				ResourceReportPluginData	.builder()//
												.setReportLabel(reportLabel)//
												.setReportPeriod(reportPeriod)//
												.build();

		assertEquals(resourceReportPluginData1, resourceReportPluginData2);
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a ResourceReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];

			ResourceReportPluginData.Builder builder = //
					ResourceReportPluginData	.builder()//
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

			ResourceReportPluginData resourceReportPluginData = builder.build();

			// create the clone builder and have it build
			ResourceReportPluginData cloneResourceReportPluginData = resourceReportPluginData.getCloneBuilder().build();

			// the result should equal the original if the clone builder was
			// initialized with the correct state
			assertEquals(resourceReportPluginData, cloneResourceReportPluginData);

		}
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a ResourceReportPluginData from the same random
			// inputs
			ResourceReportPluginData.Builder builder1 = ResourceReportPluginData.builder();
			ResourceReportPluginData.Builder builder2 = ResourceReportPluginData.builder();

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

			ResourceReportPluginData resourceReportPluginData1 = builder1.build();
			ResourceReportPluginData resourceReportPluginData2 = builder2.build();

			assertEquals(resourceReportPluginData1, resourceReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the default inclusion
			resourceReportPluginData2 = //
					resourceReportPluginData1	.getCloneBuilder()//
													.setDefaultInclusion(!defaultInclusion)//
													.build();
			assertNotEquals(resourceReportPluginData2, resourceReportPluginData1);

			// change the report period
			int ord = reportPeriod.ordinal() + 1;
			ord = ord % ReportPeriod.values().length;
			reportPeriod = ReportPeriod.values()[ord];
			resourceReportPluginData2 = //
					resourceReportPluginData1	.getCloneBuilder()//
													.setReportPeriod(reportPeriod)//
													.build();
			assertNotEquals(resourceReportPluginData2, resourceReportPluginData1);

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			resourceReportPluginData2 = //
					resourceReportPluginData1	.getCloneBuilder()//
													.setReportLabel(reportLabel)//
													.build();
			assertNotEquals(resourceReportPluginData2, resourceReportPluginData1);

			// change an included property id
			if (!resourceReportPluginData1.getIncludedResourceIds().isEmpty()) {
				ResourceId resourceId = resourceReportPluginData1.getIncludedResourceIds().iterator().next();
				resourceReportPluginData2 = //
						resourceReportPluginData1	.getCloneBuilder()//
														.excludeResource(resourceId)//
														.build();
				assertNotEquals(resourceReportPluginData2, resourceReportPluginData1);
			}
			// change an excluded property id
			if (!resourceReportPluginData1.getExcludedResourceIds().isEmpty()) {
				ResourceId resourceId = resourceReportPluginData1.getExcludedResourceIds().iterator().next();
				resourceReportPluginData2 = //
						resourceReportPluginData1	.getCloneBuilder()//
														.includeResource(resourceId)//
														.build();
				assertNotEquals(resourceReportPluginData2, resourceReportPluginData1);
			}

		}

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a ResourceReportPluginData from the same random
			// inputs
			ResourceReportPluginData.Builder builder1 = ResourceReportPluginData.builder();
			ResourceReportPluginData.Builder builder2 = ResourceReportPluginData.builder();

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

			ResourceReportPluginData resourceReportPluginData1 = builder1.build();
			ResourceReportPluginData resourceReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = resourceReportPluginData1.hashCode();
			assertEquals(hashCode, resourceReportPluginData1.hashCode());
			assertEquals(hashCode, resourceReportPluginData1.hashCode());
			assertEquals(hashCode, resourceReportPluginData1.hashCode());
			assertEquals(hashCode, resourceReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(resourceReportPluginData1.hashCode(), resourceReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(resourceReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are
		 * unique values -- this is dependent on the random seed
		 */
		assertEquals(50, observedHashCodes.size());

	}

}
