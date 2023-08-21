package gov.hhs.aspr.ms.gcm.plugins.personproperties.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.PeriodicReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

/**
 * A PluginData class supporting PersonPropertyReport construction.
 */
@ThreadSafe
public final class PersonPropertyReportPluginData extends PeriodicReportPluginData {

	private final Data data;

	private PersonPropertyReportPluginData(Data data) {
		super(data);
		this.data = data;
	}

	/*
	 * Data class for collecting the inputs to the report
	 */
	private static class Data extends PeriodicReportPluginData.Data {
		private Set<PersonPropertyId> includedProperties = new LinkedHashSet<>();
		private Set<PersonPropertyId> excludedProperties = new LinkedHashSet<>();
		private boolean defaultInclusionPolicy = true;

		private Data() {
			super();
		}

		private Data(Data data) {
			super(data);
			includedProperties.addAll(data.includedProperties);
			excludedProperties.addAll(data.excludedProperties);
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
		public PersonPropertyReportPluginData build() {
			return new PersonPropertyReportPluginData(data);
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
		 * Selects the given person property id to be included in the report.
		 * 
		 * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *                           person property id is null
		 */
		public Builder includePersonProperty(PersonPropertyId personPropertyId) {
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
		 * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *                           person property id is null
		 */
		public Builder excludePersonProperty(PersonPropertyId personPropertyId) {
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
		 * @throws ContractException {@linkplain ReportError#NULL_REPORT_LABEL} if the
		 *                           report label is null
		 */
		public Builder setReportLabel(ReportLabel reportLabel) {
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

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("PersonPropertyReportPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

}