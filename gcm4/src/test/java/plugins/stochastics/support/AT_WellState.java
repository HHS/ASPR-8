package plugins.stochastics.support;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AT_WellState {

    @Test
    @UnitTestMethod(target = WellState.Builder.class, name = "build", args = {}, tags = UnitTag.INCOMPLETE)
    public void testBuild() {
        WellState wellState = WellState.builder().build();
        assertNotNull(wellState);
    }

    @Test
    @UnitTestMethod(target = WellState.Builder.class, name = "setSeed", args = {long.class}, tags = UnitTag.INCOMPLETE)
    public void testSetSeed() {
        Long expectedSeed = 7408812677905090200L;

        WellState wellState = WellState.builder()
                .setSeed(expectedSeed)
                .build();

        Long actualSeed = wellState.getSeed();
        assertEquals(expectedSeed, actualSeed);
    }

    @Test
    @UnitTestMethod(target = WellState.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(WellState.builder());
    }

    @Test
    @UnitTestMethod(target = WellState.class, name = "getSeed", args = {})
    public void testGetSeed() {
        Long expectedSeed = 764258969836004163L;

        WellState wellState = WellState.builder()
                .setSeed(expectedSeed)
                .build();

        Long actualSeed = wellState.getSeed();
        assertEquals(expectedSeed, actualSeed);
    }

    @Test
    @UnitTestMethod(target = WellState.class, name = "getIndex", args = {})
    public void testGetIndex() {
        WellState.Builder builder = WellState.builder();
        Long seed = 6559152513645047938L;
        int expectedIndex = 13;
        int[] vArray = new int[1391];

        WellState wellState = builder
                .setInternals(expectedIndex, vArray)
                .setSeed(seed)
                .build();
        int actualIndex = wellState.getIndex();
        assertEquals(expectedIndex, actualIndex);
    }

    @Test
    @UnitTestMethod(target = WellState.class, name = "getVArray", args = {})
    public void testGetVArray() {
        WellState.Builder builder = WellState.builder();
        Long seed = 6559152513645047938L;
        int index = 13;
        int[] expectedVArray = new int[1391];

        WellState wellState = builder
                .setInternals(index, expectedVArray)
                .setSeed(seed)
                .build();
        int[] actualVArray = wellState.getVArray();

        assertTrue(Arrays.equals(expectedVArray, actualVArray));
    }

    @Test
    @UnitTestMethod(target = WellState.class, name = "hashCode", args = {})
    public void testHashCode() {
        // show that equal well states have equal hash codes
        Set<Integer> hashCodes = new LinkedHashSet<>();
        Long seed = 6559152513645047938L;
        WellState wellState1 = createWellState(seed);
        Integer wellStateHash = wellState1.hashCode();
        for (int i = 0; i < 5; i++) {
            WellState wellState = createWellState(seed);
            Integer stateHash = wellState.hashCode();
            hashCodes.add(stateHash);
            // show that when called multiple times on the same object, the same hash code is returned
            assertEquals(stateHash, wellState.hashCode());
        }
        for (Integer hashCode : hashCodes) {
            assertEquals(wellStateHash, hashCode);
        }
        WellState differentWellState = createWellState(5887805138861262404L);
        Integer differentHash = differentWellState.hashCode();
        assertNotEquals(differentHash, wellStateHash);

        // show that hash codes are reasonably distributed
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4898576492932415902L);
        hashCodes = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            Long stateSeed = randomGenerator.nextLong();
            WellState wellState = createWellState(stateSeed);
            int stateHash = wellState.hashCode();
            hashCodes.add(stateHash);
        }
        assertTrue(hashCodes.size() >= 90);
    }

    @Test
    @UnitTestMethod(target = WellState.Builder.class, name = "setInternals", args = {int.class, int[].class}, tags = UnitTag.INCOMPLETE)
    public void testSetInternals() {
        WellState.Builder builder = WellState.builder();
        Long seed = 6559152513645047938L;
        int index = 13;
        int[] vArray = new int[1391];

        WellState wellState = builder
                .setInternals(index, vArray)
                .setSeed(seed)
                .build();

        int actualIndex = wellState.getIndex();
        int[] actualVArray = wellState.getVArray();

        assertEquals(index, actualIndex);
        assertTrue(Arrays.equals(vArray, actualVArray));

        // precondition test: null vArray
        ContractException contractException = assertThrows(ContractException.class, () -> builder.setInternals(index, null));
        assertEquals(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE, contractException.getErrorType());

        // precondition test: improper vArray size
        ContractException contractException2 = assertThrows(ContractException.class, () -> builder.setInternals(index, new int[15]));
        assertEquals(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE, contractException2.getErrorType());

        // precondition test: index out of allowed range
        ContractException contractException3 = assertThrows(ContractException.class, () -> builder.setInternals(-1, vArray));
        assertEquals(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE, contractException3.getErrorType());

        ContractException contractException4 = assertThrows(ContractException.class, () -> builder.setInternals(1391, vArray));
        assertEquals(StochasticsError.ILLEGAL_SEED_ININITIAL_STATE, contractException4.getErrorType());
    }

    @Test
    @UnitTestMethod(target = WellState.class, name = "equals", args = {Object.class}, tags = UnitTag.INCOMPLETE)
    public void testEquals() {
        Set<WellState> wellStates = new LinkedHashSet<>();
        Long seed = 2864116845603920430L;
        WellState wellState1 = createWellState(seed);
        for (int i = 0; i < 5; i++) {
            WellState wellState = createWellState(seed);
            wellStates.add(wellState);
        }
        for (WellState wellState : wellStates) {
            assertEquals(wellState1, wellState);
        }

        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7242512295369848202L);
        wellStates = new LinkedHashSet<>();
        for (int i = 0; i < 100; i++) {
            Long stateSeed = randomGenerator.nextLong();
            WellState wellState = createWellState(stateSeed);
            wellStates.add(wellState);
        }
        assertTrue(wellStates.size() >= 95);
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
