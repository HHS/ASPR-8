package util.spherical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestConstructor;

public class AT_MalformedSphericalPointException {
	@Test
	@UnitTestConstructor(target = MalformedSphericalPointException.class, args = {})
	public void testConstructor() {
		MalformedSphericalPointException malformedSphericalPointException = new MalformedSphericalPointException(null);
		assertNull(malformedSphericalPointException.getMessage());
	}

	@Test
	@UnitTestConstructor(target = MalformedSphericalPointException.class, args = { String.class })
	public void testConstructor_String() {
		String details = "details";
		MalformedSphericalPointException malformedSphericalPointException = new MalformedSphericalPointException(details);
		assertEquals(details, malformedSphericalPointException.getMessage());

		malformedSphericalPointException = new MalformedSphericalPointException(null);
		assertNull(malformedSphericalPointException.getMessage());

	}
}
