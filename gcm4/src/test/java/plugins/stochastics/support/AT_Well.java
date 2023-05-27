package plugins.stochastics.support;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AT_Well {

    @Test
    @UnitTestConstructor(target = Well.class, args = {WellState.class})
    public void testWell() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3201733256995562119L);
        for (int i = 0; i < 30; i++) {
            Long seed = randomGenerator.nextLong();
            WellState wellState = createWellState(seed);
            Well well = new Well(wellState);
            WellState actualWellState = well.getWellState();
            assertNotNull(well);
            assertEquals(wellState, actualWellState);
        }
    }

    @Test
    @UnitTestMethod(target = Well.class, name = "getWellState", args = {})
    public void testGetWellState() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3201733256995562119L);
        for (int i = 0; i < 30; i++) {
            Long seed = randomGenerator.nextLong();
            WellState wellState = createWellState(seed);
            Well well = new Well(wellState);
            WellState actualWellState = well.getWellState();
            assertEquals(wellState, actualWellState);
        }
    }

    @Test
    @UnitTestMethod(target = Well.class, name = "hashCode", args = {})
    public void testHashCode() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3103545448276048549L);
        // show that equal well states have equal hash codes
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
            WellState wellState1 = createWellState(seed);
            WellState wellState2 = createWellState(seed);
            assertEquals(wellState1, wellState2);
            assertEquals(wellState1.hashCode(), wellState2.hashCode());
        }

        // show that hash codes are reasonably distributed
        Set<Integer> hashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            Long stateSeed = randomGenerator.nextLong();
            WellState wellState = createWellState(stateSeed);
            Well well = new Well(wellState);
            hashCodes.add(well.hashCode());
        }
        assertTrue(hashCodes.size() >= 90);
    }

    @Test
    @UnitTestMethod(target = Well.class, name = "equals", args = {Object.class})
    public void testEquals() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7242512295369848202L);
        // no object equals null
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
            WellState wellState = createWellState(seed);
            Well well = new Well(wellState);
            assertFalse(well.equals(null));
        }

        // reflexive
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
            WellState wellState = createWellState(seed);
            Well well = new Well(wellState);
            assertTrue(well.equals(well));
        }

        // symmetric
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
            WellState wellState1 = createWellState(seed);
            Well well1 = new Well(wellState1);
            WellState wellState2 = createWellState(seed);
            Well well2 = new Well(wellState2);
            assertTrue(well1.equals(well2));
            assertTrue(well2.equals(well1));
        }

        // transitive
        for (int i = 0; i < 30; i++) {
            long seed = randomGenerator.nextLong();
            WellState wellState1 = createWellState(seed);
            Well well1 = new Well(wellState1);
            WellState wellState2 = createWellState(seed);
            Well well2 = new Well(wellState2);
            WellState wellState3 = createWellState(seed);
            Well well3 = new Well(wellState3);
            assertTrue(well1.equals(well2));
            assertTrue(well2.equals(well3));
            assertTrue(well1.equals(well3));
        }
    }

    private WellState createWellState(Long seed) {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
        int stateIndex = randomGenerator.nextInt(1390);
        int[] vArray = new int[1391];

        for (int i = 0; i < 1391; i++) {
            vArray[i] = randomGenerator.nextInt();
        }
        WellState wellState = WellState.builder().setInternals(stateIndex, vArray).setSeed(seed).build();
        return wellState;
    }
}
