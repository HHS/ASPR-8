package plugins.people.support;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * Identifier for all people
 * 
 *
 */
@Immutable
public final class PersonId implements Comparable<PersonId> {

	private final int id;

	/**
	 * Consructs the person id
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PersonError#NEGATIVE_PERSON_ID}</li>
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
