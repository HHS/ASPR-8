package nucleus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = DataManager.class)
public class AT_DataManager {
	@Test
	@UnitTestMethod(name = "init", args = {DataManagerContext.class})
	public void testInit() {
		DataManager dataManager = new DataManager();
		assertFalse(dataManager.isInitialized());
		dataManager.init(null);
		assertTrue(dataManager.isInitialized());
	}
}
