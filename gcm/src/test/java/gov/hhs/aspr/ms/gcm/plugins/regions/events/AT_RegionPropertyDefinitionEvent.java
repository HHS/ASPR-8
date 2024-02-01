package gov.hhs.aspr.ms.gcm.plugins.regions.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionPropertyId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_RegionPropertyDefinitionEvent {

	@Test
	@UnitTestConstructor(target = RegionPropertyDefinitionEvent.class, args = { RegionPropertyId.class })
	public void testConstructor() {

		// precondition: region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> new RegionPropertyDefinitionEvent(null));
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = RegionPropertyDefinitionEvent.class, name = "regionPropertyId", args = {})
	public void testRegionPropertyId() {
		// nothing to test
	}

}
