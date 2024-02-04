package gov.hhs.aspr.ms.gcm.plugins.globalproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.SimpleGlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_GlobalPropertyDefinitionEvent {

	@Test
	@UnitTestConstructor(target = GlobalPropertyDefinitionEvent.class, args = { GlobalPropertyId.class, Object.class })
	public void testConstructor() {

		ContractException contractException = assertThrows(ContractException.class, () -> new GlobalPropertyDefinitionEvent(null, 7));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		SimpleGlobalPropertyId goodId = new SimpleGlobalPropertyId(5);

		ContractException contractException2 = assertThrows(ContractException.class, () -> new GlobalPropertyDefinitionEvent(goodId, null));
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException2.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDefinitionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDefinitionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyDefinitionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GlobalPropertyDefinitionEvent.class, name = "globalPropertyId", args = {})
	public void testGlobalPropertyId() {
		// nothing to test
	}
	
	@Test
	@UnitTestMethod(target = GlobalPropertyDefinitionEvent.class, name = "initialPropertyValue", args = {})
	public void testInitialPropertyValue() {
		// nothing to test
	}

}