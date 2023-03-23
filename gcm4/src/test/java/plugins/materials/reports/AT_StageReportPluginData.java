package plugins.materials.reports;

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
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_StageReportPluginData {

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		StageReportPluginData.Builder builder = StageReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report label is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		StageReportPluginData	.builder()//
								.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			StageReportPluginData stageReportPluginData = //
					StageReportPluginData	.builder()//
											.setReportLabel(expectedReportLabel)//
											.build();

			assertEquals(expectedReportLabel, stageReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			StageReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			StageReportPluginData stageReportPluginData = //
					StageReportPluginData	.builder()//
											.setReportLabel(expectedReportLabel)//
											.build();

			assertEquals(expectedReportLabel, stageReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "getEmptyBuilder", args = {})
	public void testGetEmptyBuilder() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		StageReportPluginData filledStageReportPluginData = //
				StageReportPluginData	.builder()//
										.setReportLabel(reportLabel)//
										.build();

		// show that the empty builder is indeed empty

		// the report label is not set
		ContractException contractException = assertThrows(ContractException.class, () -> {
			filledStageReportPluginData.getEmptyBuilder().build();
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// After filling the report label and report period we should get the
		// same results as if starting from an empty builder
		reportLabel = new SimpleReportLabel("another label");

		StageReportPluginData stageReportPluginData1 = //
				filledStageReportPluginData	.getEmptyBuilder()//
											.setReportLabel(reportLabel)//
											.build();
		StageReportPluginData stageReportPluginData2 = //
				StageReportPluginData	.builder()//
										.setReportLabel(reportLabel)//
										.build();

		assertEquals(stageReportPluginData1, stageReportPluginData2);
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a StageReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

			StageReportPluginData.Builder builder = //
					StageReportPluginData	.builder()//
											.setReportLabel(reportLabel);

			StageReportPluginData stageReportPluginData = builder.build();

			// create the clone builder and have it build
			StageReportPluginData cloneStageReportPluginData = stageReportPluginData.getCloneBuilder().build();

			// the result should equal the original if the clone builder was
			// initialized with the correct state
			assertEquals(stageReportPluginData, cloneStageReportPluginData);

		}
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a StageReportPluginData from the same random
			// inputs
			StageReportPluginData.Builder builder1 = StageReportPluginData.builder();
			StageReportPluginData.Builder builder2 = StageReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			StageReportPluginData stageReportPluginData1 = builder1.build();
			StageReportPluginData stageReportPluginData2 = builder2.build();

			assertEquals(stageReportPluginData1, stageReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			stageReportPluginData2 = //
					stageReportPluginData1	.getCloneBuilder()//
											.setReportLabel(reportLabel)//
											.build();
			assertNotEquals(stageReportPluginData2, stageReportPluginData1);
		}

	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a StageReportPluginData from the same random
			// inputs
			StageReportPluginData.Builder builder1 = StageReportPluginData.builder();
			StageReportPluginData.Builder builder2 = StageReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			StageReportPluginData stageReportPluginData1 = builder1.build();
			StageReportPluginData stageReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = stageReportPluginData1.hashCode();
			assertEquals(hashCode, stageReportPluginData1.hashCode());
			assertEquals(hashCode, stageReportPluginData1.hashCode());
			assertEquals(hashCode, stageReportPluginData1.hashCode());
			assertEquals(hashCode, stageReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(stageReportPluginData1.hashCode(), stageReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(stageReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are
		 * unique values -- this is dependent on the random seed
		 */
		assertTrue(observedHashCodes.size() > 40);

	}

}
