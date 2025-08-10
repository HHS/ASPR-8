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
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {

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
			RegionTransferReportPluginData.Builder cloneBuilder = regionTransferReportPluginData.toBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(regionTransferReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// setReportLabel
			cloneBuilder = regionTransferReportPluginData.toBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(regionTransferReportPluginData, cloneBuilder.build());
			
			// setReportPeriod
			cloneBuilder = regionTransferReportPluginData.toBuilder();
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
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

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
	
		// never equal to another type
		for (int i = 0; i < 30; i++) {
			RegionTransferReportPluginData pluginData = getRandomRegionTransferReportPluginData(
					randomGenerator.nextLong());
			assertFalse(pluginData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			RegionTransferReportPluginData pluginData = getRandomRegionTransferReportPluginData(
					randomGenerator.nextLong());
			assertFalse(pluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			RegionTransferReportPluginData pluginData = getRandomRegionTransferReportPluginData(
					randomGenerator.nextLong());
			assertTrue(pluginData.equals(pluginData));
		}

		// symmetric, transitive and consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RegionTransferReportPluginData pluginData1 = getRandomRegionTransferReportPluginData(seed);
			RegionTransferReportPluginData pluginData2 = getRandomRegionTransferReportPluginData(seed);
			assertFalse(pluginData1 == pluginData2);
			for (int j = 0; j < 10; j++) {
				assertTrue(pluginData1.equals(pluginData2));
				assertTrue(pluginData2.equals(pluginData1));
			}
		}

		// Different inputs yield unequal values
		Set<RegionTransferReportPluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RegionTransferReportPluginData pluginData = getRandomRegionTransferReportPluginData(
					randomGenerator.nextLong());
			set.add(pluginData);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = RegionTransferReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			RegionTransferReportPluginData pluginData1 = getRandomRegionTransferReportPluginData(seed);
			RegionTransferReportPluginData pluginData2 = getRandomRegionTransferReportPluginData(seed);

			assertEquals(pluginData1, pluginData2);
			assertEquals(pluginData1.hashCode(), pluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			RegionTransferReportPluginData pluginData = getRandomRegionTransferReportPluginData(
					randomGenerator.nextLong());
			hashCodes.add(pluginData.hashCode());
		}

		assertEquals(100, hashCodes.size());
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

	private RegionTransferReportPluginData getRandomRegionTransferReportPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		RegionTransferReportPluginData.Builder builder = RegionTransferReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
		builder.setReportLabel(reportLabel);

		ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
		builder.setReportPeriod(reportPeriod);

		return builder.build();
	}
}
