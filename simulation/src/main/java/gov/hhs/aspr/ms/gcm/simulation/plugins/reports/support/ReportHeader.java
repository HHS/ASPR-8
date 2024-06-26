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

	private final List<String> headerStrings;

	private ReportHeader(List<String> headerStrings) {
		this.headerStrings = new ArrayList<>(headerStrings);
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
		 * Returns a report header from the collected header strings. Clears the state
		 * of the builder.
		 */
		public ReportHeader build() {
			return new ReportHeader(headerStrings);
		}
	}

	/**
	 * Returns the list of header strings in the order of addition.
	 */
	public List<String> getHeaderStrings() {
		return new ArrayList<>(headerStrings);
	}

	/**
	 * String representation that preserves the order of the added strings presented
	 * as: ReportHeader [headerStrings=[string1, string2...]
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ReportHeader [headerStrings=");
		builder.append(headerStrings);
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
		result = prime * result + ((headerStrings == null) ? 0 : headerStrings.hashCode());
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
