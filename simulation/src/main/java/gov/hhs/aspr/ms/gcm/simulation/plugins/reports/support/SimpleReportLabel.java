package gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support;

import java.util.Objects;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

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

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	/**
	 * Two {@link SimpleReportLabel} instances are equal if and only if
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
		SimpleReportLabel other = (SimpleReportLabel) obj;
		return Objects.equals(value, other.value);
	}

}
