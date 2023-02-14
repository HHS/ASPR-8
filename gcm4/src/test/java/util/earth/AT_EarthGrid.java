package util.earth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestField;
import util.annotations.UnitTestMethod;
import util.vector.Vector2D;

public class AT_EarthGrid {

	@Test
	@UnitTestField(target = EarthGrid.class,name = "MIN_ANGLE_FROM_POLE")
	public void testMinAngleFromPole() {
		assertEquals(0.001, EarthGrid.MIN_ANGLE_FROM_POLE, 0);
	}

	@Test
	@UnitTestConstructor(target = EarthGrid.class, args = { LatLon.class, double.class })
	public void testConstructor() {
		// precondition test: if the latitude is too close to the poles
		assertThrows(IllegalArgumentException.class, () -> new EarthGrid(new LatLon(89.9995, 128), 45.0));
		assertThrows(IllegalArgumentException.class, () -> new EarthGrid(new LatLon(-89.9995, 128), 45.0));
	}

	@Test
	@UnitTestMethod(target = EarthGrid.class, name = "getCartesian2DCoordinate", args = { LatLon.class }, tags = { UnitTag.MANUAL })
	public void testGetCartesian2DCoordinate() {
		// requires manual, inspection based testing
	}

	@Test
	@UnitTestMethod(target = EarthGrid.class, name = "getLatLon", args = { Vector2D.class }, tags = { UnitTag.MANUAL })
	public void getLatLon() {
		// requires manual, inspection based testing
	}

}
