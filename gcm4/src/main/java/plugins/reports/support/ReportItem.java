package plugins.reports.support;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

/**
 * A thread safe(immutable), container that supports output lines for multiple
 * reports. The values contained in a report item should be immutable and
 * support toString().
 *
 *
 */
@ThreadSafe
public final class ReportItem {

	/**
	 * Returns a new Builder instance.
	 */
	public static Builder builder() {
		return new Builder();
	}

	@NotThreadSafe
	public final static class Builder {
		private Builder() {
		}

		private Scaffold scaffold = new Scaffold();

		/**
		 * Adds an entry's string value to the report item. Order should follow the order in the
		 * {@link ReportHeader}
		 * 
		 * @throws ContractException
		 *             <li>if the entry is null</li>
		 */
		public Builder addValue(final Object entry) {
			if (entry == null) {
				throw new ContractException(ReportError.NULL_REPORT_ITEM_ENTRY);
			}
			scaffold.values.add(entry.toString());
			return this;
		}

		/*
		 * Null checks for the various fields.
		 */
		private void validateData() {

			if (scaffold.reportHeader == null) {
				throw new ContractException(ReportError.NULL_REPORT_HEADER);
			}

			if (scaffold.reportId == null) {
				throw new ContractException(ReportError.NULL_REPORT_ID);
			}

		}

		/**
		 * Builds the {@link ReportItem} from the colleced data.
		 * 
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain ReportError#NULL_REPORT_HEADER} if the collected report header is null</li>
		 *             <li>{@linkplain ReportError#NULL_REPORT_ID} if the collected report id is null</li>
		 * 
		 */
		public ReportItem build() {
			try {
				validateData();
				return new ReportItem(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the associated {@link ReportHeader} for this {@link ReportItem}.
		 * The report header and the report item should have the same order of
		 * added fiels values.
		 */
		public Builder setReportHeader(ReportHeader reportHeader) {
			if (reportHeader == null) {
				throw new ContractException(ReportError.NULL_REPORT_HEADER);
			}
			scaffold.reportHeader = reportHeader;
			return this;
		}

		/**
		 * Sets the report type for this {@link ReportItem}. The report type
		 * should be the class type of the report that authors the report item.
		 */
		public Builder setReportId(ReportId reportId) {
			if (reportId == null) {
				throw new ContractException(ReportError.NULL_REPORT_ID);
			}
			scaffold.reportId = reportId;
			return this;
		}

	}

	private static class Scaffold {
		private ReportId reportId;
		private ReportHeader reportHeader;
		private final List<String> values = new ArrayList<>();
	}

	private final ReportId reportId;

	private final List<String> values;

	private final ReportHeader reportHeader;

	private ReportItem(final Scaffold scaffold) {
		reportId = scaffold.reportId;
		reportHeader = scaffold.reportHeader;
		values = scaffold.values;
	}

	/**
	 * Returns the report id for this report item
	 */
	public ReportId getReportId() {
		return reportId;
	}

	/**
	 * Returns the report header for this report item
	 */
	public ReportHeader getReportHeader() {
		return reportHeader;
	}

	/**
	 * Returns the string value stored at the given index
	 *
	 *@throws IndexOutOfBoundsException
	 *<li> if the index < 0</li>	 
	 *<li> if the index >= size()</li>
	 */
	public String getValue(final int index) {
		return values.get(index);
	}

	/**
	 * Returns the number of values stored in this report item
	 *
	 * @return
	 */
	public int size() {
		return values.size();
	}

	/**
	 * A string listing the values as added to this ReportItem delimited by
	 * commas in the form:
	 * 
	 * ReportItem
	 * [reportType=reportType,reportHeader=reportHeader,values=[value1,
	 * value2...]]
	 */
	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("ReportItem [reportId=");
		builder2.append(reportId);
		builder2.append(", reportHeader=");
		builder2.append(reportHeader);
		builder2.append(", values=");
		builder2.append(values);
		builder2.append("]");
		return builder2.toString();
	}
	
	/**
	 * Returns the values in the form [value[0], value[1], ... ,value[N-1]]
	 */
	public String toValueString() {
		return values.toString();	
	}

	/**
	 * Standard hash code implementation
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reportHeader == null) ? 0 : reportHeader.hashCode());
		result = prime * result + ((reportId == null) ? 0 : reportId.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	/**
	 * Two report items are equal iff and only if their ids, headers and ordered
	 * values are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ReportItem)) {
			return false;
		}
		ReportItem other = (ReportItem) obj;
		if (reportHeader == null) {
			if (other.reportHeader != null) {
				return false;
			}
		} else if (!reportHeader.equals(other.reportHeader)) {
			return false;
		}
		if (reportId == null) {
			if (other.reportId != null) {
				return false;
			}
		} else if (!reportId.equals(other.reportId)) {
			return false;
		}
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}

}
