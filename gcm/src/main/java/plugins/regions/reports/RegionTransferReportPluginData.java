package plugins.regions.reports;

import net.jcip.annotations.ThreadSafe;
import plugins.reports.support.PeriodicReportPluginData;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import util.errors.ContractException;

/**
 * A PluginData class supporting PersonPropertyReport construction.
 */
@ThreadSafe
public final class RegionTransferReportPluginData extends PeriodicReportPluginData {

	private final Data data;

	private RegionTransferReportPluginData(Data data) {
		super(data);
		this.data = data;
	}

	/*
	 * Data class for collecting the inputs to the report
	 */
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

	/**
	 * Builder class for the report
	 * 
	 *
	 */
	public final static class Builder extends PeriodicReportPluginData.Builder {
		private Data data;

		private Builder(Data data) {
			super(data);
			this.data = data;
		}

		/**
		 * Returns a PersonPropertyReportPluginData created from the collected
		 * inputs
		 * 
		 * @throws ContractException
		 *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
		 *                           the
		 *                           report label is not assigned</li>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if
		 *                           the
		 *                           report period is not assigned</li>
		 */
		public RegionTransferReportPluginData build() {
			return new RegionTransferReportPluginData(data);
		}

		/**
		 * Sets the report label
		 * 
		 * @throws ContractException
		 *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
		 *                           the
		 *                           report label is null</li>
		 */
		public Builder setReportLabel(ReportLabel reportLabel) {
			super.setReportLabel(reportLabel);
			return this;
		}

		/**
		 * Sets the report period id
		 * 
		 * @throws ContractException
		 *                           <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if
		 *                           the
		 *                           report period is null</li>
		 */
		public Builder setReportPeriod(ReportPeriod reportPeriod) {
			super.setReportPeriod(reportPeriod);
			return this;
		}
	}

	/**
	 * Returns a new instance of the builder class
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	@Override
	public Builder getCloneBuilder() {
		return new Builder(new Data(data));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RegionTransferReportPluginData)) {
			return false;
		}
		RegionTransferReportPluginData other = (RegionTransferReportPluginData) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("RegionTransferReportPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

}