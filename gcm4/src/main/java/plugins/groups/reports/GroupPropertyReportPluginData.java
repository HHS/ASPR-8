package plugins.groups.reports;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * A PluginData class supporting GroupPropertyReport construction.
 */
@ThreadSafe
public final class GroupPropertyReportPluginData implements PluginData {

	/*
	 * Data class for collecting the inputs to the report
	 */
	private static class Data {
		private ReportLabel reportLabel;
		private ReportPeriod reportPeriod;
		private Map<GroupTypeId, Set<GroupPropertyId>> includedProperties = new LinkedHashMap<>();
		private Map<GroupTypeId, Set<GroupPropertyId>> excludedProperties = new LinkedHashMap<>();
		private boolean defaultInclusionPolicy = true;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			reportLabel = data.reportLabel;
			reportPeriod = data.reportPeriod;
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
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (defaultInclusionPolicy ? 1231 : 1237);
			result = prime * result + ((excludedProperties == null) ? 0 : excludedProperties.hashCode());
			result = prime * result + ((includedProperties == null) ? 0 : includedProperties.hashCode());
			result = prime * result + (locked ? 1231 : 1237);
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
			if (locked != other.locked) {
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
		 * Returns a GroupPropertyReportPluginData created from the collected
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
		public GroupPropertyReportPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new GroupPropertyReportPluginData(data);

		}

		/**
		 * Sets the default policy for inclusion of group properties in the
		 * report. This policy is used when a group property has not been
		 * explicitly included or excluded. Defaulted to true.
		 */
		public Builder setDefaultInclusion(boolean include) {
			ensureDataMutability();
			data.defaultInclusionPolicy = include;
			return this;
		}

		/**
		 * Selects the given group property id to be included in the report.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             group property id is null</li>
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the
		 *             group type id is null</li>
		 */
		public Builder includeGroupProperty(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
			ensureDataMutability();
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
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             group property id is null</li>
		 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the
		 *             group type id is null</li>
		 */
		public Builder excludeGroupProperty(GroupTypeId groupTypeId, GroupPropertyId groupPropertyId) {
			ensureDataMutability();
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

	private GroupPropertyReportPluginData(Data data) {
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

	/**
	 * Returns the included group property values for the given group type id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the
	 *             group type id is null</li>
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
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_GROUP_TYPE_ID} if the
	 *             group type id is null</li>
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
	 * @throws ContractException
	 *             <li>{@linkplain GroupError#NULL_GROUP_TYPE_ID} if the
	 *             group type id is null</li>
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

}