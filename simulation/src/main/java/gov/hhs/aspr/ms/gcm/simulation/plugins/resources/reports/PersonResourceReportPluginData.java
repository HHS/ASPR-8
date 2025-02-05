package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.PeriodicReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

/**
 * A PluginData class supporting PersonResourceReport construction.
 */
@ThreadSafe
public final class PersonResourceReportPluginData extends PeriodicReportPluginData {

	private final Data data;

	private PersonResourceReportPluginData(Data data) {
		super(data);
		this.data = data;
	}

	/*
	 * Data class for collecting the inputs to the report
	 */
	private static class Data extends PeriodicReportPluginData.Data {
		private Set<ResourceId> includedResourceIds = new LinkedHashSet<>();
		private Set<ResourceId> excludedResourceIds = new LinkedHashSet<>();
		private boolean defaultInclusionPolicy = true;
		private boolean locked;

		private Data() {
			super();
		}

		private Data(Data data) {
			super(data);
			includedResourceIds.addAll(data.includedResourceIds);
			excludedResourceIds.addAll(data.excludedResourceIds);
			defaultInclusionPolicy = data.defaultInclusionPolicy;
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + (defaultInclusionPolicy ? 1231 : 1237);
			result = prime * result + ((excludedResourceIds == null) ? 0 : excludedResourceIds.hashCode());
			result = prime * result + ((includedResourceIds == null) ? 0 : includedResourceIds.hashCode());
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
			if (defaultInclusionPolicy != other.defaultInclusionPolicy) {
				return false;
			}
			if (excludedResourceIds == null) {
				if (other.excludedResourceIds != null) {
					return false;
				}
			} else if (!excludedResourceIds.equals(other.excludedResourceIds)) {
				return false;
			}
			if (includedResourceIds == null) {
				if (other.includedResourceIds != null) {
					return false;
				}
			} else if (!includedResourceIds.equals(other.includedResourceIds)) {
				return false;
			}

			return super.equals(other);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(super.toString());
			builder.append(", includedResourceIds=");
			builder.append(includedResourceIds);
			builder.append(", excludedResourceIds=");
			builder.append(excludedResourceIds);
			builder.append(", defaultInclusionPolicy=");
			builder.append(defaultInclusionPolicy);
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
		public PersonResourceReportPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new PersonResourceReportPluginData(data);
		}

		/**
		 * Sets the default policy for inclusion of person properties in the report.
		 * This policy is used when a person property has not been explicitly included
		 * or excluded. Defaulted to true.
		 */
		public Builder setDefaultInclusion(boolean include) {
			ensureDataMutability();
			data.defaultInclusionPolicy = include;
			return this;
		}

		/**
		 * Selects the given resource id to be included in the report.
		 * 
		 * @throws ContractException {@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *                           resource id is null
		 */
		public Builder includeResource(ResourceId resourceId) {
			ensureDataMutability();
			if (resourceId == null) {
				throw new ContractException(ResourceError.NULL_RESOURCE_ID);
			}
			data.includedResourceIds.add(resourceId);
			data.excludedResourceIds.remove(resourceId);
			return this;
		}

		/**
		 * Selects the given resource id to be excluded from the report
		 * 
		 * @throws ContractException {@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *                           resource id is null
		 */
		public Builder excludeResource(ResourceId resourceId) {
			ensureDataMutability();
			if (resourceId == null) {
				throw new ContractException(ResourceError.NULL_RESOURCE_ID);
			}
			data.includedResourceIds.remove(resourceId);
			data.excludedResourceIds.add(resourceId);
			return this;
		}

		/**
		 * Sets the report label
		 * 
		 * @throws ContractException {@linkplain ReportError#NULL_REPORT_LABEL} if the
		 *                           report label is null
		 */
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
	
	@Override
	public Builder toBuilder() {
		return new Builder(data);
	}

	public Set<ResourceId> getIncludedResourceIds() {
		return new LinkedHashSet<>(data.includedResourceIds);
	}

	public Set<ResourceId> getExcludedResourceIds() {
		return new LinkedHashSet<>(data.excludedResourceIds);
	}

	public boolean getDefaultInclusionPolicy() {
		return data.defaultInclusionPolicy;
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
		if (!(obj instanceof PersonResourceReportPluginData)) {
			return false;
		}
		PersonResourceReportPluginData other = (PersonResourceReportPluginData) obj;
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
		builder2.append("PersonResourceReportPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}
}