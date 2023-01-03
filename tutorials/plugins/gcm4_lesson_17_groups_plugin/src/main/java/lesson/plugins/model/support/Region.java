package lesson.plugins.model.support;

import net.jcip.annotations.Immutable;
import plugins.regions.support.RegionId;
import util.errors.ContractException;

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
	 *             <li>{@linkplain ModelError#NEGATIVE_REGION_ID}</li>
	 */
	public Region(int id) {
		if (id < 0) {
			throw new ContractException(ModelError.NEGATIVE_REGION_ID);
		}
		this.id = id;
	}

	public int getValue() {
		return id;
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
		if (!(obj instanceof Region)) {
			return false;
		}
		Region other = (Region) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Region_"+id;
	}
 }
