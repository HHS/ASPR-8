package plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_MaterialsProducerConstructionData {

    @Test
    @UnitTestMethod(target = MaterialsProducerConstructionData.class,name = "builder", args = {})
    public void testBuilder() {
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();

        assertNotNull(builder);
    }

    @Test
    @UnitTestMethod(target = MaterialsProducerConstructionData.class,name = "getValues", args = { Class.class })
    public void testGetValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7180465772129297639L);
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();
        builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);

        List<Integer> expectedIntegers = new ArrayList<>();
        List<Double> expectedDoubles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            if (i % 2 == 0) {
                int value = randomGenerator.nextInt(100);
                expectedIntegers.add(value);
                builder.addValue(value);
            } else {
                double value = randomGenerator.nextDouble() * 100;
                expectedDoubles.add(value);
                builder.addValue(value);
            }
        }

        MaterialsProducerConstructionData constructionData = builder.build();
        assertNotNull(constructionData);

        assertEquals(expectedIntegers, constructionData.getValues(Integer.class));
        assertEquals(expectedDoubles, constructionData.getValues(Double.class));

    }

    @Test
    @UnitTestMethod(target = MaterialsProducerConstructionData.class,name = "getMaterialsProducerId", args = {})
    public void testGetMaterialsProducerId() {
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();
        builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);

        MaterialsProducerConstructionData constructionData = builder.build();
        assertNotNull(constructionData);
        assertEquals(TestMaterialsProducerId.MATERIALS_PRODUCER_1, constructionData.getMaterialsProducerId());
    }

    @Test
    @UnitTestMethod(target = MaterialsProducerConstructionData.class,name = "getMaterialsProducerPropertyValues", args = {})
    public void testGetMaterialsProducerPropertyValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6832539036105490849L);
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();
        builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);

        Map<MaterialsProducerPropertyId, Object> expectedValues = new LinkedHashMap<>();

        for (int i = 0; i < 15; i++) {
            TestMaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId
                    .getRandomMaterialsProducerPropertyId(randomGenerator);
            if (!expectedValues.containsKey(producerPropertyId)) {
                Object value = producerPropertyId.getRandomPropertyValue(randomGenerator);
                builder.setMaterialsProducerPropertyValue(producerPropertyId, value);
                expectedValues.put(producerPropertyId, value);
            }
        }

        MaterialsProducerConstructionData constructionData = builder.build();
        assertNotNull(constructionData);
        assertEquals(expectedValues, constructionData.getMaterialsProducerPropertyValues());
    }

    @Test
	@UnitTestMethod(target = MaterialsProducerConstructionData.class,name = "getResourceLevels", args = {})
    public void testGetResourceLevels() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6832539036105490849L);
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();
        builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);

        Map<ResourceId, Long> expectedValues = new LinkedHashMap<>();
        for (int i = 0; i < 15; i++) {
            ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
            if (!expectedValues.containsKey(resourceId)) {
                long value = randomGenerator.nextInt(100 * (i + 1)) + 1;
                builder.setResourceLevel(resourceId, value);
                expectedValues.put(resourceId, value);
            }
        }

        MaterialsProducerConstructionData constructionData = builder.build();
        assertNotNull(constructionData);
        assertEquals(expectedValues, constructionData.getResourceLevels());

    }

    @Test
    @UnitTestMethod(target = MaterialsProducerConstructionData.Builder.class, name = "build", args = {})
    public void testBuild() {
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();
        builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);

        MaterialsProducerConstructionData constructionData = builder.build();
        assertNotNull(constructionData);

        // precondition: materials producer id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsProducerConstructionData.builder().build());
        assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = MaterialsProducerConstructionData.Builder.class, name = "addValue", args = {
            Object.class })
    public void testAddValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8237136898094031982L);
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();
        builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);

        List<Integer> expectedIntegers = new ArrayList<>();
        List<Double> expectedDoubles = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            if (i % 2 == 0) {
                int value = randomGenerator.nextInt(100);
                expectedIntegers.add(value);
                builder.addValue(value);
            } else {
                double value = randomGenerator.nextDouble() * 100;
                expectedDoubles.add(value);
                builder.addValue(value);
            }
        }

        MaterialsProducerConstructionData constructionData = builder.build();
        assertNotNull(constructionData);

        assertEquals(expectedIntegers, constructionData.getValues(Integer.class));
        assertEquals(expectedDoubles, constructionData.getValues(Double.class));

        // precondition: value is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsProducerConstructionData.builder().addValue(null));
        assertEquals(MaterialsError.NULL_AUXILIARY_DATA, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = MaterialsProducerConstructionData.Builder.class, name = "setMaterialsProducerPropertyValue", args = {
            MaterialsProducerPropertyId.class, Object.class })
    public void testSetMaterialsProducerPropertyValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8800503605965150018L);
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();
        builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);

        Map<MaterialsProducerPropertyId, Object> expectedValues = new LinkedHashMap<>();

        for (int i = 0; i < 15; i++) {
            TestMaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId
                    .getRandomMaterialsProducerPropertyId(randomGenerator);
            if (!expectedValues.containsKey(producerPropertyId)) {
                Object value = producerPropertyId.getRandomPropertyValue(randomGenerator);
                builder.setMaterialsProducerPropertyValue(producerPropertyId, value);
                expectedValues.put(producerPropertyId, value);
            }
        }

        MaterialsProducerConstructionData constructionData = builder.build();
        assertNotNull(constructionData);
        assertEquals(expectedValues, constructionData.getMaterialsProducerPropertyValues());

        // precondition: materials producer property id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsProducerConstructionData.builder()
                        .setMaterialsProducerPropertyValue(null, "100"));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition: value is null
        contractException = assertThrows(ContractException.class,
                () -> MaterialsProducerConstructionData.builder()
                        .setMaterialsProducerPropertyValue(
                                TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK,
                                null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

        // precondition: duplicate producer property id
        contractException = assertThrows(ContractException.class,
                () -> MaterialsProducerConstructionData.builder()
                        .setMaterialsProducerPropertyValue(
                                TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK,
                                false)
                        .setMaterialsProducerPropertyValue(
                                TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK,
                                true));
        assertEquals(PropertyError.DUPLICATE_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = MaterialsProducerConstructionData.Builder.class, name = "setMaterialsProducerId", args = {
            MaterialsProducerId.class })
    public void testSetMaterialsProducerId() {
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();
        builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);

        MaterialsProducerConstructionData constructionData = builder.build();
        assertNotNull(constructionData);
        assertEquals(TestMaterialsProducerId.MATERIALS_PRODUCER_1, constructionData.getMaterialsProducerId());

        // precondition: materials producer id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsProducerConstructionData.builder().setMaterialsProducerId(null));
        assertEquals(MaterialsError.NULL_MATERIALS_PRODUCER_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = MaterialsProducerConstructionData.Builder.class, name = "setResourceLevel", args = {
            ResourceId.class, long.class })
    public void testSetResourceLevel() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6090230296084756769L);
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();
        builder.setMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_1);

        Map<ResourceId, Long> expectedValues = new LinkedHashMap<>();
        for (int i = 0; i < 15; i++) {
            ResourceId resourceId = TestResourceId.getRandomResourceId(randomGenerator);
            if (!expectedValues.containsKey(resourceId)) {
                long value = randomGenerator.nextInt(100 * (i + 1)) + 1;
                builder.setResourceLevel(resourceId, value);
                expectedValues.put(resourceId, value);
            }
        }

        MaterialsProducerConstructionData constructionData = builder.build();
        assertNotNull(constructionData);
        assertEquals(expectedValues, constructionData.getResourceLevels());

        // precondition: resource id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> MaterialsProducerConstructionData.builder()
                        .setResourceLevel(null, 0));
        assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

        // precondition: level is negative (< 0)
        contractException = assertThrows(ContractException.class,
                () -> MaterialsProducerConstructionData.builder()
                        .setResourceLevel(TestResourceId.RESOURCE_1, -100));
        assertEquals(ResourceError.NEGATIVE_RESOURCE_AMOUNT, contractException.getErrorType());

        // precondition: duplicate resource id
        contractException = assertThrows(ContractException.class,
                () -> MaterialsProducerConstructionData.builder()
                        .setResourceLevel(TestResourceId.RESOURCE_1, 100)
                        .setResourceLevel(TestResourceId.RESOURCE_1, 100));
        assertEquals(ResourceError.DUPLICATE_REGION_RESOURCE_LEVEL_ASSIGNMENT, contractException.getErrorType());
    }
}
