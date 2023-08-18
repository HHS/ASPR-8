package gov.hhs.aspr.ms.gcm.plugins.resources.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.plugins.reports.support.PeriodicReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

/**
 * A PluginData class supporting PersonResourceReport construction.
 */
@ThreadSafe
public final class ResourceReportPluginData extends PeriodicReportPluginData {

	private final Data data;

	private ResourceReportPluginData(Data data) {
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

		private Data() {
			super();
		}

		private Data(Data data) {
			super(data);
			includedResourceIds.addAll(data.includedResourceIds);
			excludedResourceIds.addAll(data.excludedResourceIds);
			defaultInclusionPolicy = data.defaultInclusionPolicy;
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
		 * @throws util.errors.ContractException
		 *                           <ul>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
		 *                           the report label is not assigned</li>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if
		 *                           the report period is not assigned</li>
		 *                           </ul>
		 */
		public ResourceReportPluginData build() {
			return new ResourceReportPluginData(data);
		}

		/**
		 * Sets the default policy for inclusion of person properties in the report.
		 * This policy is used when a person property has not been explicitly included
		 * or excluded. Defaulted to true.
		 */
		public Builder setDefaultInclusion(boolean include) {
			data.defaultInclusionPolicy = include;
			return this;
		}

		/**
		 * Selects the given resource id to be included in the report.
		 * 
		 * @throws util.errors.ContractException {@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *                           resource id is null
		 */
		public Builder includeResource(ResourceId resourceId) {
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
		 * @throws util.errors.ContractException {@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *                           resource id is null
		 */
		public Builder excludeResource(ResourceId resourceId) {
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
		 * @throws util.errors.ContractException {@linkplain ReportError#NULL_REPORT_LABEL} if the
		 *                           report label is null
		 */
		public Builder setReportLabel(ReportLabel reportLabel) {
			super.setReportLabel(reportLabel);
			return this;
		}

		/**
		 * Sets the report period id
		 * 
		 * @throws util.errors.ContractException {@linkplain ReportError#NULL_REPORT_PERIOD} if the
		 *                           report period is null
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

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("ResourceReportPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

}