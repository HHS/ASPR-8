package plugins.resources.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ResourceInitialization.class)
public class AT_ResourceInitialization {

	@Test
	@UnitTestConstructor(args = { ResourceId.class, Long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		for (TestResourceId testResourceId : TestResourceId.values()) {
			ResourceInitialization resourceInitialization = new ResourceInitialization(testResourceId, 123L);
			assertEquals(testResourceId, resourceInitialization.getResourceId());
		}
	}

	@Test
	@UnitTestMethod(name = "getAmount", args = {})
	public void testGetAmount() {
		for (long value = 0; value < 10; value++) {
			ResourceInitialization resourceInitialization = new ResourceInitialization(TestResourceId.RESOURCE_3, value);
			assertEquals(value, resourceInitialization.getAmount().longValue());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		ResourceInitialization resourceInitialization = new ResourceInitialization(TestResourceId.RESOURCE_3, 15L);
		assertEquals("ResourceAssignment [resourceId=RESOURCE_3, amount=15]", resourceInitialization.toString());
	}

}
