package nucleus;

/**
 * A convenience class for representing a class reference as a ReportId
 * 
 * @author Shawn Hatch
 *
 */
public final class SimpleReportId implements ReportId {

	private final Object value;

	public SimpleReportId(Object value) {
		if (value == null) {
			throw new RuntimeException("null value");
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
