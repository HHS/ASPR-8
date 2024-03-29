package gov.hhs.aspr.ms.gcm.lessons.plugins.people;

import net.jcip.annotations.Immutable;

@Immutable
/* start code_ref=plugin_dependencies_defining_a_person_id|code_cap=The PersonId class defines people and wraps an int value.*/
public final class PersonId implements Comparable<PersonId> {

	private final int id;

	public PersonId(int id) {
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
/* end */
