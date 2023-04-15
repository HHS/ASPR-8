package plugins.personproperties.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.personproperties.support.PersonPropertyId;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * A PluginData class supporting PersonPropertyReport construction.
 */
@ThreadSafe
public final class PersonPropertyReportPluginData implements PluginData {

	/*
	 * Data class for collecting the inputs to the report
	 */
	private static class Data {
		private ReportLabel reportLabel;
		private ReportPeriod reportPeriod;
		private Set<PersonPropertyId> includedProperties = new LinkedHashSet<>();
		private Set<PersonPropertyId> excludedProperties = new LinkedHashSet<>();
		private boolean defaultInclusionPolicy = true;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			reportLabel = data.reportLabel;
			reportPeriod = data.reportPeriod;
			includedProperties.addAll(data.includedProperties);
			excludedProperties.addAll(data.excludedProperties);
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
		public PersonPropertyReportPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new PersonPropertyReportPluginData(data);

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
		 * Selects the given person property id to be included in the report.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             person property id is null</li>
		 */
		public Builder includePersonProperty(PersonPropertyId personPropertyId) {
			ensureDataMutability();
			if (personPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.includedProperties.add(personPropertyId);
			data.excludedProperties.remove(personPropertyId);
			return this;
		}

		/**
		 * Selects the given person property id to be excluded from the report
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             person property id is null</li>
		 */
		public Builder excludePersonProperty(PersonPropertyId personPropertyId) {
			ensureDataMutability();
			if (personPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.includedProperties.remove(personPropertyId);
			data.excludedProperties.add(personPropertyId);
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

	private PersonPropertyReportPluginData(Data data) {
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

	public Set<PersonPropertyId> getIncludedProperties() {
		return new LinkedHashSet<>(data.includedProperties);
	}

	public Set<PersonPropertyId> getExcludedProperties() {
		return new LinkedHashSet<>(data.excludedProperties);
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
		if (!(obj instanceof PersonPropertyReportPluginData)) {
			return false;
		}
		PersonPropertyReportPluginData other = (PersonPropertyReportPluginData) obj;
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