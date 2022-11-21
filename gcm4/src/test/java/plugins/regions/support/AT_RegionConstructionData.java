package plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

@UnitTest(target = RegionConstructionData.class)
public class AT_RegionConstructionData {
    @Test
    @UnitTestMethod(name = "builder", args = {})
    public void testBuilder() {
        RegionConstructionData.Builder builder = RegionConstructionData.builder();

        assertNotNull(builder);
    }

    @Test
    @UnitTestMethod(name = "getValues", args = { Class.class })
    public void testGetValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3711237688912261992L);
        RegionConstructionData.Builder builder = RegionConstructionData.builder();

        RegionId regionId = new SimpleRegionId(1000);

        List<String> expectedStringValues = new ArrayList<>();
        List<Integer> expectedIntValues = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            int integerValue = randomGenerator.nextInt(100);
            String stringValue = Integer.toString(randomGenerator.nextInt(100));

            expectedIntValues.add(integerValue);
            expectedStringValues.add(stringValue);
            builder.addValue(stringValue).addValue(integerValue);
        }

        builder.setRegionId(regionId);

        RegionConstructionData regionConstructionData = builder.build();

        assertNotNull(regionConstructionData);
        assertEquals(expectedIntValues, regionConstructionData.getValues(Integer.class));
        assertEquals(expectedStringValues, regionConstructionData.getValues(String.class));
    }

    @Test
    @UnitTestMethod(name = "getRegionPropertyValues", args = {})
    public void testGetRegionPropertyValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1088787935993630091L);
        RegionConstructionData.Builder builder = RegionConstructionData.builder();

        RegionId regionId = new SimpleRegionId(1000);

        Map<RegionPropertyId, Object> expectedValues = new LinkedHashMap<>();

        for (int i = 0; i < 15; i++) {
            RegionPropertyId regionPropertyId = new SimpleRegionPropertyId(i * 2 + 5);
            int integerValue = randomGenerator.nextInt(100);

            expectedValues.put(regionPropertyId, integerValue);
            builder.setRegionPropertyValue(regionPropertyId, integerValue);
        }

        builder.setRegionId(regionId);

        RegionConstructionData regionConstructionData = builder.build();

        assertNotNull(regionConstructionData);
        assertEquals(expectedValues, regionConstructionData.getRegionPropertyValues());
    }

    @Test
    @UnitTestMethod(name = "getRegionId", args = {})
    public void testGetRegionId() {
        RegionConstructionData.Builder builder = RegionConstructionData.builder();

        RegionId regionId = new SimpleRegionId(1000);
        builder.setRegionId(regionId);

        RegionConstructionData regionConstructionData = builder.build();

        assertNotNull(regionConstructionData);
        assertEquals(regionId, regionConstructionData.getRegionId());
    }

    @Test
    @UnitTestMethod(target = RegionConstructionData.Builder.class, name = "build", args = {})
    public void testBuild() {
        RegionConstructionData.Builder builder = RegionConstructionData.builder();

        RegionId regionId = new SimpleRegionId(1000);
        builder.setRegionId(regionId);

        RegionConstructionData regionConstructionData = builder.build();

        assertNotNull(regionConstructionData);

        // precondition: null region id
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionConstructionData.builder()
                        .build());
        assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = RegionConstructionData.Builder.class, name = "setRegionPropertyValue", args = {
            RegionPropertyId.class, Object.class })
    public void testSetRegionPropertyValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5396117443174616496L);
        RegionConstructionData.Builder builder = RegionConstructionData.builder();

        RegionId regionId = new SimpleRegionId(1000);

        Map<RegionPropertyId, Object> expectedValues = new LinkedHashMap<>();

        for (int i = 0; i < 15; i++) {
            RegionPropertyId regionPropertyId = new SimpleRegionPropertyId(i * 2 + 5);
            int integerValue = randomGenerator.nextInt(100);

            expectedValues.put(regionPropertyId, integerValue);
            builder.setRegionPropertyValue(regionPropertyId, integerValue);
        }

        builder.setRegionId(regionId);

        RegionConstructionData regionConstructionData = builder.build();

        assertNotNull(regionConstructionData);
        assertEquals(expectedValues, regionConstructionData.getRegionPropertyValues());

        // precondition: null property id
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionConstructionData.builder()
                        .setRegionId(regionId)
                        .setRegionPropertyValue(null, 100)
                        .build());
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition: null property value
        contractException = assertThrows(ContractException.class,
                () -> RegionConstructionData.builder()
                        .setRegionId(regionId)
                        .setRegionPropertyValue(new SimpleRegionPropertyId(100), null)
                        .build());
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

        // precondition: duplicate property value
        RegionPropertyId regionPropertyId = new SimpleRegionPropertyId(100);
        contractException = assertThrows(ContractException.class,
                () -> RegionConstructionData.builder()
                        .setRegionId(regionId)
                        .setRegionPropertyValue(regionPropertyId, 100)
                        .setRegionPropertyValue(regionPropertyId, 150)
                        .build());
        assertEquals(PropertyError.DUPLICATE_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = RegionConstructionData.Builder.class, name = "addValue", args = { Object.class })
    public void testAddValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4498242625038387679L);
        RegionConstructionData.Builder builder = RegionConstructionData.builder();

        RegionId regionId = new SimpleRegionId(1000);

        List<String> expectedStringValues = new ArrayList<>();
        List<Integer> expectedIntValues = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            int integerValue = randomGenerator.nextInt(100);
            String stringValue = Integer.toString(randomGenerator.nextInt(100));

            expectedIntValues.add(integerValue);
            expectedStringValues.add(stringValue);
            builder.addValue(stringValue).addValue(integerValue);
        }

        builder.setRegionId(regionId);

        RegionConstructionData regionConstructionData = builder.build();

        assertNotNull(regionConstructionData);
        assertEquals(expectedIntValues, regionConstructionData.getValues(Integer.class));
        assertEquals(expectedStringValues, regionConstructionData.getValues(String.class));
    }

    @Test
    @UnitTestMethod(target = RegionConstructionData.Builder.class, name = "setRegionId", args = { RegionId.class })
    public void testSetRegionId() {
        RegionConstructionData.Builder builder = RegionConstructionData.builder();

        RegionId regionId = new SimpleRegionId(1000);
        builder.setRegionId(regionId);

        RegionConstructionData regionConstructionData = builder.build();

        assertNotNull(regionConstructionData);
        assertEquals(regionId, regionConstructionData.getRegionId());

        // precondition: null region id
        ContractException contractException = assertThrows(ContractException.class,
                () -> RegionConstructionData.builder()
                        .build());
        assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());
    }
}
