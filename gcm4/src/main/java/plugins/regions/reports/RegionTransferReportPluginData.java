package plugins.regions.reports;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import util.errors.ContractException;

/**
 * A PluginData class supporting PersonPropertyReport construction.
 */
@ThreadSafe
public final class RegionTransferReportPluginData implements PluginData {

	/*
	 * Data class for collecting the inputs to the report
	 */
	private static class Data {
		private ReportLabel reportLabel;
		private ReportPeriod reportPeriod;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			reportLabel = data.reportLabel;
			reportPeriod = data.reportPeriod;
			
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((reportLabel == null) ? 0 : reportLabel.hashCode());
			result = prime * result + ((reportPeriod == null) ? 0 : reportPeriod.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;
			
			if (reportLabel == null) {
				if (other.reportLabel != null) {
					return false;
				}
			} else if (!reportLabel.equals(other.reportLabel)) {
				return false;
			}
			if (reportPeriod != other.reportPeriod) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Returns a new instance of the builder class
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Builder class for the report
	 * 
	 *
	 */
	public final static class Builder implements PluginDataBuilder {
		private Builder(Data data) {
			this.data = data;
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

		private Data data;

		/**
		 * Returns a PersonPropertyReportPluginData created from the collected
		 * inputs
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_LABEL} if the
		 *             report label is not assigned</li>
		 *             <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if the
		 *             report period is not assigned</li>
		 * 
		 * 
		 */
		public RegionTransferReportPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new RegionTransferReportPluginData(data);

		}

		/**
		 * Sets the report label
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_LABEL} if the
		 *             report label is null</li>
		 */
		public Builder setReportLabel(ReportLabel reportLabel) {
			ensureDataMutability();
			if (reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}
			data.reportLabel = reportLabel;
			return this;
		}

		/**
		 * Sets the report period id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if the
		 *             report period is null</li>
		 */
		public Builder setReportPeriod(ReportPeriod reportPeriod) {
			ensureDataMutability();
			if (reportPeriod == null) {
				throw new ContractException(ReportError.NULL_REPORT_PERIOD);
			}
			data.reportPeriod = reportPeriod;
			return this;
		}

	}

	private final Data data;

	private RegionTransferReportPluginData(Data data) {
		this.data = data;
	}

	@Override
	public Builder getCloneBuilder() {
		return new Builder(data);
	}

	public ReportLabel getReportLabel() {
		return data.reportLabel;
	}

	public ReportPeriod getReportPeriod() {
		return data.reportPeriod;
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
	
	

}