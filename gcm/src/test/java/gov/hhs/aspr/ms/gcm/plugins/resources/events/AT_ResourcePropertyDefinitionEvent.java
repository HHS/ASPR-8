package gov.hhs.aspr.ms.gcm.plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourcePropertyId;
import gov.hhs.aspr.ms.gcm.plugins.resources.testsupport.TestResourceId;
import gov.hhs.aspr.ms.gcm.plugins.resources.testsupport.TestResourcePropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_ResourcePropertyDefinitionEvent {

	@Test
	@UnitTestConstructor(target = ResourcePropertyDefinitionEvent.class, args = { ResourceId.class, ResourcePropertyId.class, Object.class })
	public void testConstructor() {
		TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
		TestResourceId testResourceId = TestResourceId.RESOURCE_1;
		Integer propertyValue = 7;

		// test case: null resource id
		ContractException contractException = assertThrows(ContractException.class, () -> new ResourcePropertyDefinitionEvent(null, testResourcePropertyId, propertyValue));
		assertEquals(contractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);

		// test case: null property id
		contractException = assertThrows(ContractException.class, () -> new ResourcePropertyDefinitionEvent(testResourceId, null, propertyValue));
		assertEquals(contractException.getErrorType(), PropertyError.NULL_PROPERTY_ID);

		// test case: null property value
		contractException = assertThrows(ContractException.class, () -> new ResourcePropertyDefinitionEvent(testResourceId, testResourcePropertyId, null));
		assertEquals(contractException.getErrorType(), PropertyError.NULL_PROPERTY_VALUE);
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyDefinitionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyDefinitionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyDefinitionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyDefinitionEvent.class, name = "resourceId", args = {})
	public void testResourceId() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyDefinitionEvent.class, name = "resourcePropertyId", args = {})
	public void testResourcePropertyId() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = ResourcePropertyDefinitionEvent.class, name = "resourcePropertyValue", args = {})
	public void testResourcePropertyValue() {
		// nothing to test
	}


}