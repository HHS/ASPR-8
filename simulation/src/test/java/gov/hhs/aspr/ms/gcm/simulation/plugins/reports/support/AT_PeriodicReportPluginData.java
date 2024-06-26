package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_PeriodicReportPluginData {

	private static class LocalPeriodicReportPluginData extends PeriodicReportPluginData {

		private final Data data;

		private LocalPeriodicReportPluginData(Data data) {
			super(data);
			this.data = data;
		}

		private static class Data extends PeriodicReportPluginData.Data {

			private Data() {
				super();
			}

			private Data(Data data) {
				super(data);
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
				return new LocalPeriodicReportPluginData(data);
			}

			@Override
			public Builder setReportLabel(ReportLabel reportLabel) {
				super.setReportLabel(reportLabel);
				return this;
			}

			@Override
			public Builder setReportPeriod(ReportPeriod reportPeriod) {
				super.setReportPeriod(reportPeriod);
				return this;
			}
		}

		public static Builder builder() {
			return new Builder(new Data());
		}

		@Override
		public Builder getCloneBuilder() {
			return new Builder(new Data(data));
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
