package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_DataManager {

	@Test
	@UnitTestMethod(target = DataManager.class, name = "init", args = { DataManagerContext.class })
	public void testInit() {
		DataManager dataManager = new DataManager();
		assertFalse(dataManager.isInitialized());
		DataManagerContext dataManagerContext = new DataManagerContext(new DataManagerId(5), null);
		dataManager.init(dataManagerContext);
		assertTrue(dataManager.isInitialized());

		ContractException contractException = assertThrows(ContractException.class,
				() -> dataManager.init(dataManagerContext));
		assertEquals(NucleusError.DATA_MANAGER_DUPLICATE_INITIALIZATION, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> {
			DataManager dm = new DataManager();
			dm.init(null);
		});
		assertEquals(NucleusError.NULL_DATA_MANAGER_CONTEXT, contractException.getErrorType());

	}

	@UnitTestConstructor(target = DataManager.class, args = {})
	@Test
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = DataManager.class, name = "toString", args = {})
	public void testToString() {
		// nothing to test
	}
}
