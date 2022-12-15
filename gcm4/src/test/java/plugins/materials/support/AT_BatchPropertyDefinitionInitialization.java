package plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

@UnitTest(target = BatchPropertyDefinitionInitialization.class)
public class AT_BatchPropertyDefinitionInitialization {
    @Test
    @UnitTestMethod(name = "builder", args={})
    public void testBuilder() {
        BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
        assertNotNull(builder);
    }
    @Test
    @UnitTestMethod(name = "getPropertyDefinition", args={})
    public void testGetPropertyDefinition() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue(100)
                .setPropertyValueMutability(false)
                .setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)
                .setType(Integer.class)
                .build();

        BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
        MaterialId materialId = TestMaterialId.MATERIAL_1;

        BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
        builder.setMaterialId(materialId);
        builder.setPropertyId(batchPropertyId);
        builder.setPropertyDefinition(propertyDefinition);

        BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

        assertNotNull(definitionInitialization);
        assertEquals(propertyDefinition, definitionInitialization.getPropertyDefinition());
    }
    @Test
    @UnitTestMethod(name = "getPropertyId", args={})
    public void testGetPropertyId() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue(100)
                .setPropertyValueMutability(false)
                .setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)
                .setType(Integer.class)
                .build();

        BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
        MaterialId materialId = TestMaterialId.MATERIAL_1;

        BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
        builder.setMaterialId(materialId);
        builder.setPropertyId(batchPropertyId);
        builder.setPropertyDefinition(propertyDefinition);

        BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

        assertNotNull(definitionInitialization);
        assertEquals(batchPropertyId, definitionInitialization.getPropertyId());
    }
    @Test
    @UnitTestMethod(name = "getPropertyValues", args={})
    public void testGetPropertyValues() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8487271708488687492L);
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("100")
                .setPropertyValueMutability(false)
                .setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)
                .setType(String.class)
                .build();

        BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
        MaterialId materialId = TestMaterialId.MATERIAL_1;

        BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
        builder.setMaterialId(materialId);
        builder.setPropertyId(batchPropertyId);
        builder.setPropertyDefinition(propertyDefinition);

        List<Pair<BatchId, Object>> expectedValues = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            String value = Integer.toString(randomGenerator.nextInt(100));
            BatchId batchId = new BatchId(i);
            builder.addPropertyValue(batchId, value);
            expectedValues.add(new Pair<>(batchId, value));
        }

        BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

        assertNotNull(definitionInitialization);
        assertEquals(expectedValues.size(), definitionInitialization.getPropertyValues().size());
        assertEquals(expectedValues, definitionInitialization.getPropertyValues());

    }
    @Test
    @UnitTestMethod(name = "getMaterialId", args={})
    public void testGetMaterialId() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue(100)
                .setPropertyValueMutability(false)
                .setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)
                .setType(Integer.class)
                .build();

        BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
        MaterialId materialId = TestMaterialId.MATERIAL_1;

        BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
        builder.setMaterialId(materialId);
        builder.setPropertyId(batchPropertyId);
        builder.setPropertyDefinition(propertyDefinition);

        BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

        assertNotNull(definitionInitialization);
        assertEquals(materialId, definitionInitialization.getMaterialId());
    }
    @Test
    @UnitTestMethod(target= BatchPropertyDefinitionInitialization.Builder.class, name = "build", args={})
    public void testBuild() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8487271708488687492L);
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("100")
                .setPropertyValueMutability(false)
                .setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)
                .setType(String.class)
                .build();

        BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
        MaterialId materialId = TestMaterialId.MATERIAL_1;

        BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
        builder.setMaterialId(materialId);
        builder.setPropertyId(batchPropertyId);
        builder.setPropertyDefinition(propertyDefinition);

        List<Pair<BatchId, Object>> expectedValues = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            String value = Integer.toString(randomGenerator.nextInt(100));
            BatchId batchId = new BatchId(i);
            builder.addPropertyValue(batchId, value);
            expectedValues.add(new Pair<>(batchId, value));
        }

        BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

        assertNotNull(definitionInitialization);

        // precondition: null property definition
        PropertyDefinition nPropertyDefinition = null;
        ContractException contractException = assertThrows(ContractException.class,
                () -> BatchPropertyDefinitionInitialization.builder()
                        .setMaterialId(materialId)
                        .setPropertyId(batchPropertyId)
                        .setPropertyDefinition(nPropertyDefinition)
                        .build());
        assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());

        // precondition: null property id
        BatchPropertyId nBatchPropertyId = null;
        contractException = assertThrows(ContractException.class,
                () -> BatchPropertyDefinitionInitialization.builder()
                        .setMaterialId(materialId)
                        .setPropertyId(nBatchPropertyId)
                        .setPropertyDefinition(propertyDefinition)
                        .build());
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition: null material id
        MaterialId nMaterialId = null;
        contractException = assertThrows(ContractException.class,
                () -> BatchPropertyDefinitionInitialization.builder()
                        .setMaterialId(nMaterialId)
                        .setPropertyId(batchPropertyId)
                        .setPropertyDefinition(propertyDefinition)
                        .build());
        assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

        // precondition: incomaptible value
        contractException = assertThrows(ContractException.class,
                () -> BatchPropertyDefinitionInitialization.builder()
                        .setMaterialId(materialId)
                        .setPropertyId(batchPropertyId)
                        .setPropertyDefinition(propertyDefinition)
                        .addPropertyValue(new BatchId(0), 100)
                        .build());
        assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

    }
    @Test
    @UnitTestMethod(target= BatchPropertyDefinitionInitialization.Builder.class, name = "setPropertyDefinition", args={PropertyDefinition.class})
    public void testSetPropertyDefinition() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue(100)
                .setPropertyValueMutability(false)
                .setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)
                .setType(Integer.class)
                .build();

        BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
        MaterialId materialId = TestMaterialId.MATERIAL_1;

        BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
        builder.setMaterialId(materialId);
        builder.setPropertyId(batchPropertyId);
        builder.setPropertyDefinition(propertyDefinition);

        BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

        assertNotNull(definitionInitialization);
        assertEquals(propertyDefinition, definitionInitialization.getPropertyDefinition());

        // precondition: null property definition
        ContractException contractException = assertThrows(ContractException.class,
                () -> BatchPropertyDefinitionInitialization.builder()
                        .setPropertyDefinition(null));
        assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, contractException.getErrorType());
    }
    @Test
    @UnitTestMethod(target= BatchPropertyDefinitionInitialization.Builder.class, name = "setPropertyId", args={BatchPropertyId.class})
    public void testSetPropertyId() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue(100)
                .setPropertyValueMutability(false)
                .setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)
                .setType(Integer.class)
                .build();

        BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
        MaterialId materialId = TestMaterialId.MATERIAL_1;

        BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
        builder.setMaterialId(materialId);
        builder.setPropertyId(batchPropertyId);
        builder.setPropertyDefinition(propertyDefinition);

        BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

        assertNotNull(definitionInitialization);
        assertEquals(batchPropertyId, definitionInitialization.getPropertyId());

        // precondition: null property id
        ContractException contractException = assertThrows(ContractException.class,
                () -> BatchPropertyDefinitionInitialization.builder()
                        .setPropertyId(null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }
    @Test
    @UnitTestMethod(target= BatchPropertyDefinitionInitialization.Builder.class, name = "addPropertyValue", args={BatchId.class, Object.class})
    public void testAddPropertyValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8487271708488687492L);
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue("100")
                .setPropertyValueMutability(false)
                .setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)
                .setType(String.class)
                .build();

        BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
        MaterialId materialId = TestMaterialId.MATERIAL_1;

        BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
        builder.setMaterialId(materialId);
        builder.setPropertyId(batchPropertyId);
        builder.setPropertyDefinition(propertyDefinition);

        List<Pair<BatchId, Object>> expectedValues = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            String value = Integer.toString(randomGenerator.nextInt(100));
            BatchId batchId = new BatchId(i);
            builder.addPropertyValue(batchId, value);
            expectedValues.add(new Pair<>(batchId, value));
        }

        BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

        assertNotNull(definitionInitialization);
        assertEquals(expectedValues.size(), definitionInitialization.getPropertyValues().size());
        assertEquals(expectedValues, definitionInitialization.getPropertyValues());

        // precondition: null batch id
        ContractException contractException = assertThrows(ContractException.class,
                () -> BatchPropertyDefinitionInitialization.builder()
                        .addPropertyValue(null, "100"));
        assertEquals(MaterialsError.NULL_BATCH_ID, contractException.getErrorType());

        // precondition: null value
        contractException = assertThrows(ContractException.class,
                () -> BatchPropertyDefinitionInitialization.builder()
                        .addPropertyValue(new BatchId(0), null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());
    }
    @Test
    @UnitTestMethod(target= BatchPropertyDefinitionInitialization.Builder.class, name = "setMaterialId", args={MaterialId.class})
    public void testSetMaterialId() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder()
                .setDefaultValue(100)
                .setPropertyValueMutability(false)
                .setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)
                .setType(Integer.class)
                .build();

        BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_2_2_INTEGER_IMMUTABLE_TRACK;
        MaterialId materialId = TestMaterialId.MATERIAL_1;

        BatchPropertyDefinitionInitialization.Builder builder = BatchPropertyDefinitionInitialization.builder();
        builder.setMaterialId(materialId);
        builder.setPropertyId(batchPropertyId);
        builder.setPropertyDefinition(propertyDefinition);

        BatchPropertyDefinitionInitialization definitionInitialization = builder.build();

        assertNotNull(definitionInitialization);
        assertEquals(materialId, definitionInitialization.getMaterialId());

        // precondition: null material id
        ContractException contractException = assertThrows(ContractException.class,
                () -> BatchPropertyDefinitionInitialization.builder()
                        .setMaterialId(null));
        assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
    }
}
