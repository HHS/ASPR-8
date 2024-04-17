package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

/**
 * A PluginData class supporting BatchStatusReport construction.
 */
@ThreadSafe
public final class MaterialsProducerResourceReportPluginData implements PluginData {

	/*
	 * Data class for collecting the inputs to the report
	 */
	private static class Data {
		private ReportLabel reportLabel;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			reportLabel = data.reportLabel;
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((reportLabel == null) ? 0 : reportLabel.hashCode());
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
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [reportLabel=");
			builder.append(reportLabel);
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
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
		}

		private Data data;

		/**
		 * Returns a PersonPropertyReportPluginData created from the collected inputs
		 * 
		 * @throws ContractException {@linkplain ReportError#NULL_REPORT_LABEL} if the
		 *                           report label is not assigned
		 */
		public MaterialsProducerResourceReportPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new MaterialsProducerResourceReportPluginData(data);

		}

		/**
		 * Sets the report label
		 * 
		 * @throws ContractException {@linkplain ReportError#NULL_REPORT_LABEL} if the
		 *                           report label is null
		 */
		public Builder setReportLabel(ReportLabel reportLabel) {
			ensureDataMutability();
			if (reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}
			data.reportLabel = reportLabel;
			return this;
		}

	}

	private final Data data;

	private MaterialsProducerResourceReportPluginData(Data data) {
		this.data = data;
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
	
	@Override
	public Builder getCloneBuilder() {
		return new Builder(data);
	}

	public ReportLabel getReportLabel() {
		return data.reportLabel;
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
		if (!(obj instanceof MaterialsProducerResourceReportPluginData)) {
			return false;
		}
		MaterialsProducerResourceReportPluginData other = (MaterialsProducerResourceReportPluginData) obj;
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
		builder2.append("MaterialsProducerResourceReportPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

}