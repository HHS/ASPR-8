package plugins.resources.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import util.errors.ContractException;

/**
 * A PluginData class supporting PersonResourceReport construction.
 */
@ThreadSafe
public final class ResourceReportPluginData implements PluginData {

	/*
	 * Data class for collecting the inputs to the report
	 */
	private static class Data {
		private ReportLabel reportLabel;
		private ReportPeriod reportPeriod;

		private Set<ResourceId> includedResourceIds = new LinkedHashSet<>();
		private Set<ResourceId> excludedResourceIds = new LinkedHashSet<>();
		private boolean defaultInclusionPolicy = true;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			reportLabel = data.reportLabel;
			reportPeriod = data.reportPeriod;
			includedResourceIds.addAll(data.includedResourceIds);
			excludedResourceIds.addAll(data.excludedResourceIds);
			defaultInclusionPolicy = data.defaultInclusionPolicy;
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (defaultInclusionPolicy ? 1231 : 1237);
			result = prime * result + ((excludedResourceIds == null) ? 0 : excludedResourceIds.hashCode());
			result = prime * result + ((includedResourceIds == null) ? 0 : includedResourceIds.hashCode());
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
		public ResourceReportPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new ResourceReportPluginData(data);

		}

		/**
		 * Sets the default policy for inclusion of person properties in the
		 * report. This policy is used when a person property has not been
		 * explicitly included or excluded. Defaulted to true.
		 */
		public Builder setDefaultInclusion(boolean include) {
			ensureDataMutability();
			data.defaultInclusionPolicy = include;
			return this;
		}


		/**
		 * Selects the given resource id to be included in the report.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 */
		public Builder includeResourceId(ResourceId resourceId) {
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
		 * @throws ContractException
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 */
		public Builder excludeResourceId(ResourceId resourceId) {
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

	private ResourceReportPluginData(Data data) {
		this.data = data;
	}

	@Override
	public Builder getCloneBuilder() {
		return new Builder(data);
	}

	@Override
	public Builder getEmptyBuilder() {
		return builder();
	}

	public ReportLabel getReportLabel() {
		return data.reportLabel;
	}

	public ReportPeriod getReportPeriod() {
		return data.reportPeriod;
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
		if (!(obj instanceof ResourceReportPluginData)) {
			return false;
		}
		ResourceReportPluginData other = (ResourceReportPluginData) obj;
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