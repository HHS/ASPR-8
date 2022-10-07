package temp.filtereventtests;

import net.jcip.annotations.Immutable;
import plugins.regions.support.RegionId;

@Immutable
public final class RegionIdentifier implements RegionId {
	private final int id;

	public RegionIdentifier(int id) {
		super();
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RegionIdentifier)) {
			return false;
		}
		RegionIdentifier other = (RegionIdentifier) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

}
