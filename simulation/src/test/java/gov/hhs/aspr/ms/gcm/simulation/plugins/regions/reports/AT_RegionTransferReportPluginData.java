package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.reports;

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
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_RegionTransferReportPluginData {

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		RegionTransferReportPluginData.Builder builder = RegionTransferReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report period is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		RegionTransferReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel(getClass()))//
				.build());
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// precondition test: if the report label is not assigned
		contractException = assertThrows(ContractException.class, () -> //
		RegionTransferReportPluginData.builder()//
				.setReportPeriod(ReportPeriod.DAILY)//
				.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.Builder.class, name = "setReportLabel", args = {
			ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			RegionTransferReportPluginData regionTransferReportPluginData = //
					RegionTransferReportPluginData.builder()//
							.setReportPeriod(ReportPeriod.DAILY)//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, regionTransferReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			RegionTransferReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.Builder.class, name = "setReportPeriod", args = {
			ReportPeriod.class })
	public void testSetReportPeriod() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			RegionTransferReportPluginData regionTransferReportPluginData = //
					RegionTransferReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel)//
							.build();

			assertEquals(reportPeriod, regionTransferReportPluginData.getReportPeriod());
		}

		// precondition: if the report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			RegionTransferReportPluginData.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			RegionTransferReportPluginData regionTransferReportPluginData = //
					RegionTransferReportPluginData.builder()//
							.setReportPeriod(ReportPeriod.DAILY)//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, regionTransferReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "getReportPeriod", args = {})
	public void testGetReportPeriod() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			RegionTransferReportPluginData regionTransferReportPluginData = //
					RegionTransferReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel)//
							.build();

			assertEquals(reportPeriod, regionTransferReportPluginData.getReportPeriod());
		}
	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {

			// build a RegionTransferReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];

			RegionTransferReportPluginData.Builder builder = //
					RegionTransferReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel);

			RegionTransferReportPluginData regionTransferReportPluginData = builder.build();

			// show that the returned clone builder will build an identical instance if no
			// mutations are made
			RegionTransferReportPluginData.Builder cloneBuilder = regionTransferReportPluginData.getCloneBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(regionTransferReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// setReportLabel
			cloneBuilder = regionTransferReportPluginData.getCloneBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(regionTransferReportPluginData, cloneBuilder.build());
			
			// setReportPeriod
			cloneBuilder = regionTransferReportPluginData.getCloneBuilder();
			cloneBuilder.setReportPeriod(regionTransferReportPluginData.getReportPeriod().next());
			assertNotEquals(regionTransferReportPluginData, cloneBuilder.build());

		}
	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		RegionTransferReportPluginData pluginData = RegionTransferReportPluginData.builder()
				.setReportLabel(new SimpleReportLabel(0))
				.setReportPeriod(ReportPeriod.DAILY)
				.build();

		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "checkVersionSupported", args = {
			String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList("", "4.0.0", "4.1.0", StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(RegionTransferReportPluginData.checkVersionSupported(version));
			assertFalse(RegionTransferReportPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(RegionTransferReportPluginData.checkVersionSupported("badVersion"));
			assertFalse(RegionTransferReportPluginData.checkVersionSupported(version + "0"));
			assertFalse(RegionTransferReportPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a RegionTransferReportPluginData from the same random
			// inputs
			RegionTransferReportPluginData.Builder builder1 = RegionTransferReportPluginData.builder();
			RegionTransferReportPluginData.Builder builder2 = RegionTransferReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);
			builder2.setReportPeriod(reportPeriod);

			RegionTransferReportPluginData regionTransferReportPluginData1 = builder1.build();
			RegionTransferReportPluginData regionTransferReportPluginData2 = builder2.build();

			assertEquals(regionTransferReportPluginData1, regionTransferReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the report period
			int ord = reportPeriod.ordinal() + 1;
			ord = ord % ReportPeriod.values().length;
			reportPeriod = ReportPeriod.values()[ord];
			regionTransferReportPluginData2 = //
					regionTransferReportPluginData1.getCloneBuilder()//
							.setReportPeriod(reportPeriod)//
							.build();
			assertNotEquals(regionTransferReportPluginData2, regionTransferReportPluginData1);

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			regionTransferReportPluginData2 = //
					regionTransferReportPluginData1.getCloneBuilder()//
							.setReportLabel(reportLabel)//
							.build();
			assertNotEquals(regionTransferReportPluginData2, regionTransferReportPluginData1);

		}

	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a RegionTransferReportPluginData from the same random
			// inputs
			RegionTransferReportPluginData.Builder builder1 = RegionTransferReportPluginData.builder();
			RegionTransferReportPluginData.Builder builder2 = RegionTransferReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);
			builder2.setReportPeriod(reportPeriod);

			RegionTransferReportPluginData regionTransferReportPluginData1 = builder1.build();
			RegionTransferReportPluginData regionTransferReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = regionTransferReportPluginData1.hashCode();
			assertEquals(hashCode, regionTransferReportPluginData1.hashCode());
			assertEquals(hashCode, regionTransferReportPluginData1.hashCode());
			assertEquals(hashCode, regionTransferReportPluginData1.hashCode());
			assertEquals(hashCode, regionTransferReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(regionTransferReportPluginData1.hashCode(), regionTransferReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(regionTransferReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are unique
		 * values -- this is dependent on the random seed
		 */
		assertTrue(observedHashCodes.size() > 45);

	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "toString", args = {})
	public void testToString() {
		RegionTransferReportPluginData regionTransferReportPluginData = RegionTransferReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel("RegionTransferReport"))//
				.setReportPeriod(ReportPeriod.DAILY)//
				.build();

		String actualValue = regionTransferReportPluginData.toString();

		String expectedValue = "RegionTransferReportPluginData [data=Data ["
				+ "reportLabel=SimpleReportLabel [value=RegionTransferReport], "
				+ "reportPeriod=DAILY]]";
		assertEquals(expectedValue, actualValue);

	}

}
