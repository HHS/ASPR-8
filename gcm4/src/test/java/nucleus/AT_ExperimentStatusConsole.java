package nucleus;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = ExperimentStatusConsole.class)
public class AT_ExperimentStatusConsole {

	
	@Test
	@UnitTestMethod(name = "init", args = {ExperimentContext.class}, manual = true)
	public void testInit() {
		/*
		 * Rather than redirecting System.out, we leave this as a manual test 
		 */
	}
	
	@Test
	@UnitTestConstructor( args = {})
	public void testConstructor() {
		//nothing to test
	}
}
