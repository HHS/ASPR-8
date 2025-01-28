package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

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

}
