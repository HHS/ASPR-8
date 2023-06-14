package plugins.groups.reports;

import net.jcip.annotations.ThreadSafe;
import plugins.reports.support.PeriodicReportPluginData;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import util.errors.ContractException;

/**
 * A PluginData class supporting GroupPopulationReport construction.
 */
@ThreadSafe
public final class GroupPopulationReportPluginData extends PeriodicReportPluginData {

	private GroupPopulationReportPluginData(Data data) {
		super(data);
	}

	/**
	 * Builder class for the report
	 */
	public final static class Builder extends PeriodicReportPluginData.Builder {

		private Builder(Data data) {
			super(data);
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
		@Override
		public GroupPopulationReportPluginData build() {
			super.validateData();

			return new GroupPopulationReportPluginData(data);
		}

		/**
		 * Sets the report label
		 * 
		 * @throws ContractException
		 *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
		 *                           the
		 *                           report label is null</li>
		 */
		@Override
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
		@Override
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
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GroupPopulationReportPluginData [data=");
		builder.append(data);
		builder.append("]");
		builder.append("]");
		return builder.toString();
	}
}