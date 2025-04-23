package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.reports;

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
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1807247797909883254L);
		for (int i = 0; i < 10; i++) {

			// build a GroupPopulationReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];

			GroupPopulationReportPluginData groupPopulationReportPluginData = //
					GroupPopulationReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel)//
													.build();

			// show that the returned clone builder will build an identical instance if no
			// mutations are made
			GroupPopulationReportPluginData.Builder cloneBuilder = groupPopulationReportPluginData.toBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(groupPopulationReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// setReportLabel
			cloneBuilder = groupPopulationReportPluginData.toBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(groupPopulationReportPluginData, cloneBuilder.build());

			// setReportPeriod
			cloneBuilder = groupPopulationReportPluginData.toBuilder();			
			int index = (reportPeriod.ordinal()+1)%ReportPeriod.values().length;
			reportPeriod = ReportPeriod.values()[index];
			cloneBuilder.setReportPeriod(reportPeriod);
			assertNotEquals(groupPopulationReportPluginData, cloneBuilder.build());
			

		}
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		ReportLabel reportLabel = new SimpleReportLabel(0);

		GroupPopulationReportPluginData pluginData = GroupPopulationReportPluginData.builder()
						.setReportLabel(reportLabel)
						.setReportPeriod(ReportPeriod.DAILY)
						.build();

		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(GroupPopulationReportPluginData.checkVersionSupported(version));
			assertFalse(GroupPopulationReportPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(GroupPopulationReportPluginData.checkVersionSupported("badVersion"));
			assertFalse(GroupPopulationReportPluginData.checkVersionSupported(version + "0"));
			assertFalse(GroupPopulationReportPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			GroupPopulationReportPluginData pluginData = getRandomGroupPopulationReportPluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			GroupPopulationReportPluginData pluginData = getRandomGroupPopulationReportPluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			GroupPopulationReportPluginData pluginData = getRandomGroupPopulationReportPluginData(randomGenerator.nextLong());
			assertTrue(pluginData.equals(pluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupPopulationReportPluginData pluginData1 = getRandomGroupPopulationReportPluginData(seed);
			GroupPopulationReportPluginData pluginData2 = getRandomGroupPopulationReportPluginData(seed);
			assertFalse(pluginData1 == pluginData2);
			for (int j = 0; j < 10; j++) {
				assertTrue(pluginData1.equals(pluginData2));
				assertTrue(pluginData2.equals(pluginData1));
			}
		}

		// different inputs yield unequal plugin datas
		Set<GroupPopulationReportPluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupPopulationReportPluginData pluginData = getRandomGroupPopulationReportPluginData(randomGenerator.nextLong());
			set.add(pluginData);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9178672375367646465L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GroupPopulationReportPluginData pluginData1 = getRandomGroupPopulationReportPluginData(seed);
			GroupPopulationReportPluginData pluginData2 = getRandomGroupPopulationReportPluginData(seed);

			assertEquals(pluginData1, pluginData2);
			assertEquals(pluginData1.hashCode(), pluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GroupPopulationReportPluginData pluginData = getRandomGroupPopulationReportPluginData(randomGenerator.nextLong());
			hashCodes.add(pluginData.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

    @Test
    @UnitTestMethod(target = GroupPopulationReportPluginData.class, name = "toString", args = {})
    public void testToString() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2394011517139293620L);
        for (int i = 0; i < 10; i++) {
            GroupPopulationReportPluginData.Builder builder = GroupPopulationReportPluginData.builder();

            ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
            builder.setReportLabel(reportLabel);

            ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
            builder.setReportPeriod(reportPeriod);

            GroupPopulationReportPluginData groupPopulationReportPluginData = builder.build();

            StringBuilder sb = new StringBuilder();
            sb.append("GroupPopulationReportPluginData [data=");

            StringBuilder superDataBuilder = new StringBuilder();
            superDataBuilder.append("Data [reportLabel=");
            superDataBuilder.append(reportLabel);
            superDataBuilder.append(", reportPeriod=");
            superDataBuilder.append(reportPeriod);

            StringBuilder dataBuilder = new StringBuilder();
            dataBuilder.append(superDataBuilder.toString());
            dataBuilder.append("]");

            sb.append(dataBuilder.toString());
            sb.append("]");

            assertEquals(sb.toString(), groupPopulationReportPluginData.toString());
        }
    }

	private GroupPopulationReportPluginData getRandomGroupPopulationReportPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		GroupPopulationReportPluginData.Builder builder = GroupPopulationReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
		builder.setReportLabel(reportLabel);

		ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
        builder.setReportPeriod(reportPeriod);

		return builder.build();
	}
}
