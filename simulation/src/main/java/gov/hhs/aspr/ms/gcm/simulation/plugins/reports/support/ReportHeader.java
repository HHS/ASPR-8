package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * An immutable, ordered container for the string values in the header of a
 * report. Constructed via the contained builder class.
 */
@Immutable
public final class ReportHeader {

	private static class Data {
		private ReportLabel reportLabel;
		private final List<String> headerStrings = new ArrayList<>();

		public Data() {

		}

		public Data(Data data) {
			reportLabel = data.reportLabel;
			headerStrings.addAll(data.headerStrings);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((reportLabel == null) ? 0 : reportLabel.hashCode());
			result = prime * result + ((headerStrings == null) ? 0 : headerStrings.hashCode());
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
			if (headerStrings == null) {
				if (other.headerStrings != null) {
					return false;
				}
			} else if (!headerStrings.equals(other.headerStrings)) {
				return false;
			}
			return true;
		}
	}

	private final Data data;

	private ReportHeader(Data data) {
		this.data = data;
	}

	/**
	 * Returns a builder for ReportHeader
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for ReportHeader
	 */
	@NotThreadSafe
	public final static class Builder {

		private Data data = new Data();
		private Builder() {

		}

		private List<String> headerStrings = new ArrayList<>();

		/**
		 * Add a string to the list of strings in the header in the order added.
		 * 
		 * @throws ContractException {@linkplain ReportError#NULL_REPORT_HEADER_STRING}
		 *                           if the header string is null
		 */
		public Builder add(String headerString) {
			if (headerString == null) {
				throw new ContractException(ReportError.NULL_REPORT_HEADER_STRING);
			}
			this.headerStrings.add(headerString);
			return this;
		}

		/**
		 * Sets the report type for this {@link ReportHeader}. The report type should be
		 * the class type of the report that authors the report item.
		 */
		public Builder setReportLabel(ReportLabel reportLabel) {
			if (reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}
			data.reportLabel = reportLabel;
			return this;
		}

		/*
		 * Null checks for the various fields.
		 */
		private void validateData() {

			if (data.reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}

		}

		/**
		 * Returns a report header from the collected header strings. Clears the state
		 * of the builder.
		 */
		public ReportHeader build() {
			validateData();
			return new ReportHeader(new Data(data));
		}
	}

	/**
	 * Returns the list of header strings in the order of addition.
	 */
	public List<String> getHeaderStrings() {
		return new ArrayList<>(data.headerStrings);
	}

	/**
	 * Returns the report label for this report item
	 */
	public ReportLabel getReportLabel() {
		return data.reportLabel;
	}

	/**
	 * String representation that preserves the order of the added strings presented
	 * as: ReportHeader [reportLabel=reportLabel, headerStrings=[string1, string2...]
	 */
	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("ReportHeader [reportLabel=");
		builder2.append(data.reportLabel);
		builder2.append(", headerStrings=");
		builder2.append(data.headerStrings);
		builder2.append("]");
		return builder2.toString();
	}

	/**
	 * Returns a standard hash code from the header strings
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	/**
	 * Report headers are equal if and only if their header strings are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ReportHeader)) {
			return false;
		}
		ReportHeader other = (ReportHeader) obj;
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
