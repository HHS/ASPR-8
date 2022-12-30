package plugins.globalproperties.testsupport;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GlobalsPropertiesActionSupport.class)

public class AT_GlobalsPropertiesActionSupport {



    @Test
    @UnitTestMethod(name = "testConsumer", args = {Consumer.class}, tags = {UnitTag.INCOMPLETE})
    public void testTestConsumer() {
    }

    @Test
    @UnitTestMethod(name = "testConsumers", args = {Plugin.class}, tags = {UnitTag.INCOMPLETE})
    public void testTestConsumers() {
    }
}