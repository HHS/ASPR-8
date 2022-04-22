package nucleus;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;

@UnitTest(target = ExperimentStateManager.class, proxy = {ExperimentStateManager.class,ExperimentContext.class })

public class AT_ExperimentStateManager {

	@Test
	public void test() {
		/*
		 * All tests of the ExperimentStateManager are covered by the Experiment
		 * and ExperimentContext tests
		 */		
	}
}
