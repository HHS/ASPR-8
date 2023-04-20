package util.spherical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;

public class AT_MalformedSphericalPolygonException {
	@Test
	@UnitTestConstructor(target = MalformedSphericalPolygonException.class, args = {})
	public void testConstructor() {
		MalformedSphericalPolygonException malformedSphericalPolygonException = new MalformedSphericalPolygonException(null);
		assertNull(malformedSphericalPolygonException.getMessage());
	}

	@Test
	@UnitTestConstructor(target = MalformedSphericalPolygonException.class, args = { String.class })
	public void testConstructor_String() {
		String details = "details";
		MalformedSphericalPolygonException malformedSphericalPolygonException = new MalformedSphericalPolygonException(details);
		assertEquals(details, malformedSphericalPolygonException.getMessage());

		malformedSphericalPolygonException = new MalformedSphericalPolygonException(null);
		assertNull(malformedSphericalPolygonException.getMessage());

	}
}
