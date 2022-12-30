package util.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;

@UnitTest(target = NonNormalVectorException.class)
public class AT_NonNormalVectorException {
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		NonNormalVectorException nonNormalVectorException = new NonNormalVectorException(null);
		assertNull(nonNormalVectorException.getMessage());
	}

	@Test
	@UnitTestConstructor(args = { String.class })
	public void testConstructor_String() {
		String details = "details";
		NonNormalVectorException nonNormalVectorException = new NonNormalVectorException(details);
		assertEquals(details, nonNormalVectorException.getMessage());
		
		nonNormalVectorException = new NonNormalVectorException(null);
		assertNull(nonNormalVectorException.getMessage());		
	}
}
