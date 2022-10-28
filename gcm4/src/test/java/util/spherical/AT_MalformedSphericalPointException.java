package util.spherical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = MalformedSphericalPointException.class)
public class AT_MalformedSphericalPointException {
	@Test
	@UnitTestConstructor(args = {}, tags = { UnitTag.EMPTY })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestConstructor(args = { String.class })
	public void testConstructor_String() {
		String details = "details";
		MalformedSphericalPointException malformedSphericalPointException = new MalformedSphericalPointException(details);
		assertEquals(details, malformedSphericalPointException.getMessage());

		malformedSphericalPointException = new MalformedSphericalPointException(null);
		assertNull(malformedSphericalPointException.getMessage());

	}
}
