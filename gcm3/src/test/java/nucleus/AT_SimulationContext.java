package nucleus;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;

@UnitTest(target = SimulationContext.class)
public class AT_SimulationContext {

	@Test
	public void test() {
		/*
		 * Nothing to test. SimulationContext implemented by the simulation as
		 * either a DataManagerContext or an ActorContext
		 */
	}
}
