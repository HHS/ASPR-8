package gov.hhs.aspr.ms.gcm.lessons.plugins.model.support;

import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import net.jcip.annotations.Immutable;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Identifier for all regions
 *
 *
 */

@Immutable
public final class Region implements RegionId {

	private final int id;

	/**
	 * Constructs the region
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain ModelError#NEGATIVE_REGION_ID}</li>
	 */
	public Region(final int id) {
		if (id < 0) {
			throw new ContractException(ModelError.NEGATIVE_REGION_ID);
		}
		this.id = id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Region)) {
			return false;
		}
		final Region other = (Region) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	public int getValue() {
		return id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return "Region_" + id;
	}
}
