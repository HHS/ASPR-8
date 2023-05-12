package plugins.stochastics.support;

import org.junit.jupiter.api.Test;
import util.annotations.UnitTestMethod;

import static org.junit.jupiter.api.Assertions.*;

public class AT_Well {

    @Test
    @UnitTestMethod(target = Well.class, name = "well", args = {WellState.class}    )
    public void testWell() {
        Long seed = 6559152513645047938L;
        int index = 13;
        int[] vArray = new int[1391];

        WellState wellState = WellState.builder()
                .setInternals(index, vArray)
                .setSeed(seed)
                .build();

        Well well = new Well(wellState);
        WellState actualWellState = well.getWellState();

        assertNotNull(well);
        assertEquals(wellState, actualWellState);
    }

    @Test
    @UnitTestMethod(target = Well.class, name = "getWellState", args = {})
    public void testGetWellState() {
        Long seed = 6559152513645047938L;
        int index = 13;
        int[] vArray = new int[1391];

        WellState wellState = WellState.builder()
                .setInternals(index, vArray)
                .setSeed(seed)
                .build();

        Well well = new Well(wellState);
        WellState actualWellState = well.getWellState();

        assertEquals(wellState, actualWellState);
    }

    @Test
    @UnitTestMethod(target = Well.class, name = "hashCode", args = {})
    public void testHashCode() {
        Long seed = 6559152513645047938L;
        int index = 13;
        int[] vArray = new int[1391];

        WellState wellState = WellState.builder()
                .setInternals(index, vArray)
                .setSeed(seed)
                .build();

        Well well = new Well(wellState);
        int expectedHashCode = wellState.hashCode();
        int actualHashCode = well.hashCode();

        assertEquals(expectedHashCode, actualHashCode);
    }

    @Test
    @UnitTestMethod(target = Well.class, name = "equals", args = {Object.class})
    public void testEquals() {
        Long seed = 6559152513645047938L;
        int index = 13;
        int[] vArray = new int[1391];

        WellState wellState = WellState.builder()
                .setInternals(index, vArray)
                .setSeed(seed)
                .build();

        Long seed2 = 9065977559722459948L;
        WellState wellState2 = WellState.builder()
                .setInternals(15, vArray)
                .setSeed(seed2)
                .build();

        Well well = new Well(wellState);
        Well duplicateWell = new Well(wellState);
        Well differentWell = new Well(wellState2);

        assertTrue(well.equals(duplicateWell));
        assertFalse(well.equals(differentWell));
    }
}
