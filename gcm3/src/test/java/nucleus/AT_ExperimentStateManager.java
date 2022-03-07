package nucleus;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;

@UnitTest(target = ExperimentStateManager.class)
@Disabled
public class AT_ExperimentStateManager {

	@Test
	public void test() {
		/*
		 * All tests of the ExperimentStateManager are covered by the Experiment
		 * and ExperimentContext tests
		 */
		fail();
	}
}
