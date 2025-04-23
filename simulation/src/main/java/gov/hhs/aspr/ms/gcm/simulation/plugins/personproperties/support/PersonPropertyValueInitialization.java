package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support;

import java.util.Objects;

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

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(personPropertyId, value);
	}

	/**
     * Two {@link PersonPropertyValueInitialization} instances are equal if and only if
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
		PersonPropertyValueInitialization other = (PersonPropertyValueInitialization) obj;
		return Objects.equals(personPropertyId, other.personPropertyId) && Objects.equals(value, other.value);
	}
}
