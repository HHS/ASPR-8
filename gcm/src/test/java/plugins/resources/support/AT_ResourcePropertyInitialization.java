package plugins.resources.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_ResourcePropertyInitialization {

	@Test
	@UnitTestMethod(target = ResourcePropertyInitialization.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(ResourcePropertyInitialization.builder());
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyInitialization.Builder.class, name = "build", args = {})
	public void testBuild() {
		ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;

		builder.setPropertyDefinition(propertyDefinition);
		builder.setValue(1);
		builder.setResourceId(testResourceId);
		builder.setResourcePropertyId(testResourcePropertyId);
		ResourcePropertyInitialization resourcePropertyInitialization = builder.build();
		assertNotNull(resourcePropertyInitialization);
		assertEquals(resourcePropertyInitialization.getValue().get(), 1);
		assertEquals(resourcePropertyInitialization.getResourcePropertyId(), testResourcePropertyId);
		assertEquals(resourcePropertyInitialization.getResourceId(), testResourceId);
		assertEquals(resourcePropertyInitialization.getPropertyDefinition(), propertyDefinition);

		// precondition test: if property definition is not set

		ContractException definitionContractException = assertThrows(ContractException.class, () -> {
			ResourcePropertyInitialization	.builder()//
											.setResourceId(testResourceId)//
											.setValue(2)//
											.setResourcePropertyId(testResourcePropertyId)//
											.build();//
		});
		assertEquals(definitionContractException.getErrorType(), PropertyError.NULL_PROPERTY_DEFINITION);

		// precondition test: if property id is not set
		ContractException propertyIdContractException = assertThrows(ContractException.class, () -> {
			ResourcePropertyInitialization	.builder()//
											.setPropertyDefinition(propertyDefinition)//
											.setValue(3)//
											.setResourceId(testResourceId)//
											.build();//
		});
		assertEquals(propertyIdContractException.getErrorType(), PropertyError.NULL_PROPERTY_ID);

		// precondition test: if resource id is not set

		ContractException resourceIdContractException = assertThrows(ContractException.class, () -> {
			ResourcePropertyInitialization	.builder()//
											.setPropertyDefinition(propertyDefinition)//
											.setValue(4)//
											.setResourcePropertyId(testResourcePropertyId)//
											.build();//
		});
		assertEquals(resourceIdContractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);

		// precondition test: if the value is not set

		ContractException insufficientValueContractException = assertThrows(ContractException.class, () -> {
			ResourcePropertyInitialization	.builder()//
											.setPropertyDefinition(propertyDefinition)//
											.setResourcePropertyId(testResourcePropertyId)//
											.setResourceId(testResourceId)//
											.build();
		});
		assertEquals(insufficientValueContractException.getErrorType(), PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);

		// precondition test: if the value in incompatible with the property
		// definition

		ContractException incompatibleContractException = assertThrows(ContractException.class, () -> {
			ResourcePropertyInitialization	.builder()//
											.setPropertyDefinition(propertyDefinition)//
											.setValue("this is a string")//
											.setResourcePropertyId(testResourcePropertyId)//
											.setResourceId(testResourceId)//
											.build();
		});
		assertEquals(incompatibleContractException.getErrorType(), PropertyError.INCOMPATIBLE_VALUE);
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyInitialization.Builder.class, name = "setResourceId", args = { ResourceId.class })
	public void testSetResourceId() {
		ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;

		// precondition test: if resource id is null
		ContractException resourceIdContractException = assertThrows(ContractException.class,
				() -> builder.setPropertyDefinition(propertyDefinition).setValue(6).setResourcePropertyId(testResourcePropertyId).setResourceId(null));
		assertEquals(resourceIdContractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);

		ResourcePropertyInitialization resourcePropertyInitialization = builder	.setPropertyDefinition(propertyDefinition).setValue(6).setPropertyDefinition(propertyDefinition)
																				.setResourcePropertyId(testResourcePropertyId).setResourceId(testResourceId).build();

		assertEquals(resourcePropertyInitialization.getResourceId(), testResourceId);
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyInitialization.Builder.class, name = "setResourcePropertyId", args = { ResourcePropertyId.class })
	public void testSetResourcePropertyId() {
		ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;

		// precondition test: if resource property id is null
		ContractException resourceIdContractException = assertThrows(ContractException.class,
				() -> builder.setPropertyDefinition(propertyDefinition).setValue(7).setResourceId(testResourceId).setResourcePropertyId(null));
		assertEquals(resourceIdContractException.getErrorType(), PropertyError.NULL_PROPERTY_ID);

		ResourcePropertyInitialization resourcePropertyInitialization = builder	.setPropertyDefinition(propertyDefinition).setValue(6).setPropertyDefinition(propertyDefinition)
																				.setResourcePropertyId(testResourcePropertyId).setResourceId(testResourceId).build();

		assertEquals(resourcePropertyInitialization.getResourcePropertyId(), testResourcePropertyId);
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyInitialization.Builder.class, name = "setPropertyDefinition", args = { PropertyDefinition.class })
	public void testSetPropertyDefinition() {
		ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;

		// precondition test: if the property definition is null
		ContractException resourceIdContractException = assertThrows(ContractException.class,
				() -> builder.setValue(8).setResourceId(testResourceId).setResourcePropertyId(testResourcePropertyId).setPropertyDefinition(null));
		assertEquals(resourceIdContractException.getErrorType(), PropertyError.NULL_PROPERTY_DEFINITION);

		ResourcePropertyInitialization resourcePropertyInitialization = builder	.setPropertyDefinition(propertyDefinition).setValue(6).setPropertyDefinition(propertyDefinition)
																				.setResourcePropertyId(testResourcePropertyId).setResourceId(testResourceId).build();

		assertEquals(resourcePropertyInitialization.getPropertyDefinition(), propertyDefinition);
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyInitialization.Builder.class, name = "setValue", args = { Object.class })
	public void testSetValue() {
		ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;

		// precondition test: if the value is null
		ContractException resourceIdContractException = assertThrows(ContractException.class,
				() -> builder.setResourceId(testResourceId).setResourcePropertyId(testResourcePropertyId).setPropertyDefinition(propertyDefinition).setValue(null));
		assertEquals(resourceIdContractException.getErrorType(), PropertyError.NULL_PROPERTY_VALUE);

		ResourcePropertyInitialization resourcePropertyInitialization = builder	.setPropertyDefinition(propertyDefinition).setValue(6).setPropertyDefinition(propertyDefinition)
																				.setResourcePropertyId(testResourcePropertyId).setResourceId(testResourceId).build();

		assertEquals(resourcePropertyInitialization.getValue().get(), 6);
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyInitialization.class, name = "getResourcePropertyId", args = {})
	public void testGetResourcePropertyId() {
		ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;

		builder.setPropertyDefinition(propertyDefinition);
		builder.setResourceId(testResourceId);
		builder.setResourcePropertyId(testResourcePropertyId);
		builder.setValue(9);
		ResourcePropertyInitialization resourcePropertyInitialization = builder.build();

		assertNotNull(resourcePropertyInitialization.getResourcePropertyId());
		assertEquals(resourcePropertyInitialization.getResourcePropertyId(), testResourcePropertyId);
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyInitialization.class, name = "getResourceId", args = {})
	public void testGetResourceId() {
		ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;

		builder.setPropertyDefinition(propertyDefinition);
		builder.setResourceId(testResourceId);
		builder.setResourcePropertyId(testResourcePropertyId);
		builder.setValue(10);
		ResourcePropertyInitialization resourcePropertyInitialization = builder.build();

		assertNotNull(resourcePropertyInitialization.getResourceId());
		assertEquals(resourcePropertyInitialization.getResourceId(), testResourceId);
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyInitialization.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;

		builder.setPropertyDefinition(propertyDefinition);
		builder.setResourceId(testResourceId);
		builder.setResourcePropertyId(testResourcePropertyId);
		builder.setValue(11);
		ResourcePropertyInitialization resourcePropertyInitialization = builder.build();

		assertNotNull(resourcePropertyInitialization.getPropertyDefinition());
		assertEquals(resourcePropertyInitialization.getPropertyDefinition(), propertyDefinition);
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyInitialization.class, name = "getValue", args = {})
	public void testGetValue() {
		ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;

		builder.setPropertyDefinition(propertyDefinition);
		builder.setResourceId(testResourceId);
		builder.setResourcePropertyId(testResourcePropertyId);
		builder.setValue(12);
		ResourcePropertyInitialization resourcePropertyInitialization = builder.build();

		assertNotNull(resourcePropertyInitialization.getValue());
		assertEquals(resourcePropertyInitialization.getValue().get(), 12);
	}

}