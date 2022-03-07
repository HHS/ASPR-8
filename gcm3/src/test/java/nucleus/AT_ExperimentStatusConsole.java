package nucleus;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;

@UnitTest(target = ExperimentStatusConsole.class)
public class AT_ExperimentStatusConsole {

	
	@Test
	public void testInit() {
		/*
		 * Rather than redirecting System.out, we leave this as a manual test 
		 */
	}
}
