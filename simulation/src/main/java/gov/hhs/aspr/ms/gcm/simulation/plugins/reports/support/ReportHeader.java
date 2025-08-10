package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			reportLabel = data.reportLabel;
			headerStrings.addAll(data.headerStrings);
			locked = data.locked;
		}

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
		@Override
		public int hashCode() {
			return Objects.hash(reportLabel, headerStrings);
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
			return Objects.equals(reportLabel, other.reportLabel) && Objects.equals(headerStrings, other.headerStrings);
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
		return new Builder(new Data());
	}

	/**
	 * Builder class for ReportHeader
	 */
	@NotThreadSafe
	public final static class Builder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

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
			ensureDataMutability();
			data.headerStrings.add(headerString);
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

			ensureDataMutability();
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
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new ReportHeader(data);
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
	 * as: ReportHeader [reportLabel=reportLabel, headerStrings=[string1,
	 * string2...]
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
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	/**
	 * Two {@link ReportHeader} instances are equal if and only if
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
		ReportHeader other = (ReportHeader) obj;
		return Objects.equals(data, other.data);
	}

	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}

}
