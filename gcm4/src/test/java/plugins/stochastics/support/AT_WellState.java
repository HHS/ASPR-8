package plugins.stochastics.support;

import org.junit.jupiter.api.Test;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

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
    @UnitTestMethod(target = WellState.Builder.class, name = "setInternals", args = {int.class, int[].class}, tags = UnitTag.INCOMPLETE)
    public void testSetInternals() {
        WellState.Builder builder = WellState.builder();
        int index = 13;
        int[] vArray = new int[1391];

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

    }


}
