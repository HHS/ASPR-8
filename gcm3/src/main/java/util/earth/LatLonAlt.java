package util.earth;

import util.vector.Vector3D;

public class LatLonAlt {

	private final double latitude;

	private final double longitude;

	private final double altitude;

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
	 * Constructs a LatLonAlt from the given latitude and longitude
	 * 
	 * @throws RuntimeException
	 *             <li>if the latitude is not in the interval [-90,90]
	 *             <li>if the longitude is not in the interval [-180,180]
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public LatLonAlt(double latitude, double longitude, double altitude) {
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
		this.altitude = altitude;
	}

	/**
	 * Constructs a LatLonAlt from the given Vector3D where latitude = x,
	 * longitude = y and altitude = z
	 * 
	 * @throws RuntimeException
	 *             <li>if the vector3d is null
	 *             <li>if the latitude(v.getX()) is not in the interval [-90,90]
	 *             <li>if the longitude(v.getY()) is not in the interval
	 *             [-180,180]
	 * @param latLonAlt
	 */
	public LatLonAlt(Vector3D vector3d) {
		if (vector3d == null) {
			throw new RuntimeException("null Vector3D value");
		}

		this.latitude = vector3d.getX();
		this.longitude = vector3d.getY();
		this.altitude = vector3d.getZ();

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

	}

	/**
	 * Constructs a LatLonAlt from the given LatLon with altitude set to zero
	 * 
	 * @throws RuntimeException
	 *             <li>if the latLon is null
	 * @param latLonAlt
	 */

	public LatLonAlt(LatLon latLon) {
		if (latLon == null) {
			throw new RuntimeException("null LatLon value");
		}
		this.latitude = latLon.getLatitude();
		this.longitude = latLon.getLongitude();
		this.altitude = 0;

	}

	/**
	 * Returns the altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * Returns a Vector3D from this LatLonAlt where x = latitude, y = longitude
	 * and z = altitude.
	 */
	public Vector3D toVector3D() {
		return new Vector3D(latitude, longitude, altitude);
	}

	/**
	 * Returns a hash code consistent with equals()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(altitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Returns true if and only if this LatLonAlt is compared to another
	 * non-null instance of LatLonAlt with matching latitude, longitude and
	 * altitude values
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LatLonAlt other = (LatLonAlt) obj;
		if (Double.doubleToLongBits(altitude) != Double.doubleToLongBits(other.altitude))
			return false;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return false;
		return true;
	}

	/**
	 * Returns a string of the form
	 * 
	 * LatLonAlt [latitude=35.0, longitude=128.0, altitude=1000.0]
	 */
	@Override
	public String toString() {
		return "LatLonAlt [latitude=" + latitude + ", longitude=" + longitude + ", altitude=" + altitude + "]";
	}

}
