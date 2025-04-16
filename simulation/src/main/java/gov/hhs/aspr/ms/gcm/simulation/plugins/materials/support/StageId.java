package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support;

import java.util.Objects;

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

	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
     * Two {@link StageId} instances are equal if and only if
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
		StageId other = (StageId) obj;
		return id == other.id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}
}
