package plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = TestAttributeId.class)
public class AT_TestAttributeId {

	@Test
	@UnitTestMethod(name = "getUnknownAttributeId", args = {})
	public void testGetUnknownAttributeId() {
		/*
		 * Show that a generated unknown attribute id is not null and not a
		 * member of the enum
		 */
		Set<AttributeId> attributeIds = new LinkedHashSet<>();
		for (int i = 0; i < 30; i++) {
			AttributeId unknownAttributeId = TestAttributeId.getUnknownAttributeId();
			assertNotNull(unknownAttributeId);
			boolean unique = attributeIds.add(unknownAttributeId);
			assertTrue(unique);
			for (TestAttributeId testAttributeId : TestAttributeId.values()) {
				assertNotEquals(testAttributeId, unknownAttributeId);
			}
		}
	}

	@Test
	@UnitTestMethod(name = "getAttributeDefinition", args = {})
	public void testGetAttributeDefinition() {
		// show that each member has an attribute definition
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertNotNull(testAttributeId.getAttributeDefinition());
		}
	}

	public void testGetRandomPropertyValue() {
		
	}

}
