package plugins.reports.support;

import net.jcip.annotations.Immutable;
import nucleus.util.ContractException;

/**
 * A convenience implementor of ReportId that wraps a value.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class SimpleReportId implements ReportId {

	private final Object value;

	/**
	 * Creates a ReportId from a value. The value must implement a proper equals
	 * contract and be immutable.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ReportError#NULL_REPORT_ID} if the value is
	 *             null</li>
	 */
	public SimpleReportId(Object value) {
		if (value == null) {
			throw new ContractException(ReportError.NULL_REPORT_ID);
		}
		this.value = value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SimpleReportId [value=");
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
		if (!(obj instanceof SimpleReportId)) {
			return false;
		}
		SimpleReportId other = (SimpleReportId) obj;
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
