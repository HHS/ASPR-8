package util.spherical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = MalformedSphericalTriangleException.class)
public class AT_MalformedSphericalTriangleException {
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		MalformedSphericalTriangleException malformedSphericalTriangleException = new MalformedSphericalTriangleException(null);
		assertNull(malformedSphericalTriangleException.getMessage());
	}

	@Test
	@UnitTestConstructor(args = { String.class })
	public void testConstructor_String() {
		String details = "details";
		MalformedSphericalTriangleException malformedSphericalTriangleException = new MalformedSphericalTriangleException(details);
		assertEquals(details, malformedSphericalTriangleException.getMessage());

		malformedSphericalTriangleException = new MalformedSphericalTriangleException(null);
		assertNull(malformedSphericalTriangleException.getMessage());

	}
}
