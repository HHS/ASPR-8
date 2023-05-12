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
        WellState.Builder builder = WellState.builder();
        Long seed = 6559152513645047938L;
        int index = 13;
        int[] vArray = new int[1391];

        WellState wellState1 = builder.setSeed(seed).setInternals(index, vArray).build();
        WellState wellState2 = builder.setSeed(seed).setInternals(index, vArray).build();

        assertEquals(wellState1.hashCode(), wellState2.hashCode());

        // show that hash codes are reasonably distributed
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4898576492932415902L);
        Set<Integer> hashCodes = new LinkedHashSet<Integer>();
        for (int i = 0; i < 100; i++) {
            Long stateSeed = randomGenerator.nextLong();
            int stateIndex = randomGenerator.nextInt(1390);
            WellState wellState = builder.setInternals(stateIndex, vArray).setSeed(stateSeed).build();
            int stateHash = wellState.hashCode();
            hashCodes.add(stateHash);
        }
        assertTrue(hashCodes.size() >= 85);
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
        WellState.Builder builder = WellState.builder();
        Long seed = 6559152513645047938L;
        int index = 13;
        int[] vArray = new int[1391];

        WellState wellState = builder
                .setInternals(index, vArray)
                .setSeed(seed)
                .build();

        WellState duplicateState = builder
                .setInternals(index, vArray)
                .setSeed(seed)
                .build();

        WellState differentState = builder
                .setInternals(23, vArray)
                .setSeed(5289854998653146199L)
                .build();

        assertTrue(wellState.equals(duplicateState));
        assertFalse(wellState.equals(differentState));
    }


}
