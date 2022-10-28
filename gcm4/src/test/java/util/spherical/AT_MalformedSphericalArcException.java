package util.spherical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = MalformedSphericalArcException.class)
public class AT_MalformedSphericalArcException {
	@Test
	@UnitTestConstructor(args = {}, tags = { UnitTag.EMPTY })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestConstructor(args = { String.class })
	public void testConstructor_String() {
		String details = "details";
		MalformedSphericalArcException malformedSphericalArcException = new MalformedSphericalArcException(details);
		assertEquals(details, malformedSphericalArcException.getMessage());

		malformedSphericalArcException = new MalformedSphericalArcException(null);
		assertNull(malformedSphericalArcException.getMessage());

	}
}
