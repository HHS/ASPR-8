package gov.hhs.aspr.ms.gcm.plugins.regions.reports;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (defaultInclusionPolicy ? 1231 : 1237);
            result = prime * result + ((excludedProperties == null) ? 0 : excludedProperties.hashCode());
            result = prime * result + ((includedProperties == null) ? 0 : includedProperties.hashCode());
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
            return true;
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

    @Override
    public Builder getCloneBuilder() {
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
        if (!(obj instanceof RegionPropertyReportPluginData)) {
            return false;
        }
        RegionPropertyReportPluginData other = (RegionPropertyReportPluginData) obj;
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
        builder2.append("RegionPropertyReportPluginData [data=");
        builder2.append(data);
        builder2.append("]");
        return builder2.toString();
    }

}