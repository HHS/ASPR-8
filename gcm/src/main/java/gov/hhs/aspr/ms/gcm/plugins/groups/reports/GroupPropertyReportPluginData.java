package gov.hhs.aspr.ms.gcm.plugins.groups.reports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupError;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.PeriodicReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

/**
 * A PluginData class supporting GroupPropertyReport construction.
 */
@ThreadSafe
public final class GroupPropertyReportPluginData extends PeriodicReportPluginData {

	private final Data data;

	private GroupPropertyReportPluginData(Data data) {
		super(data);
		this.data = data;
	}

	/*
	 * Data class for collecting the inputs to the report
	 */
	private static class Data extends PeriodicReportPluginData.Data {
		private Map<GroupTypeId, Set<GroupPropertyId>> includedProperties = new LinkedHashMap<>();
		private Map<GroupTypeId, Set<GroupPropertyId>> excludedProperties = new LinkedHashMap<>();
		private boolean defaultInclusionPolicy = true;

		private Data() {
			super();
		}

		private Data(Data data) {
			super(data);
			for (GroupTypeId groupTypeId : data.includedProperties.keySet()) {
				Set<GroupPropertyId> set = data.includedProperties.get(groupTypeId);
				Set<GroupPropertyId> newSet = new LinkedHashSet<>(set);
				includedProperties.put(groupTypeId, newSet);
			}
			for (GroupTypeId groupTypeId : data.excludedProperties.keySet()) {
				Set<GroupPropertyId> set = data.excludedProperties.get(groupTypeId);
				Set<GroupPropertyId> newSet = new LinkedHashSet<>(set);
				excludedProperties.put(groupTypeId, newSet);
			}
			defaultInclusionPolicy = data.defaultInclusionPolicy;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + (defaultInclusionPolicy ? 1231 : 1237);
			result = prime * result + ((excludedProperties == null) ? 0 : excludedProperties.hashCode());
			result = prime * result + ((includedProperties == null) ? 0 : includedProperties.hashCode());
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
			if (excludedProperties == null) {
				if (other.excludedProperties != null) {
					return false;
				}
			} else if (!excludedProperties.equals(other.excludedProperties)) {
				return false;
			}
			if (includedProperties == null) {
				if (other.includedProperties != null) {
					return false;
				}
			} else if (!includedProperties.equals(other.includedProperties)) {
				return false;
			}

			return super.equals(other);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(super.toString());
			builder.append(", includedProperties=");
			builder.append(includedProperties);
			builder.append(", excludedProperties=");
			builder.append(excludedProperties);
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
		 * Returns a GroupPropertyReportPluginData created from the collected inputs
		 * 
		 * @throws util.errors.ContractException
		 *                           <ul>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
		 *                           the report label is not assigned</li>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if
		 *                           the report period is not assigned</li>
		 *                           </ul>
		 */
		public GroupPropertyReportPluginData build() {
			return new GroupPropertyReportPluginData(data);
		}

		/**
		 * Sets the default policy for inclusion of group properties in the report. This
		 * policy is used when a group property has not been explicitly included or
		 * excluded. Defaulted to true.
		 */
		public Builder setDefaultInclusion(boolean include) {
			data.defaultInclusionPolicy = include;
			return this;
		}

		/**
		 * Selects the given group property id to be included in the report.
		 * 
		 * @throws util.errors.ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the group property id is null</li>
		 *                           <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if
		 *                           the group type id is null</li>
		 *                           </ul>
		 */
		public Builder includeGroupProperty(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
			if (groupTypeId == null) {
				throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
			}
			if (groupPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			Set<GroupPropertyId> set = data.includedProperties.get(groupTypeId);
			if (set == null) {
				set = new LinkedHashSet<>();
				data.includedProperties.put(groupTypeId, set);
			}
			set.add(groupPropertyId);
			set = data.excludedProperties.get(groupTypeId);
			if (set != null) {
				set.remove(groupPropertyId);
			}
			return this;
		}

		/**
		 * Selects the given group property id to be excluded from the report
		 * 
		 * @throws util.errors.ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the group property id is null</li>
		 *                           <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if
		 *                           the group type id is null</li>
		 *                           </ul>
		 */
		public Builder excludeGroupProperty(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
			if (groupPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			if (groupTypeId == null) {
				throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
			}
			Set<GroupPropertyId> set = data.excludedProperties.get(groupTypeId);
			if (set == null) {
				set = new LinkedHashSet<>();
				data.excludedProperties.put(groupTypeId, set);
			}
			set.add(groupPropertyId);
			set = data.includedProperties.get(groupTypeId);
			if (set != null) {
				set.remove(groupPropertyId);
			}
			return this;
		}

		/**
		 * Sets the report label
		 * 
		 * @throws util.errors.ContractException {@linkplain ReportError#NULL_REPORT_LABEL} if the
		 *                           report label is null
		 */
		@Override
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

	/**
	 * Returns the included group property values for the given group type id
	 * 
	 * @throws util.errors.ContractException {@linkplain GroupError#NULL_GROUP_TYPE_ID} if the
	 *                           group type id is null
	 */
	public Set<GroupPropertyId> getIncludedProperties(GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		Set<GroupPropertyId> result = new LinkedHashSet<>();
		Set<GroupPropertyId> set = data.includedProperties.get(groupTypeId);
		if (set != null) {
			result.addAll(set);
		}
		return result;
	}

	/**
	 * Returns the included group property values for the given group type id
	 * 
	 * @throws util.errors.ContractException {@linkplain PropertyError#NULL_GROUP_TYPE_ID} if
	 *                           the group type id is null
	 */
	public Set<GroupTypeId> getGroupTypeIds() {
		Set<GroupTypeId> result = new LinkedHashSet<>();
		result.addAll(data.includedProperties.keySet());
		result.addAll(data.excludedProperties.keySet());
		return result;
	}

	/**
	 * Returns the excluded group property values for the given group type id
	 * 
	 * @throws util.errors.ContractException {@linkplain GroupError#NULL_GROUP_TYPE_ID} if the
	 *                           group type id is null
	 */
	public Set<GroupPropertyId> getExcludedProperties(GroupTypeId groupTypeId) {
		if (groupTypeId == null) {
			throw new ContractException(GroupError.NULL_GROUP_TYPE_ID);
		}
		Set<GroupPropertyId> result = new LinkedHashSet<>();
		Set<GroupPropertyId> set = data.excludedProperties.get(groupTypeId);
		if (set != null) {
			result.addAll(set);
		}
		return result;
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
		if (!(obj instanceof GroupPropertyReportPluginData)) {
			return false;
		}
		GroupPropertyReportPluginData other = (GroupPropertyReportPluginData) obj;
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
		builder2.append("GroupPropertyReportPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

}