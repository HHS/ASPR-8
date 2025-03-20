package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support;

import java.util.Objects;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * Identifier for all people
 */
@Immutable
public final class PersonId implements Comparable<PersonId> {

	private final int id;

	/**
	 * Consructs the person id
	 * 
	 * @throws ContractException {@linkplain PersonError#NEGATIVE_PERSON_ID}
	 */
	public PersonId(int id) {
		if (id < 0) {
			throw new ContractException(PersonError.NEGATIVE_PERSON_ID);
		}
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	@Override
	public int compareTo(PersonId personId) {
		return Integer.compare(id, personId.id);
	}

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
     * Two {@link PersonId} instances are equal if and only if
     * their {@link #id} is equal.
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersonId other = (PersonId) obj;
		return id == other.id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}
}
