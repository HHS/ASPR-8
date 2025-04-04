package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.reports;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

/**
 * A PluginData class supporting RegionPropertyReport construction.
 */
@ThreadSafe
public final class RegionPropertyReportPluginData implements PluginData {

    /*
     * Data class for collecting the inputs to the report
     */
    private static class Data {
        private ReportLabel reportLabel;
        private Set<RegionPropertyId> includedProperties = new LinkedHashSet<>();
        private Set<RegionPropertyId> excludedProperties = new LinkedHashSet<>();
        private boolean defaultInclusionPolicy = true;

        private boolean locked;

        private Data() {
        }

        private Data(Data data) {
            reportLabel = data.reportLabel;
            includedProperties.addAll(data.includedProperties);
            excludedProperties.addAll(data.excludedProperties);
            defaultInclusionPolicy = data.defaultInclusionPolicy;
            locked = data.locked;
        }

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
        @Override
        public int hashCode() {
            return Objects.hash(reportLabel, includedProperties, excludedProperties, defaultInclusionPolicy);
        }

        /**
    	 * Two {@link Data} instances are equal if and only if
    	 * their inputs are equal.
    	 */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Data other = (Data) obj;
            return Objects.equals(reportLabel, other.reportLabel)
                    && Objects.equals(includedProperties, other.includedProperties)
                    && Objects.equals(excludedProperties, other.excludedProperties)
                    && defaultInclusionPolicy == other.defaultInclusionPolicy;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Data [reportLabel=");
            builder.append(reportLabel);
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
         * Returns a RegionPropertyPluginData created from the collected inputs
         *
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
         *                           the report label is not assigned</li>
         *                           <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if
         *                           the report period is not assigned</li>
         *                           </ul>
         */
        public RegionPropertyReportPluginData build() {

            if (!data.locked) {
                validateData();
            }
            ensureImmutability();
            return new RegionPropertyReportPluginData(data);

        }

        /**
         * Sets the default policy for inclusion of region properties in the report.
         * This policy is used when a region property has not been explicitly included
         * or excluded. Defaulted to true.
         */
        public Builder setDefaultInclusion(boolean include) {
            ensureDataMutability();
            data.defaultInclusionPolicy = include;
            return this;
        }

        /**
         * Selects the given region property id to be included in the report.
         *
         * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_ID} if the
         *                           person property id is null
         */
        public Builder includeRegionProperty(RegionPropertyId regionPropertyId) {
            ensureDataMutability();
            if (regionPropertyId == null) {
                throw new ContractException(PropertyError.NULL_PROPERTY_ID);
            }
            data.includedProperties.add(regionPropertyId);
            data.excludedProperties.remove(regionPropertyId);
            return this;
        }

        /**
         * Selects the given region property id to be excluded from the report
         *
         * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_ID} if the
         *                           person property id is null
         */
        public Builder excludeRegionProperty(RegionPropertyId regionPropertyId) {
            ensureDataMutability();
            if (regionPropertyId == null) {
                throw new ContractException(PropertyError.NULL_PROPERTY_ID);
            }
            data.includedProperties.remove(regionPropertyId);
            data.excludedProperties.add(regionPropertyId);
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
            if (reportLabel == null) {
                throw new ContractException(ReportError.NULL_REPORT_LABEL);
            }
            data.reportLabel = reportLabel;
            return this;
        }
    }

    private final Data data;

    private RegionPropertyReportPluginData(Data data) {
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
    public Builder toBuilder() {
        return new Builder(data);
    }

    public ReportLabel getReportLabel() {
        return data.reportLabel;
    }

    public Set<RegionPropertyId> getIncludedProperties() {
        return data.includedProperties;
    }

    public Set<RegionPropertyId> getExcludedProperties() {
        return data.excludedProperties;
    }

    public boolean getDefaultInclusionPolicy() {
        return data.defaultInclusionPolicy;
    }

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    /**
     * Two {@link RegionPropertyReportPluginData} instances are equal if and only if
     * their inputs are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RegionPropertyReportPluginData other = (RegionPropertyReportPluginData) obj;
        return Objects.equals(data, other.data);
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("RegionPropertyReportPluginData [data=");
        builder2.append(data);
        builder2.append("]");
        return builder2.toString();
    }

}