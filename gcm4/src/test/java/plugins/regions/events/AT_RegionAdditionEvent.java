package plugins.regions.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.support.SimpleRegionId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

@UnitTest(target = RegionAdditionEvent.class)
public class AT_RegionAdditionEvent {

    @Test
    @UnitTestMethod(name = "builder", args = {})
    public void testBuilder() {
        RegionAdditionEvent.Builder builder = RegionAdditionEvent.builder();

        assertNotNull(builder);
    }

    @Test
    @UnitTestMethod(name = "getValues", args = { Class.class })
    public void testGetValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2586891640909005860L);
        RegionAdditionEvent.Builder builder = RegionAdditionEvent.builder();
        RegionId regionId = new SimpleRegionId(1000);

        List<String> expectedStringValues = new ArrayList<>();
        List<Integer> expectedIntValues = new ArrayList<>();

        int numValues = randomGenerator.nextInt(15);

        for (int i = 0; i < numValues; i++) {
            int intValue = randomGenerator.nextInt(100);
            String value = Integer.toString(randomGenerator.nextInt(100));
            expectedStringValues.add(value);
            expectedIntValues.add(intValue);
            builder.addValue(value).addValue(intValue);
        }
        builder.setRegionId(regionId);

        RegionAdditionEvent regionAdditionEvent = builder.build();

        assertNotNull(regionAdditionEvent);
        assertEquals(expectedStringValues, regionAdditionEvent.getValues(String.class));
        assertEquals(expectedIntValues, regionAdditionEvent.getValues(Integer.class));
    }

    @Test
    @UnitTestMethod(name = "getRegionId", args = {})
    public void testGetRegionId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3237014269401968266L);
        RegionAdditionEvent.Builder builder = RegionAdditionEvent.builder();
        RegionId regionId = new SimpleRegionId(1000);

        for (int i = 0; i < 15; i++) {
            String value = Integer.toString(randomGenerator.nextInt(100));
            builder.addValue(value);
        }
        builder.setRegionId(regionId);

        RegionAdditionEvent regionAdditionEvent = builder.build();

        assertNotNull(regionAdditionEvent);
        assertEquals(regionId, regionAdditionEvent.getRegionId());
    }

    @Test
    @UnitTestMethod(target = RegionAdditionEvent.Builder.class, name = "build", args = {})
    public void testBuild() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5095911653055460787L);
        RegionAdditionEvent.Builder builder = RegionAdditionEvent.builder();
        RegionId regionId = new SimpleRegionId(1000);

        for (int i = 0; i < 15; i++) {
            String value = Integer.toString(randomGenerator.nextInt(100));
            builder.addValue(value);
        }
        builder.setRegionId(regionId);

        RegionAdditionEvent regionAdditionEvent = builder.build();

        assertNotNull(regionAdditionEvent);
        // builder.addValue() and builder.setRegionId are covered by other tests

        // precondition: null region id
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionAdditionEvent.builder().addValue(Integer.toString(randomGenerator.nextInt(100))).build());
        assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = RegionAdditionEvent.Builder.class, name = "addValue", args = { Object.class })
    public void testAddValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3583185907954183553L);
        RegionAdditionEvent.Builder builder = RegionAdditionEvent.builder();
        RegionId regionId = new SimpleRegionId(1000);

        List<String> expectedStringValues = new ArrayList<>();
        List<Integer> expectedIntValues = new ArrayList<>();

        int numValues = randomGenerator.nextInt(15);

        for (int i = 0; i < numValues; i++) {
            int intValue = randomGenerator.nextInt(100);
            String value = Integer.toString(randomGenerator.nextInt(100));
            expectedStringValues.add(value);
            expectedIntValues.add(intValue);
            builder.addValue(value).addValue(intValue);
        }
        builder.setRegionId(regionId);

        RegionAdditionEvent regionAdditionEvent = builder.build();

        assertNotNull(regionAdditionEvent);
        assertEquals(expectedStringValues, regionAdditionEvent.getValues(String.class));
        assertEquals(expectedIntValues, regionAdditionEvent.getValues(Integer.class));

        // precondition: value is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionAdditionEvent.builder().addValue(null).setRegionId(regionId).build());
        assertEquals(RegionError.NULL_AUXILIARY_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = RegionAdditionEvent.Builder.class, name = "setRegionId", args = { RegionId.class })
    public void testSetRegiodId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5731494247551639715L);
        RegionAdditionEvent.Builder builder = RegionAdditionEvent.builder();
        RegionId regionId = new SimpleRegionId(1000);

        for (int i = 0; i < 15; i++) {
            String value = Integer.toString(randomGenerator.nextInt(100));
            builder.addValue(value);
        }
        builder.setRegionId(regionId);

        RegionAdditionEvent regionAdditionEvent = builder.build();

        assertNotNull(regionAdditionEvent);
        assertEquals(regionId, regionAdditionEvent.getRegionId());

        // precondition: region id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionAdditionEvent.builder().setRegionId(null).build());
        assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
    }
}
