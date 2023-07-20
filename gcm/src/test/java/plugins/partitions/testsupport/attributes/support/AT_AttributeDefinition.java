package plugins.partitions.testsupport.attributes.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public final class AT_AttributeDefinition {

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(AttributeDefinition.builder());
	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.Builder.class, name = "build", args = {})
	public void testBuild() {
		//precondition tests
		
		//if the class type of the definition is not assigned or null
		ContractException contractException = assertThrows(ContractException.class,()-> AttributeDefinition.builder().setDefaultValue(12).setType(null).build());
		assertEquals(AttributeError.NULL_ATTRIBUTE_TYPE, contractException.getErrorType());
		
		//if the default value null
		contractException = assertThrows(ContractException.class,()-> AttributeDefinition.builder().setDefaultValue(null).setType(Integer.class).build());
		assertEquals(AttributeError.NULL_DEFAULT_VALUE, contractException.getErrorType());

		//if the class type is not a super-type of the default value
		contractException = assertThrows(ContractException.class,()-> AttributeDefinition.builder().setDefaultValue("bad value").setType(Integer.class).build());
		assertEquals(AttributeError.INCOMPATIBLE_DEFAULT_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.Builder.class, name = "setType", args = { Class.class })
	public void testSetType() {
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		assertEquals(Integer.class, attributeDefinition.getType());
		
		attributeDefinition = AttributeDefinition.builder().setDefaultValue("value").setType(String.class).build();
		assertEquals(String.class, attributeDefinition.getType());
	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.Builder.class, name = "setDefaultValue", args = { Object.class })
	public void testSetDefaultValue() {
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		assertEquals(12, attributeDefinition.getDefaultValue());
		
		attributeDefinition = AttributeDefinition.builder().setDefaultValue(13).setType(Integer.class).build();
		assertEquals(13, attributeDefinition.getDefaultValue());

	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "getDefaultValue", args = {})
	public void testGetDefaultValue() {
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		assertEquals(12, attributeDefinition.getDefaultValue());
		
		attributeDefinition = AttributeDefinition.builder().setDefaultValue(13).setType(Integer.class).build();
		assertEquals(13, attributeDefinition.getDefaultValue());
	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "getType", args = {})
	public void testGetType() {
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		assertEquals(Integer.class, attributeDefinition.getType());
		
		attributeDefinition = AttributeDefinition.builder().setDefaultValue("value").setType(String.class).build();
		assertEquals(String.class, attributeDefinition.getType());

	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "hashCode", args = {})
	public void testHashCode() {
		

		//equal objects have equal hash codes
		AttributeDefinition attributeDefinition1 = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		AttributeDefinition attributeDefinition2 = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		
		assertEquals(attributeDefinition1.hashCode(), attributeDefinition2.hashCode());
	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "equals", args = { Object.class })
	public void testEquals() {
		AttributeDefinition attributeDefinition1 = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		AttributeDefinition attributeDefinition2 = AttributeDefinition.builder().setDefaultValue(13).setType(Integer.class).build();
		AttributeDefinition attributeDefinition3 = AttributeDefinition.builder().setDefaultValue(13.0).setType(Double.class).build();
		AttributeDefinition attributeDefinition4 = AttributeDefinition.builder().setDefaultValue(13.0).setType(Double.class).build();
		AttributeDefinition attributeDefinition5 = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		
		assertEquals(attributeDefinition1, attributeDefinition1);
		assertEquals(attributeDefinition2, attributeDefinition2);
		assertEquals(attributeDefinition3, attributeDefinition3);
		assertEquals(attributeDefinition4, attributeDefinition4);
		assertEquals(attributeDefinition5, attributeDefinition5);
		
		assertEquals(attributeDefinition1, attributeDefinition5);
		
		assertNotEquals(attributeDefinition1, attributeDefinition2);
		assertNotEquals(attributeDefinition2, attributeDefinition3);
	}

	@Test
	@UnitTestMethod(target = AttributeDefinition.class,name = "toString", args = {})
	public void testToString() {
		AttributeDefinition attributeDefinition = AttributeDefinition.builder().setDefaultValue(12).setType(Integer.class).build();
		String expectedValue = "AttributeDefinition [type=class java.lang.Integer, defaultValue=12]";
		assertEquals(expectedValue, attributeDefinition.toString());
	}

}