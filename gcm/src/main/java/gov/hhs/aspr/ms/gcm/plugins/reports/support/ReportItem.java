package gov.hhs.aspr.ms.gcm.plugins.reports.support;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

/**
 * A thread safe(immutable), container that supports output lines for multiple
 * reports. The values contained in a report item should be immutable and
 * support toString().
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

		private Data data = new Data();

		/**
		 * Adds an entry's string value to the report item. Order should follow the
		 * order in the {@link ReportHeader}
		 * 
		 * @throws util.errors.ContractException if the entry is null
		 */
		public Builder addValue(final Object entry) {
			if (entry == null) {
				throw new ContractException(ReportError.NULL_REPORT_ITEM_ENTRY);
			}
			data.values.add(entry.toString());
			return this;
		}

		/*
		 * Null checks for the various fields.
		 */
		private void validateData() {

			if (data.reportHeader == null) {
				throw new ContractException(ReportError.NULL_REPORT_HEADER);
			}

			if (data.reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}

		}

		/**
		 * Builds the {@link ReportItem} from the colleced data.
		 * 
		 * @throws util.errors.ContractException
		 *                           <ul>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_HEADER} if
		 *                           the collected report header is null</li>
		 *                           <li>{@linkplain ReportError#NULL_REPORT_LABEL} if
		 *                           the collected report label is null</li>
		 *                           </ul>
		 */
		public ReportItem build() {
			validateData();
			return new ReportItem(new Data(data));
		}

		/**
		 * Sets the associated {@link ReportHeader} for this {@link ReportItem}. The
		 * report header and the report item should have the same order of added fiels
		 * values.
		 */
		public Builder setReportHeader(ReportHeader reportHeader) {
			if (reportHeader == null) {
				throw new ContractException(ReportError.NULL_REPORT_HEADER);
			}
			data.reportHeader = reportHeader;
			return this;
		}

		/**
		 * Sets the report type for this {@link ReportItem}. The report type should be
		 * the class type of the report that authors the report item.
		 */
		public Builder setReportLabel(ReportLabel reportLabel) {
			if (reportLabel == null) {
				throw new ContractException(ReportError.NULL_REPORT_LABEL);
			}
			data.reportLabel = reportLabel;
			return this;
		}

	}

	private static class Data {
		private ReportLabel reportLabel;
		private ReportHeader reportHeader;
		private final List<String> values = new ArrayList<>();

		public Data() {

		}

		public Data(Data data) {
			reportLabel = data.reportLabel;
			reportHeader = data.reportHeader;
			values.addAll(data.values);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((reportHeader == null) ? 0 : reportHeader.hashCode());
			result = prime * result + ((reportLabel == null) ? 0 : reportLabel.hashCode());
			result = prime * result + ((values == null) ? 0 : values.hashCode());
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
			if (reportHeader == null) {
				if (other.reportHeader != null) {
					return false;
				}
			} else if (!reportHeader.equals(other.reportHeader)) {
				return false;
			}
			if (reportLabel == null) {
				if (other.reportLabel != null) {
					return false;
				}
			} else if (!reportLabel.equals(other.reportLabel)) {
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

	private final Data data;

	private ReportItem(final Data data) {
		this.data = data;
	}

	/**
	 * Returns the report label for this report item
	 */
	public ReportLabel getReportLabel() {
		return data.reportLabel;
	}

	/**
	 * Returns the report header for this report item
	 */
	public ReportHeader getReportHeader() {
		return data.reportHeader;
	}

	/**
	 * Returns the string value stored at the given index
	 *
	 * @throws IndexOutOfBoundsException
	 *                                   <li>if the index < 0</li>
	 *                                   <li>if the index >= size()</li>
	 *                                   </ul>
	 */
	public String getValue(final int index) {
		return data.values.get(index);
	}

	/**
	 * Returns the number of values stored in this report item
	 *
	 * @return
	 */
	public int size() {
		return data.values.size();
	}

	/**
	 * A string listing the values as added to this ReportItem delimited by commas
	 * in the form: ReportItem
	 * [reportType=reportType,reportHeader=reportHeader,values=[value1, value2...]]
	 */
	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("ReportItem [reportLabel=");
		builder2.append(data.reportLabel);
		builder2.append(", reportHeader=");
		builder2.append(data.reportHeader);
		builder2.append(", values=");
		builder2.append(data.values);
		builder2.append("]");
		return builder2.toString();
	}

	/**
	 * Returns the values in the form [value[0], value[1], ... ,value[N-1]]
	 */
	public String toValueString() {
		return data.values.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
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
