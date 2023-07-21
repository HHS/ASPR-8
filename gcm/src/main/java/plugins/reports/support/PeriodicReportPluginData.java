package plugins.reports.support;

import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import util.errors.ContractException;

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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
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
         * @throws ContractException
         *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
         *                           the
         *                           report label is null</li>
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
         * @throws ContractException
         *                           <li>{@linkplain ReportError#NULL_REPORT_PERIOD} if
         *                           the
         *                           report period is null</li>
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
        if (!(obj instanceof PeriodicReportPluginData)) {
            return false;
        }
        PeriodicReportPluginData other = (PeriodicReportPluginData) obj;
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
