package nucleus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;


public class AT_DataManager {
	
	
	
	@Test
	@UnitTestMethod(target = DataManager.class ,name = "init", args = {DataManagerContext.class})
	public void testInit() {
		DataManager dataManager = new DataManager();
		assertFalse(dataManager.isInitialized());
		dataManager.init(null);
		assertTrue(dataManager.isInitialized());
	}
	
	@UnitTestConstructor(target = DataManager.class ,args = {})
	@Test
	public void testConstructor() {
		//nothing to test
	}
}
