package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support;

import net.jcip.annotations.Immutable;

/**
 * Identifier for all material stages
 */
@Immutable
public final class StageId implements Comparable<StageId> {

	private final int id;

	public StageId(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	@Override
	public int compareTo(StageId personId) {
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
		if (!(obj instanceof StageId)) {
			return false;
		}
		StageId other = (StageId) obj;
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
