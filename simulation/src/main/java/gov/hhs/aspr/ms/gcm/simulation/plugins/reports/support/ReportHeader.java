package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

/**
 * An immutable, ordered container for the string values in the header of a
 * report. Constructed via the contained builder class.
 */
@Immutable
public final class ReportHeader {

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

	private static class Data {
		private List<String> headerStrings = new ArrayList<>();
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			headerStrings.addAll(data.headerStrings);
			locked = data.locked;
		}
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
			ensureDataMutability();
			if (headerString == null) {
				throw new ContractException(ReportError.NULL_REPORT_HEADER_STRING);
			}
			data.headerStrings.add(headerString);
			return this;
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

		private void validateData() {
		}
	}

	/**
	 * Returns the list of header strings in the order of addition.
	 */
	public List<String> getHeaderStrings() {
		return new ArrayList<>(data.headerStrings);
	}

	/**
	 * String representation that preserves the order of the added strings presented
	 * as: ReportHeader [headerStrings=[string1, string2...]
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReportHeader [headerStrings=");
		builder.append(data.headerStrings);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns a standard hash code from the header strings
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data.headerStrings == null) ? 0 : data.headerStrings.hashCode());
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
		if (data.headerStrings == null) {
			if (other.data.headerStrings != null) {
				return false;
			}
		} else if (!data.headerStrings.equals(other.data.headerStrings)) {
			return false;
		}
		return true;
	}

	public Builder toBuilder() {
		return new Builder(data);
	}

}
