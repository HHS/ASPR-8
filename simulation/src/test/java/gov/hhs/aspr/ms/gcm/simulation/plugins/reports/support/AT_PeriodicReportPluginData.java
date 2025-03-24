package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_PeriodicReportPluginData {

	private static class LocalPeriodicReportPluginData extends PeriodicReportPluginData {

		private final Data data;

		private LocalPeriodicReportPluginData(Data data) {
			super(data);
			this.data = data;
		}

		private static class Data extends PeriodicReportPluginData.Data {

			private boolean locked;

			private Data() {
				super();
			}

			private Data(Data data) {
				super(data);
				locked = data.locked;
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append(super.toString());
				builder.append("]");
				return builder.toString();
			}
		}

		public final static class Builder extends PeriodicReportPluginData.Builder {

			private Data data;

			private Builder(Data data) {
				super(data);
				this.data = data;
			}

			@Override
			public LocalPeriodicReportPluginData build() {
				if (!data.locked) {
					validateData();
				}
				ensureImmutability();
				return new LocalPeriodicReportPluginData(data);
			}

			@Override
			public Builder setReportLabel(ReportLabel reportLabel) {
				ensureDataMutability();
				super.setReportLabel(reportLabel);
				return this;
			}

			@Override
			public Builder setReportPeriod(ReportPeriod reportPeriod) {
				ensureDataMutability();
				super.setReportPeriod(reportPeriod);
				return this;
			}

			private void ensureDataMutability() {
				if (data.locked) {
					data = new Data(data);
					data.locked = false;
				}
			}

			private void ensureImmutability() {
				if (!data.locked) {
					data.locked = true;
				}
			}

			private void validateData() {
				if (data.reportLabel == null) {
					throw new ContractException(ReportError.NULL_REPORT_LABEL);
				}
				if (data.reportPeriod == null) {
					throw new ContractException(ReportError.NULL_REPORT_PERIOD);
				}
			}

		}

		public static Builder builder() {
			return new Builder(new Data());
		}

		@Override
		public Builder toBuilder() {
			return new Builder(data);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("GroupPopulationReportPluginData [data=");
			builder.append(data);
			builder.append("]");
			return builder.toString();
		}
	}

	@Test
	@UnitTestMethod(target = PeriodicReportPluginData.Builder.class, name = "setReportLabel", args = {
			ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 10; i++) {
			SimpleReportLabel simpleReportLabel = new SimpleReportLabel("report label " + i);

			LocalPeriodicReportPluginData localPeriodicReportPluginData = //
					LocalPeriodicReportPluginData.builder()//
							.setReportLabel(simpleReportLabel)//
							.setReportPeriod(ReportPeriod.DAILY)//
							.build();
			assertEquals(simpleReportLabel, localPeriodicReportPluginData.getReportLabel());
		}
	}

	@Test
	@UnitTestMethod(target = PeriodicReportPluginData.Builder.class, name = "setReportPeriod", args = {
			ReportPeriod.class })
	public void testSetReportPeriod() {
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {
			SimpleReportLabel simpleReportLabel = new SimpleReportLabel("report label");

			LocalPeriodicReportPluginData localPeriodicReportPluginData = //
					LocalPeriodicReportPluginData.builder()//
							.setReportLabel(simpleReportLabel)//
							.setReportPeriod(reportPeriod)//
							.build();
			assertEquals(reportPeriod, localPeriodicReportPluginData.getReportPeriod());
		}
	}

	@Test
	@UnitTestMethod(target = PeriodicReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2142811459970946523L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			LocalPeriodicReportPluginData pluginData1 = getRandomLocalPeriodicReportPluginData(seed);
			LocalPeriodicReportPluginData pluginData2 = getRandomLocalPeriodicReportPluginData(seed);

			assertEquals(pluginData1, pluginData2);
			assertEquals(pluginData1.hashCode(), pluginData2.hashCode());

		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			LocalPeriodicReportPluginData pluginData = getRandomLocalPeriodicReportPluginData(
					randomGenerator.nextLong());
			hashCodes.add(pluginData.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = PeriodicReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8984499250224306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			LocalPeriodicReportPluginData pluginData = getRandomLocalPeriodicReportPluginData(
					randomGenerator.nextLong());
			assertFalse(pluginData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			LocalPeriodicReportPluginData pluginData = getRandomLocalPeriodicReportPluginData(
					randomGenerator.nextLong());
			assertFalse(pluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			LocalPeriodicReportPluginData pluginData = getRandomLocalPeriodicReportPluginData(
					randomGenerator.nextLong());
			assertTrue(pluginData.equals(pluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			LocalPeriodicReportPluginData pluginData1 = getRandomLocalPeriodicReportPluginData(seed);
			LocalPeriodicReportPluginData pluginData2 = getRandomLocalPeriodicReportPluginData(seed);
			assertFalse(pluginData1 == pluginData2);
			for (int j = 0; j < 10; j++) {
				assertTrue(pluginData1.equals(pluginData2));
				assertTrue(pluginData2.equals(pluginData1));
			}
		}

		// different inputs yield unequal plugin datas
		Set<LocalPeriodicReportPluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			LocalPeriodicReportPluginData pluginData = getRandomLocalPeriodicReportPluginData(
					randomGenerator.nextLong());
			set.add(pluginData);
		}
		assertEquals(100, set.size());
	}

	private LocalPeriodicReportPluginData getRandomLocalPeriodicReportPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		LocalPeriodicReportPluginData.Builder builder = LocalPeriodicReportPluginData.builder();

		builder.setReportLabel(new SimpleReportLabel(randomGenerator.nextInt()));

		ReportPeriod[] reportPeriodValues = ReportPeriod.values();
		int randomIndex = randomGenerator.nextInt(reportPeriodValues.length);
		ReportPeriod randomReportPeriod = reportPeriodValues[randomIndex];
		builder.setReportPeriod(randomReportPeriod);

		return builder.build();
	}
}
