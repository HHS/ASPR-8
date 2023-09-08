package gov.hhs.aspr.ms.gcm.plugins.materials.support;

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

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BatchId)) {
			return false;
		}
		BatchId other = (BatchId) obj;
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
