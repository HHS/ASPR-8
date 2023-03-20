package plugins.groups.reports;

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
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_GroupPopulationReportPluginData {

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		GroupPopulationReportPluginData.Builder builder = GroupPopulationReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report period is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		GroupPopulationReportPluginData	.builder()//
										.setReportLabel(new SimpleReportLabel(getClass()))//
										.build());
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// precondition test: if the report label is not assigned
		contractException = assertThrows(ContractException.class, () -> //
		GroupPopulationReportPluginData	.builder()//
										.setReportPeriod(ReportPeriod.DAILY)//
										.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			GroupPopulationReportPluginData proupPopulationReportPluginData = //
					GroupPopulationReportPluginData	.builder()//
													.setReportPeriod(ReportPeriod.DAILY)//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, proupPopulationReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GroupPopulationReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.Builder.class, name = "setReportPeriod", args = { ReportPeriod.class })
	public void testSetReportPeriod() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			GroupPopulationReportPluginData proupPopulationReportPluginData = //
					GroupPopulationReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel)//
													.build();

			assertEquals(reportPeriod, proupPopulationReportPluginData.getReportPeriod());
		}

		// precondition: if the report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GroupPopulationReportPluginData.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			GroupPopulationReportPluginData proupPopulationReportPluginData = //
					GroupPopulationReportPluginData	.builder()//
													.setReportPeriod(ReportPeriod.DAILY)//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, proupPopulationReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "getReportPeriod", args = {})
	public void testGetReportPeriod() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			GroupPopulationReportPluginData proupPopulationReportPluginData = //
					GroupPopulationReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel)//
													.build();

			assertEquals(reportPeriod, proupPopulationReportPluginData.getReportPeriod());
		}
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "getEmptyBuilder", args = {})
	public void testGetEmptyBuilder() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		GroupPopulationReportPluginData filledGroupPopulationReportPluginData = //
				GroupPopulationReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();

		// show that the empty builder is indeed empty

		// the report label is not set
		ContractException contractException = assertThrows(ContractException.class, () -> {
			filledGroupPopulationReportPluginData.getEmptyBuilder().build();
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// the report period is not set
		contractException = assertThrows(ContractException.class, () -> {
			filledGroupPopulationReportPluginData	.getEmptyBuilder()//
													.setReportLabel(new SimpleReportLabel("report label"))//
													.build();
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// After filling the report label and report period we should get the
		// same results as if starting from an empty builder
		reportLabel = new SimpleReportLabel("another label");
		reportPeriod = ReportPeriod.END_OF_SIMULATION;

		GroupPopulationReportPluginData groupPopulationReportPluginData1 = //
				filledGroupPopulationReportPluginData	.getEmptyBuilder()//
														.setReportLabel(reportLabel)//
														.setReportPeriod(reportPeriod)//
														.build();
		GroupPopulationReportPluginData groupPopulationReportPluginData2 = //
				GroupPopulationReportPluginData	.builder()//
												.setReportLabel(reportLabel)//
												.setReportPeriod(reportPeriod)//
												.build();

		assertEquals(groupPopulationReportPluginData1, groupPopulationReportPluginData2);
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1807247797909883254L);
		for (int i = 0; i < 10; i++) {

			// build a GroupPopulationReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];

			GroupPopulationReportPluginData proupPopulationReportPluginData = //
					GroupPopulationReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel).build();

			// create the clone builder and have it build
			GroupPopulationReportPluginData cloneGroupPopulationReportPluginData = proupPopulationReportPluginData.getCloneBuilder().build();

			// the result should equal the original if the clone builder was
			// initialized with the correct state
			assertEquals(proupPopulationReportPluginData, cloneGroupPopulationReportPluginData);

		}
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a GroupPopulationReportPluginData from the same random
			// inputs
			GroupPopulationReportPluginData.Builder builder1 = GroupPopulationReportPluginData.builder();
			GroupPopulationReportPluginData.Builder builder2 = GroupPopulationReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);
			builder2.setReportPeriod(reportPeriod);

			GroupPopulationReportPluginData proupPopulationReportPluginData1 = builder1.build();
			GroupPopulationReportPluginData proupPopulationReportPluginData2 = builder2.build();

			assertEquals(proupPopulationReportPluginData1, proupPopulationReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the report period
			int ord = reportPeriod.ordinal() + 1;
			ord = ord % ReportPeriod.values().length;
			reportPeriod = ReportPeriod.values()[ord];
			proupPopulationReportPluginData2 = //
					proupPopulationReportPluginData1.getCloneBuilder()//
													.setReportPeriod(reportPeriod)//
													.build();
			assertNotEquals(proupPopulationReportPluginData2, proupPopulationReportPluginData1);

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			proupPopulationReportPluginData2 = //
					proupPopulationReportPluginData1.getCloneBuilder()//
													.setReportLabel(reportLabel)//
													.build();
			assertNotEquals(proupPopulationReportPluginData2, proupPopulationReportPluginData1);

		}
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9178672375367646465L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a GroupPopulationReportPluginData from the same random
			// inputs
			GroupPopulationReportPluginData.Builder builder1 = GroupPopulationReportPluginData.builder();
			GroupPopulationReportPluginData.Builder builder2 = GroupPopulationReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);
			builder2.setReportPeriod(reportPeriod);

			GroupPopulationReportPluginData proupPopulationReportPluginData1 = builder1.build();
			GroupPopulationReportPluginData proupPopulationReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = proupPopulationReportPluginData1.hashCode();
			assertEquals(hashCode, proupPopulationReportPluginData1.hashCode());
			assertEquals(hashCode, proupPopulationReportPluginData1.hashCode());
			assertEquals(hashCode, proupPopulationReportPluginData1.hashCode());
			assertEquals(hashCode, proupPopulationReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(proupPopulationReportPluginData1.hashCode(), proupPopulationReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(proupPopulationReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are
		 * unique values -- this is dependent on the random seed
		 */
		assertTrue(observedHashCodes.size()>45);

	}

}
