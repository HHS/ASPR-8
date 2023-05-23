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
        Set<Integer> hashCodes = new LinkedHashSet<>();
        Long seed = 6559152513645047938L;
        WellState wellState1 = createWellState(seed);
        Well well1 = new Well(wellState1);
        Integer wellHash1 = well1.hashCode();
        for (int i = 0; i < 5; i++) {
            WellState wellState = createWellState(seed);
            Well well = new Well(wellState);
            Integer wellHash = well.hashCode();
            hashCodes.add(wellHash);
            // show that when called multiple times on the same object, the same hash code is returned
            assertEquals(wellHash, well.hashCode());
        }
        for (Integer hashCode : hashCodes) {
            assertEquals(wellHash1, hashCode);
        }

        WellState differentWellState = createWellState(5887805138861262404L);
        Well differentWell = new Well(differentWellState);
        Integer differentHash = differentWell.hashCode();
        assertNotEquals(differentHash, wellHash1);

        // show that hash codes are reasonably distributed
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4898576492932415902L);
        hashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            Long stateSeed = randomGenerator.nextLong();
            WellState wellState = createWellState(stateSeed);
            Well well = new Well(wellState);
            int wellHash = well.hashCode();
            hashCodes.add(wellHash);
        }
        assertTrue(hashCodes.size() >= 90);
    }

    @Test
    @UnitTestMethod(target = Well.class, name = "equals", args = {Object.class})
    public void testEquals() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7242512295369848202L);
        Set<Well> wells = new LinkedHashSet<>();
        Long seed = 2864116845603920430L;
        WellState wellState1 = createWellState(seed);
        Well well1 = new Well(wellState1);
        for (int i = 0; i < 5; i++) {
            WellState wellState = createWellState(seed);
            Well well = new Well(wellState);
            wells.add(well);
        }
        for (Well well : wells) {
            assertEquals(well1, well);
        }

        for (int i = 0; i < 5; i++) {
            WellState wellState = createWellState(randomGenerator.nextLong());
            Well well = new Well(wellState);
            assertNotEquals(well1, well);
        }

        wells = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            Long wellSeed = randomGenerator.nextLong();
            WellState wellState = createWellState(wellSeed);
            Well well = new Well(wellState);
            wells.add(well);
        }
        assertTrue(wells.size() >= 95);
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
