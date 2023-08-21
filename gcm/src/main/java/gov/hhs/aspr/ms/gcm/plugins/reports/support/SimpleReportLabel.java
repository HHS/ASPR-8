package gov.hhs.aspr.ms.gcm.plugins.reports.support;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * A convenience implementor of ReportLabel that wraps a value.
 */
@Immutable
public final class SimpleReportLabel implements ReportLabel {

	private final Object value;

	/**
	 * Creates a ReportLabel from a value. The value must implement a proper equals
	 * contract and be immutable.
	 * 
	 * @throws ContractException {@linkplain ReportError#NULL_REPORT_LABEL} if the
	 *                           value is null
	 */
	public SimpleReportLabel(Object value) {
		if (value == null) {
			throw new ContractException(ReportError.NULL_REPORT_LABEL);
		}
		this.value = value;
	}

	public Object getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SimpleReportLabel [value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SimpleReportLabel)) {
			return false;
		}
		SimpleReportLabel other = (SimpleReportLabel) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
