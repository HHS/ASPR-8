package plugins.partitions.testsupport.attributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import plugins.partitions.testsupport.attributes.support.AttributeDefinition;
import plugins.partitions.testsupport.attributes.support.AttributeError;
import plugins.partitions.testsupport.attributes.support.AttributeId;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = AttributesPluginData.class)
public class AT_AttributesPluginData {

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(AttributesPluginData.builder());
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		assertNotNull(AttributesPluginData.builder().build());
	}

	@Test
	@UnitTestMethod(target = AttributesPluginData.Builder.class, name = "defineAttribute", args = { AttributeId.class, AttributeDefinition.class })
	public void testDefineAttribute() {
		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			builder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributesPluginData attributesPluginData = builder.build();
		assertEquals(EnumSet.allOf(TestAttributeId.class), attributesPluginData.getAttributeIds());
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertEquals(testAttributeId.getAttributeDefinition(), attributesPluginData.getAttributeDefinition(testAttributeId));
		}

		// precondition tests

		// if the attribute id is null
		ContractException contractException = assertThrows(ContractException.class, () -> builder.defineAttribute(null, TestAttributeId.BOOLEAN_0.getAttributeDefinition()));
		assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

		// if the attribute definition is null
		contractException = assertThrows(ContractException.class, () -> builder.defineAttribute(TestAttributeId.BOOLEAN_0, null));
		assertEquals(AttributeError.NULL_ATTRIBUTE_DEFINITION, contractException.getErrorType());

		// if the attribute id was previously added
		builder.defineAttribute(TestAttributeId.BOOLEAN_0, TestAttributeId.BOOLEAN_0.getAttributeDefinition());
		contractException = assertThrows(ContractException.class, () -> builder.defineAttribute(TestAttributeId.BOOLEAN_0, TestAttributeId.BOOLEAN_0.getAttributeDefinition()));
		assertEquals(AttributeError.DUPLICATE_ATTRIBUTE_DEFINITION, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "getAttributeDefinition", args = { AttributeId.class })
	public void testGetAttributeDefinition() {
		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			builder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributesPluginData attributesPluginData = builder.build();
		assertEquals(EnumSet.allOf(TestAttributeId.class), attributesPluginData.getAttributeIds());
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			assertEquals(testAttributeId.getAttributeDefinition(), attributesPluginData.getAttributeDefinition(testAttributeId));
		}

		//precondition tests
		
		// if the attribute id is null
		ContractException contractException = assertThrows(ContractException.class, () -> attributesPluginData.getAttributeDefinition(null));
		assertEquals(AttributeError.NULL_ATTRIBUTE_ID, contractException.getErrorType());

		// if the attribute id is unknown</li>
		contractException = assertThrows(ContractException.class, () -> attributesPluginData.getAttributeDefinition(TestAttributeId.getUnknownAttributeId()));
		assertEquals(AttributeError.UNKNOWN_ATTRIBUTE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(name = "getAttributeIds", args = {})
	public void testGetAttributeIds() {
		AttributesPluginData.Builder builder = AttributesPluginData.builder();
		for (TestAttributeId testAttributeId : TestAttributeId.values()) {
			builder.defineAttribute(testAttributeId, testAttributeId.getAttributeDefinition());
		}
		AttributesPluginData attributesPluginData = builder.build();
		assertEquals(EnumSet.allOf(TestAttributeId.class), attributesPluginData.getAttributeIds());
	}

	public void testGetCloneBuilder() {
		
	}
}
