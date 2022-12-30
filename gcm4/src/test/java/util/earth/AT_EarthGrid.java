package util.earth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestField;
import tools.annotations.UnitTestMethod;
import util.vector.Vector2D;

@UnitTest(target = EarthGrid.class)
public class AT_EarthGrid {
	
	
	@Test
	@UnitTestField(name = "MIN_ANGLE_FROM_POLE")
	public void testMinAngleFromPole() {
		assertEquals(0.001,	EarthGrid.MIN_ANGLE_FROM_POLE, 0);
	}
	

	@Test
	@UnitTestConstructor(args = { LatLon.class, double.class })
	public void testConstructor() {
		// precondition test: if the latitude is too close to the poles
		assertThrows(IllegalArgumentException.class, () -> new EarthGrid(new LatLon(89.9995, 128), 45.0));
		assertThrows(IllegalArgumentException.class, () -> new EarthGrid(new LatLon(-89.9995, 128), 45.0));
	}

	@Test
	@UnitTestMethod(name = "getCartesian2DCoordinate", args = { LatLon.class }, tags = { UnitTag.MANUAL })
	public void testGetCartesian2DCoordinate() {
		// requires manual, inspection based testing
	}

	@Test
	@UnitTestMethod(name = "getLatLon", args = { Vector2D.class }, tags= {UnitTag.MANUAL})
	public void getLatLon() {
		//requires manual, inspection based testing 
	}

}
