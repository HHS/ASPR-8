package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.reports;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.PeriodicReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

/**
 * A PluginData class supporting GroupPopulationReport construction.
 */
@ThreadSafe
public final class GroupPopulationReportPluginData extends PeriodicReportPluginData {

	private final Data data;

	private GroupPopulationReportPluginData(Data data) {
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

	/**
	 * Builder class for the report
	 */
	public final static class Builder extends PeriodicReportPluginData.Builder {

		private Data data;

		private Builder(Data data) {
			super(data);
			this.data = data;
		}

		/**
		 * Returns a PersonPropertyReportPluginData created from the collected inputs
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
		 *                           the report label is not assigned</li>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if
		 *                           the report period is not assigned</li>
		 *                           </ul>
		 */
		@Override
		public GroupPopulationReportPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new GroupPopulationReportPluginData(data);
		}

		/**
		 * Sets the report label
		 * 
		 * @throws ContractException {@linkplain ReportError#NULL_REPORT_LABEL} if the
		 *                           report label is null
		 */
		@Override
		public Builder setReportLabel(ReportLabel reportLabel) {
			ensureDataMutability();
			super.setReportLabel(reportLabel);
			return this;
		}

		/**
		 * Sets the report period id
		 * 
		 * @throws ContractException {@linkplain ReportError#NULL_REPORT_PERIOD} if the
		 *                           report period is null
		 */
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

	/**
	 * Returns a new instance of the builder class
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Returns the current version of this Simulation Plugin, which is equal to the
	 * version of the GCM Simulation
	 */
	public String getVersion() {
		return StandardVersioning.VERSION;
	}

	/**
	 * Given a version string, returns whether the version is a supported version or
	 * not.
	 */
	public static boolean checkVersionSupported(String version) {
		return StandardVersioning.checkVersionSupported(version);
	}
	
	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
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