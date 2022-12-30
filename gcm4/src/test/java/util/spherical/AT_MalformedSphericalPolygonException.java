package util.spherical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = MalformedSphericalPolygonException.class)
public class AT_MalformedSphericalPolygonException {
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		MalformedSphericalPolygonException malformedSphericalPolygonException = new MalformedSphericalPolygonException(null);
		assertNull(malformedSphericalPolygonException.getMessage());
	}

	@Test
	@UnitTestConstructor(args = { String.class })
	public void testConstructor_String() {
		String details = "details";
		MalformedSphericalPolygonException malformedSphericalPolygonException = new MalformedSphericalPolygonException(details);
		assertEquals(details, malformedSphericalPolygonException.getMessage());

		malformedSphericalPolygonException = new MalformedSphericalPolygonException(null);
		assertNull(malformedSphericalPolygonException.getMessage());

	}
}
