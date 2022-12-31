package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

public class AT_MaterialIdAdditionEvent {

	@Test
	@UnitTestConstructor(target = MaterialIdAdditionEvent.class, args = { MaterialId.class })
	public void testConstructor() {

		// precondition: null material id
		ContractException contractException = assertThrows(ContractException.class, () -> new MaterialIdAdditionEvent(null));
		assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
	}
}
