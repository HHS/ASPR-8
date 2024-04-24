package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support;

public class PersonPropertyValueInitialization {
	private final PersonPropertyId personPropertyId;
	private final Object value;

	public PersonPropertyValueInitialization(PersonPropertyId personPropertyId, Object value) {
		super();
		this.personPropertyId = personPropertyId;
		this.value = value;
	}

	public PersonPropertyId getPersonPropertyId() {
		return personPropertyId;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersonPropertyAssignment [personPropertyId=");
		builder.append(personPropertyId);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((personPropertyId == null) ? 0 : personPropertyId.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PersonPropertyValueInitialization)) {
			return false;
		}
		PersonPropertyValueInitialization other = (PersonPropertyValueInitialization) obj;
		if (personPropertyId == null) {
			if (other.personPropertyId != null) {
				return false;
			}
		} else if (!personPropertyId.equals(other.personPropertyId)) {
			return false;
		}
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
