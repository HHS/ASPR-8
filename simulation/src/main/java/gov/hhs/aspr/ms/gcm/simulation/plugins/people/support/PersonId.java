package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support;

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

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PersonId)) {
			return false;
		}
		PersonId other = (PersonId) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}
}
