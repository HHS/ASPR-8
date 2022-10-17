package lesson.plugins.family;

import net.jcip.annotations.Immutable;

 @Immutable
 public final class FamilyId implements Comparable<FamilyId> {

	private final int id;

	public FamilyId(int id) {		
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	@Override
	public int compareTo(FamilyId familyId) {
		return Integer.compare(id, familyId.id);
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
		if (!(obj instanceof FamilyId)) {
			return false;
		}
		FamilyId other = (FamilyId) obj;
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
 
 
 
