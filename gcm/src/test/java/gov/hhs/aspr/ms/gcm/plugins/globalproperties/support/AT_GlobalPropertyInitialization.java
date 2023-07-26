package gov.hhs.aspr.ms.gcm.plugins.globalproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_GlobalPropertyInitialization {

	@Test
	@UnitTestMethod(target = GlobalPropertyInitialization.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(GlobalPropertyInitialization.builder());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyInitialization.Builder.class, name = "build", args = {})
	public void testBuild() {

		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();

		assertNotNull(GlobalPropertyInitialization	.builder()//
													.setValue(5)//
													.setGlobalPropertyId(new SimpleGlobalPropertyId("firstTestId"))//
													.setPropertyDefinition(propertyDefinition).build());

		// precondition test: if property id is not set
		ContractException idContractException = assertThrows(ContractException.class, () -> {
			GlobalPropertyInitialization.builder()//
										.setPropertyDefinition(propertyDefinition)//
										.setValue(5)//
										.build();
		});

		assertEquals(PropertyError.NULL_PROPERTY_ID, idContractException.getErrorType());

		// precondition test: if property definition is not set
		ContractException propertyContractException = assertThrows(ContractException.class, () -> {
			GlobalPropertyInitialization.builder()//
										.setGlobalPropertyId(new SimpleGlobalPropertyId("secondTestId"))//
										.setValue(6).build();
		});
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, propertyContractException.getErrorType());

		// precondition test: if the property value is not set
		ContractException valueContractException = assertThrows(ContractException.class, () -> {
			GlobalPropertyInitialization.builder()//
										.setGlobalPropertyId(new SimpleGlobalPropertyId("thirdTestId"))//
										.setPropertyDefinition(propertyDefinition)//
										.build();
		});

		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, valueContractException.getErrorType());

		// precondition test: if the property definition and value types are
		// incompatible
		ContractException incompatibleContractException = assertThrows(ContractException.class, () -> {
			GlobalPropertyInitialization.builder()//
										.setGlobalPropertyId(new SimpleGlobalPropertyId("fourthTestId"))//
										.setPropertyDefinition(propertyDefinition)//
										.setValue(15.5f).build();
		});

		assertEquals(PropertyError.INCOMPATIBLE_VALUE, incompatibleContractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyInitialization.Builder.class, name = "setGlobalPropertyId", args = { GlobalPropertyId.class })
	public void testSetGlobalPropertyId() {
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();

		// precondition test: if the property id is null
		ContractException idContractException = assertThrows(ContractException.class,
				() -> GlobalPropertyInitialization.builder().setPropertyDefinition(propertyDefinition).setValue(5).setGlobalPropertyId(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, idContractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyInitialization.Builder.class, name = "setPropertyDefinition", args = { PropertyDefinition.class })
	public void testSetPropertyDefinition() {
		// precondition test: if the property definition is null
		ContractException propertyContractException = assertThrows(ContractException.class,
				() -> GlobalPropertyInitialization.builder().setGlobalPropertyId(new SimpleGlobalPropertyId("fifthTestId")).setValue(5).setPropertyDefinition(null));
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, propertyContractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyInitialization.Builder.class, name = "setValue", args = { Object.class })
	public void testSetValue() {
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();

		// precondition test: if the property value is null
		ContractException valueContractException = assertThrows(ContractException.class,
				() -> GlobalPropertyInitialization.builder().setGlobalPropertyId(new SimpleGlobalPropertyId("sixthTestId")).setPropertyDefinition(propertyDefinition).setValue(null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, valueContractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyInitialization.class, name = "getGlobalPropertyId", args = {})
	public void testGetGlobalPropertyId() {
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		GlobalPropertyInitialization.Builder builder = GlobalPropertyInitialization.builder();
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("seventhTestId");
		Integer value = 6;

		builder.setGlobalPropertyId(globalPropertyId).setPropertyDefinition(propertyDefinition).setValue(value);
		GlobalPropertyInitialization globalPropertyInitialization = builder.build();

		assertNotNull(globalPropertyInitialization.getGlobalPropertyId());
		assertEquals(globalPropertyId, globalPropertyInitialization.getGlobalPropertyId());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyInitialization.class, name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		GlobalPropertyInitialization.Builder builder = GlobalPropertyInitialization.builder();
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("eighthTestId");
		Integer value = 6;

		builder.setGlobalPropertyId(globalPropertyId).setPropertyDefinition(propertyDefinition).setValue(value);
		GlobalPropertyInitialization globalPropertyInitialization = builder.build();

		assertNotNull(globalPropertyInitialization.getPropertyDefinition());
		assertEquals(propertyDefinition, globalPropertyInitialization.getPropertyDefinition());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyInitialization.class, name = "getValue", args = {})
	public void testGetValue() {
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
		GlobalPropertyInitialization.Builder builder = GlobalPropertyInitialization.builder();
		GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("eighthTestId");
		Integer value = 6;

		builder.setGlobalPropertyId(globalPropertyId).setPropertyDefinition(propertyDefinition).setValue(value);
		GlobalPropertyInitialization globalPropertyInitialization = builder.build();

		assertNotNull(globalPropertyInitialization.getValue());
		assertEquals(value, globalPropertyInitialization.getValue().get());
	}
}