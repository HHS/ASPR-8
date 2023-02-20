package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;

public class AT_TimeTrackingPolicy {

	@Test
	@UnitTestMethod(target = TimeTrackingPolicy.class, name="next", args= {})
	public void testNext() {
		fail();
		
	}

	@Test
	@UnitTestMethod(target = TimeTrackingPolicy.class, name="getRandomTimeTrackingPolicy", args= {RandomGenerator.class})

	public void testGetRandomTimeTrackingPolicy() {
		fail();
	}
	
}
