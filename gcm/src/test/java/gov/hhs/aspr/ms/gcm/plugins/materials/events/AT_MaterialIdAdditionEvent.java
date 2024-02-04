package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_MaterialIdAdditionEvent {

	@Test
	@UnitTestConstructor(target = MaterialIdAdditionEvent.class, args = { MaterialId.class })
	public void testConstructor() {

		// precondition: null material id
		ContractException contractException = assertThrows(ContractException.class, () -> new MaterialIdAdditionEvent(null));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialIdAdditionEvent.class, name = "equals", args = { Object.class })
	public void testEquals() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialIdAdditionEvent.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialIdAdditionEvent.class, name = "hashCode", args = {})
	public void testHashCode() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = MaterialIdAdditionEvent.class, name = "materialId", args = {})
	public void testMaterialId() {
		// nothing to test
	}

}
