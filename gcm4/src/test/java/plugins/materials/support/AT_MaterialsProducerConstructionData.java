package plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.materials.testsupport.TestMaterialsProducerId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = MaterialsProducerConstructionData.class)
public class AT_MaterialsProducerConstructionData {

    @Test
    @UnitTestMethod(name = "builder", args = {})
    public void testBuilder() {
        MaterialsProducerConstructionData.Builder builder = MaterialsProducerConstructionData.builder();

        assertNotNull(builder);
    }

    @Test
    @UnitTestMethod(name = "getValues", args = { Class.class })
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

    public void testGetMaterialsProducerId() {

    }

    public void testGetMaterialsProducerPropertyValues() {

    }

    public void testGetResourceLevel() {

    }

    public void testBuild() {

    }

    public void testAddValue() {

    }

    public void testSetMaterialsPropertyValue() {

    }

    public void testSetMaterialsProducerId() {

    }

    public void testSetResourceLevel() {

    }
}
