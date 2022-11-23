package plugins.materials.testsupport;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = MaterialsActionSupport.class)
public class AT_MaterialsActionSupport {

    @Test
    @UnitTestMethod(name = "testConsumer", args = { long.class, Consumer.class }, tags = { UnitTag.INCOMPLETE })
    public void testTestConsumer() {
        // TBD
    }

    @Test
    @UnitTestMethod(name = "testConsumers", args = { long.class, Plugin.class }, tags = { UnitTag.INCOMPLETE })
    public void testTestConsumers2() {
        // TBD
    }

    @Test
    @UnitTestMethod(name = "testConsumers", args = { long.class, Plugin.class, Consumer.class }, tags = {
            UnitTag.INCOMPLETE })
    public void testTestConsumers() {
        // TBD
    }
}
