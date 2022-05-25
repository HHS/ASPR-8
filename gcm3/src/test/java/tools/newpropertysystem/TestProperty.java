package tools.newpropertysystem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestProperty {

    private static final Property<Integer> PROPERTY_A = new Property<>(0);
    private static final Property<Boolean> PROPERTY_B = new Property<>(false);

    @Test
    public void test() {
        PropertyContainer propertyContainer = new PropertyContainer();

        propertyContainer.set(PROPERTY_A, 1, 1);
        propertyContainer.set(PROPERTY_B, 1, true);

        assertEquals(0, propertyContainer.get(PROPERTY_A, 0));
        assertEquals(false, propertyContainer.get(PROPERTY_B, 0));
        assertEquals(1, propertyContainer.get(PROPERTY_A, 1));
        assertEquals(true, propertyContainer.get(PROPERTY_B, 1));
    }

}
