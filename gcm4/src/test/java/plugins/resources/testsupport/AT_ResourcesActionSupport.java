package plugins.resources.testsupport;

import nucleus.Plugin;
import org.junit.jupiter.api.Test;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

import java.util.function.Consumer;

@UnitTest(target = ResourcesActionSupport.class)

public class AT_ResourcesActionSupport {

    @Test
    @UnitTestMethod(name = "testConsumer", args = {int.class, long.class, Consumer.class}, tags = UnitTag.INCOMPLETE)
    public void testTestConsumer() {
    }

    @Test
    @UnitTestMethod(name = "testConsumers", args = {int.class, long.class, Plugin.class}, tags = UnitTag.INCOMPLETE)
    public void testTestConsumers() {
    }
}