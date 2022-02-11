package plugins.people.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PopulationGrowthProjectionEvent.class)
public final class AT_PopulationGrowthProjectionEvent implements Event {

	@Test
	@UnitTestConstructor(args = { int.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getCount", args = {})
	public void testGetCount() {
		for (int i = 0; i <10; i++) {
			PopulationGrowthProjectionEvent populationGrowthProjectionEvent = new PopulationGrowthProjectionEvent(i);
			assertEquals(i, populationGrowthProjectionEvent.getCount());
		}
	}

}
