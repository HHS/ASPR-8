package util.earth;

import net.jcip.annotations.Immutable;

/**
 * An immutable container class for a pair of latitude/longitude values measured
 * in degrees where latitude is in the interval [-90,90] and longitude is in the
 * interval [-180,180]
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class LatLon {

	private final double latitude;

	private final double longitude;

	/**
	 * Returns the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Returns the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Constructs a LatLon from the given latitude and longitude
	 * 
	 * @throws RuntimeException
	 *             <li>if the latitude is not in the interval [-90,90]
	 *             <li>if the longitude is not in the interval [-180,180]
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public LatLon(double latitude, double longitude) {
		if (latitude > 90) {
			throw new RuntimeException("Latitude > 90 degrees");
		}
		if (latitude < -90) {
			throw new RuntimeException("Latitude < -90 degrees");
		}
		if (longitude > 180) {
			throw new RuntimeException("Longitude > 180 degrees");
		}
		if (longitude < -180) {
			throw new RuntimeException("Longitude < -180 degrees");
		}
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * Constructs a LatLon from the given LatLonAlt
	 * 
	 * @throws RuntimeException
	 *             <li>if the latLonAlt is null
	 * @param latLonAlt
	 */
	public LatLon(LatLonAlt latLonAlt) {
		if (latLonAlt == null) {
			throw new RuntimeException("null latLonAlt");
		}
		this.latitude = latLonAlt.getLatitude();
		this.longitude = latLonAlt.getLongitude();
	}

	/**
	 * Returns a string of the form LatLonAlt [latitude=35.0, longitude=128.0]
	 */
	@Override
	public String toString() {
		return "LatLon [latitude=" + latitude + ", longitude=" + longitude + "]";
	}

	/**
	 * Returns a hash code consistent with equals()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Returns true if and only if this LatLon is compared to another non-null
	 * instance of LatLon with matching latitude and longitude values
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LatLon other = (LatLon) obj;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return false;
		return true;
	}

}
