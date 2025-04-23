package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support;

import java.util.Objects;

import net.jcip.annotations.Immutable;

/**
 * Identifier for all batches
 */
@Immutable
public final class BatchId implements Comparable<BatchId> {

	private final int id;

	public BatchId(int id) {
		this.id = id;
	}

	public int getValue() {
		return id;
	}

	@Override
	public int compareTo(BatchId personId) {
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
     * Two {@link BatchId} instances are equal if and only if
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
		BatchId other = (BatchId) obj;
		return id == other.id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}

}
