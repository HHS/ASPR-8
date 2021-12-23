package util.earth;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;
import util.vector.MutableVector3D;
import util.vector.Vector3D;

/**
 * Represents an immutable, minimum rectangle aligned to the four compass
 * directions that contains some set of LatLon values.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public final class LatLonBox {

	/**
	 * Static method for creating a builder for LatLonBox
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for LatLonBox
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		private Builder() {
		}

		private List<LatLon> latLons = new ArrayList<>();

		/**
		 * Adds a LatLon to the set of points that will be used to form the
		 * LatLonBox
		 * 
		 * @throws RuntimeException
		 *             <li>if the latLon is null
		 */
		public Builder add(LatLon latLon) {
			if (latLon == null) {
				throw new RuntimeException("null latLon");
			}
			latLons.add(latLon);
			return this;
		}

		/**
		 * Adds the four corners of an existing LatLonBox to the set of points
		 * that will form the new LatLonBox
		 * 
		 * @throws RuntimeException
		 *             <li>if the latLonBox is null
		 * 
		 */
		public Builder add(LatLonBox latLonBox) {
			if (latLonBox == null) {
				throw new RuntimeException("null latLonBox");
			}
			add(latLonBox.getNorthEastLatLon());
			add(latLonBox.getNorthWestLatLon());
			add(latLonBox.getSouthEastLatLon());
			add(latLonBox.getSouthWestLatLon());
			return this;
		}

		/**
		 * Builds the LatLonBox from the collected LatLon points
		 */
		public LatLonBox build() {
			try {

				Earth earth = Earth.fromMeanRadius();
				MutableVector3D centroid = new MutableVector3D();

				double northLat = -90;
				double southLat = 90;
				for (LatLon latLon : latLons) {
					northLat = FastMath.max(northLat, latLon.getLatitude());
					southLat = FastMath.min(southLat, latLon.getLatitude());
				}

				MutableVector3D v = new MutableVector3D();
				for (LatLon latLon : latLons) {
					Vector3D ecc = earth.getECCFromLatLonAlt(new LatLonAlt(latLon));
					v.assign(ecc.getX(), ecc.getY(), 0);
					centroid.add(v);
				}

				double westLon = 180;
				double eastLon = -180;

				double maxDeltaLon = Double.NEGATIVE_INFINITY;
				double minDeltaLon = Double.POSITIVE_INFINITY;
				MutableVector3D u = new MutableVector3D();
				for (LatLon latLon : latLons) {
					Vector3D ecc = earth.getECCFromLatLonAlt(new LatLonAlt(latLon));
					v.assign(ecc.getX(), ecc.getY(), 0);
					double deltaLon = centroid.angle(v);
					u.assign(centroid);
					u.cross(v);
					if (u.getZ() < 0) {
						deltaLon = -deltaLon;
					}
					if (maxDeltaLon < deltaLon) {
						maxDeltaLon = deltaLon;
						eastLon = latLon.getLongitude();
					}
					if (minDeltaLon > deltaLon) {
						minDeltaLon = deltaLon;
						westLon = latLon.getLongitude();
					}
				}

				return new LatLonBox(//
						new LatLon(northLat, westLon), //
						new LatLon(southLat, westLon), //
						new LatLon(southLat, eastLon), //
						new LatLon(northLat, eastLon));//

			} finally {
				latLons = new ArrayList<>();
			}
		}
	}

	/*
	 * Hidden constructor
	 */
	private LatLonBox(LatLon northWestLatLon, LatLon southWestLatLon, LatLon southEastLatLon, LatLon northEastLatLon) {
		this.northWestLatLon = northWestLatLon;
		this.southWestLatLon = southWestLatLon;
		this.northEastLatLon = northEastLatLon;
		this.southEastLatLon = southEastLatLon;
	}

	/**
	 * Returns a string representing the four corners in NW, SW, SE, NE order
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LatLonBox [northWestLatLon=");
		builder.append(northWestLatLon);
		builder.append(", southWestLatLon=");
		builder.append(southWestLatLon);
		builder.append(", southEastLatLon=");
		builder.append(southEastLatLon);
		builder.append(", northEastLatLon=");
		builder.append(northEastLatLon);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns a hash code consistent with equals()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((northEastLatLon == null) ? 0 : northEastLatLon.hashCode());
		result = prime * result + ((northWestLatLon == null) ? 0 : northWestLatLon.hashCode());
		result = prime * result + ((southEastLatLon == null) ? 0 : southEastLatLon.hashCode());
		result = prime * result + ((southWestLatLon == null) ? 0 : southWestLatLon.hashCode());
		return result;
	}

	/**
	 * Returns true if and only if this LatLonBox is compared to another
	 * non-null instance of LatLonBox with matching corners.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LatLonBox other = (LatLonBox) obj;
		if (northEastLatLon == null) {
			if (other.northEastLatLon != null)
				return false;
		} else if (!northEastLatLon.equals(other.northEastLatLon))
			return false;
		if (northWestLatLon == null) {
			if (other.northWestLatLon != null)
				return false;
		} else if (!northWestLatLon.equals(other.northWestLatLon))
			return false;
		if (southEastLatLon == null) {
			if (other.southEastLatLon != null)
				return false;
		} else if (!southEastLatLon.equals(other.southEastLatLon))
			return false;
		if (southWestLatLon == null) {
			if (other.southWestLatLon != null)
				return false;
		} else if (!southWestLatLon.equals(other.southWestLatLon))
			return false;
		return true;
	}

	private final LatLon northWestLatLon;
	private final LatLon southWestLatLon;
	private final LatLon northEastLatLon;
	private final LatLon southEastLatLon;

	/**
	 * Returns the northwest corner LatLon
	 */
	public LatLon getNorthWestLatLon() {
		return northWestLatLon;
	}

	/**
	 * Returns the northeast corner LatLon
	 */
	public LatLon getNorthEastLatLon() {
		return northEastLatLon;
	}

	/**
	 * Returns the southeast corner LatLon
	 */
	public LatLon getSouthEastLatLon() {
		return southEastLatLon;
	}

	/**
	 * Returns the southwest corner LatLon
	 */
	public LatLon getSouthWestLatLon() {
		return southWestLatLon;
	}

	/**
	 * Returns the northern edge latitude
	 */
	public double getNorthLatitude() {
		return northWestLatLon.getLatitude();
	}

	/**
	 * Returns the southern edge latitude
	 */
	public double getSouthLatitude() {
		return southWestLatLon.getLatitude();
	}

	/**
	 * Returns the eastern edge latitude
	 */
	public double getEastLongitude() {
		return northEastLatLon.getLongitude();
	}

	/**
	 * Returns the western edge latitude
	 */
	public double getWestLongitude() {
		return northWestLatLon.getLongitude();
	}
}
