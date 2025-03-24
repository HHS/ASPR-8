package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import java.util.Objects;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginDataBuilder;
import gov.hhs.aspr.ms.util.errors.ContractException;

public abstract class PeriodicReportPluginData implements PluginData {

    protected final Data data;

    protected PeriodicReportPluginData(Data data) {
        validateBaseData(data);
        this.data = data;
    }

    /*
     * Data class for collecting the inputs to the report
     */
    protected static class Data {
        public ReportLabel reportLabel;
        public ReportPeriod reportPeriod;

        public Data() {
        }

        public Data(Data data) {
            reportLabel = data.reportLabel;
            reportPeriod = data.reportPeriod;
        }

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
        @Override
        public int hashCode() {
            return Objects.hash(reportLabel, reportPeriod);
        }

        /**
    	 * Two {@link Data} instances are equal if and only if
    	 * their inputs are equal.
    	 */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Data other = (Data) obj;
            return Objects.equals(reportLabel, other.reportLabel) && reportPeriod == other.reportPeriod;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Data [reportLabel=");
            builder.append(reportLabel);
            builder.append(", reportPeriod=");
            builder.append(reportPeriod);
            return builder.toString();
        }
    }

    /**
     * Builder class for the report
     */
    public static abstract class Builder implements PluginDataBuilder {

        protected Data data;

        protected Builder(Data data) {
            this.data = data;
        }

        public abstract PluginData build();

        /**
         * Sets the report label
         * 
         * @throws ContractException {@linkplain ReportError#NULL_REPORT_LABEL} if the
         *                           report label is null
         */
        public Builder setReportLabel(ReportLabel reportLabel) {
            if (reportLabel == null) {
                throw new ContractException(ReportError.NULL_REPORT_LABEL);
            }
            data.reportLabel = reportLabel;
            return this;
        }

        /**
         * Sets the report period id
         * 
         * @throws ContractException {@linkplain ReportError#NULL_REPORT_PERIOD} if the
         *                           report period is null
         */
        public Builder setReportPeriod(ReportPeriod reportPeriod) {
            if (reportPeriod == null) {
                throw new ContractException(ReportError.NULL_REPORT_PERIOD);
            }
            data.reportPeriod = reportPeriod;
            return this;
        }
    }

    protected final void validateBaseData(Data data) {
        if (data.reportLabel == null) {
            throw new ContractException(ReportError.NULL_REPORT_LABEL);
        }
        if (data.reportPeriod == null) {
            throw new ContractException(ReportError.NULL_REPORT_PERIOD);
        }
    }

    public ReportLabel getReportLabel() {
        return data.reportLabel;
    }

    public ReportPeriod getReportPeriod() {
        return data.reportPeriod;
    }

    /**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    /**
     * Two {@link PeriodicReportPluginData} instances are equal if and only if
     * their inputs are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PeriodicReportPluginData other = (PeriodicReportPluginData) obj;
        return Objects.equals(data, other.data);
    }
}
